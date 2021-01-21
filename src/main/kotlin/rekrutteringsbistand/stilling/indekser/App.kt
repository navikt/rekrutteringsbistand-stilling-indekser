package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClientImpl
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.log
import java.io.Closeable
import kotlin.concurrent.thread

class App(
    private val webServer: Javalin,
    private val elasticSearchService: ElasticSearchService,
    private val stillingConsumer: StillingConsumer,
    private val gammelStillingConsumer: StillingConsumer?
) : Closeable {
    fun start() {
        webServer.routes {
            get("/internal/isAlive") { it.status(200) }
            get("/internal/isReady") { it.status(200) }
            get("/internal/byttIndeks") {
                byttIndeks(it, gammelStillingConsumer, elasticSearchService)
            }
        }.start(8222)

        if (elasticSearchService.skalReindeksere()) {
            startReindeksering()
        } else {
            startIndeksering()
        }
    }

    private fun startReindeksering() {
        val nyIndeks = hentNyesteIndeks()
        val gjeldendeIndeks = elasticSearchService.hentGjeldendeIndeks() ?: kanIkkeStarteReindeksering()
        elasticSearchService.initialiserReindeksering(nyIndeks, gjeldendeIndeks)

        thread { stillingConsumer.start(nyIndeks) }
        thread { gammelStillingConsumer!!.start(gjeldendeIndeks) }
    }

    private fun startIndeksering() {
        val indeks = hentNyesteIndeks()
        elasticSearchService.initialiserIndeksering(indeks)

        thread { stillingConsumer.start(indeks) }
    }

    override fun close() {
        stillingConsumer.close()
        gammelStillingConsumer?.close()
        webServer.stop()
    }
}

fun main() {
    val webServer = Javalin.create()
    try {
        val accessTokenClient = AccessTokenClient(FuelManager())
        val httpClientAutentisertMedAccessToken = authenticateWithAccessToken(FuelManager(), accessTokenClient)
        val stillingsinfoClient = StillingsinfoClientImpl(httpClientAutentisertMedAccessToken)

        val esClient = ElasticSearchClient(getRestHighLevelClient())
        val elasticSearchService = ElasticSearchService(esClient)

        val versjonTilStillingConsumer = hentVersjonFraNaisConfig()
        val kafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilStillingConsumer))
        val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
        val stillingConsumer = StillingConsumer(kafkaConsumer, stillingMottattService)

        val skalReindeksere = elasticSearchService.skalReindeksere()
        val gammelStillingConsumer = if (skalReindeksere) {
            val versjonTilGammelConsumer = elasticSearchService.hentGjeldendeIndeksversjon()
                ?: kanIkkeStarteReindeksering()
            val gammelKafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilGammelConsumer))

            val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
            StillingConsumer(gammelKafkaConsumer, gammelStillingMottattService)
        } else null

        App(
            webServer,
            elasticSearchService,
            stillingConsumer,
            gammelStillingConsumer
        ).start()
        // TODO Are: Trenger vi å kjøre App.close() e.l. for å stoppe Javalin sin webserver hvos noe av det andre kræsjer? HVis noe annet kræsjer, vil Javalin fortsette å returnere HTTP 200 på isALive og isReady? Se kode for oppstart av LokalApp.

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde – indekseringen er stoppet", exception)
    }
}

private fun kanIkkeStarteReindeksering(): Nothing {
    throw Exception("Kan ikke starte reindeksering uten noen alias som peker på indeks")
}
