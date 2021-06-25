package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.pam.stilling.ext.avro.Ad
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClientImpl
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.Liveness
import rekrutteringsbistand.stilling.indekser.utils.log
import java.io.Closeable
import kotlin.concurrent.thread

class App(
    private val elasticSearchService: ElasticSearchService,
    private val stillingConsumer: StillingConsumer,
    private val gammelStillingConsumer: StillingConsumer?
) : Closeable {

    private val webServer = Javalin.create().apply {
        config.defaultContentType = "application/json"
        routes {
            get("/internal/isAlive") { if (Liveness.isAlive) it.status(200) else it.status(500) }
            get("/internal/isReady") { it.status(200) }
            get("/internal/byttIndeks") {
                byttIndeks(it, gammelStillingConsumer, elasticSearchService)
            }
        }
    }

    fun start() = try {
        webServer.start(8222)

        if (elasticSearchService.skalReindeksere()) {
            startReindeksering()
        } else {
            startIndeksering()
        }

        RapidApplication.create(System.getenv()).apply {
            EierOppdatert(this)
            register(StansApplikasjonOmRapidApplikasjonSlåsAv)
        }
    } catch (exception: Exception) {
        close()
        throw exception
    }

    private object StansApplikasjonOmRapidApplikasjonSlåsAv : RapidsConnection.StatusListener {
        override fun onShutdown(rapidsConnection: RapidsConnection) {
            Liveness.kill(
                "Rapidapplikasjonen stenges ned.",
                RuntimeException("Rapidapplikasjonen stenges ned.")
            )
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
            elasticSearchService,
            stillingConsumer,
            gammelStillingConsumer
        ).start()

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde – indekseringen er stoppet", exception)
    }
}

private fun kanIkkeStarteReindeksering(): Nothing {
    throw Exception("Kan ikke starte reindeksering uten noen alias som peker på indeks")
}
