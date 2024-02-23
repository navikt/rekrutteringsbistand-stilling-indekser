package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import no.nav.pam.stilling.ext.avro.Ad
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.opensearch.*
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClientImpl
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.Liveness
import rekrutteringsbistand.stilling.indekser.utils.log
import java.io.Closeable
import kotlin.concurrent.thread

class App(
    private val openSearchService: OpenSearchService,
    private val stillingConsumer: StillingConsumer,
    private val gammelStillingConsumer: StillingConsumer?
) : Closeable {

    private val webServer = Javalin.create { config ->
        config.http.defaultContentType = "application/json"
    }.apply {
        routes {
            get("/internal/isAlive") { if (Liveness.isAlive) it.status(200) else it.status(500) }
            get("/internal/isReady") { it.status(200) }
            get("/internal/byttIndeks") {
                byttIndeks(it, gammelStillingConsumer, openSearchService)
            }
        }
    }

    fun start() = try {
        webServer.start(8222)

        if (openSearchService.skalReindeksere()) {
            startReindeksering()
        } else {
            startIndeksering()
        }

    } catch (exception: Exception) {
        close()
        throw exception
    }

    private fun startReindeksering() {
        val nyIndeks = hentNyesteIndeks()
        val gjeldendeIndeks = openSearchService.hentGjeldendeIndeks() ?: kanIkkeStarteReindeksering()
        openSearchService.initialiserReindeksering(nyIndeks, gjeldendeIndeks)

        thread { stillingConsumer.start(nyIndeks) }
        thread { gammelStillingConsumer!!.start(gjeldendeIndeks) }
    }

    private fun startIndeksering() {
        val indeks = hentNyesteIndeks()
        openSearchService.initialiserIndeksering(indeks)

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

        val osClient = OpenSearchClient(getRestHighLevelClient())
        val openSearchService = OpenSearchService(osClient)

        val versjonTilStillingConsumer = hentVersjonFraNaisConfig()
        val kafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilStillingConsumer))
        val stillingMottattService = StillingMottattService(stillingsinfoClient, openSearchService)
        val stillingConsumer = StillingConsumer(kafkaConsumer, stillingMottattService)

        val skalReindeksere = openSearchService.skalReindeksere()
        val gammelStillingConsumer = if (skalReindeksere) {
            val versjonTilGammelConsumer = openSearchService.hentGjeldendeIndeksversjon()
                ?: kanIkkeStarteReindeksering()
            val gammelKafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilGammelConsumer))

            val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, openSearchService)
            StillingConsumer(gammelKafkaConsumer, gammelStillingMottattService)
        } else null

        App(
            openSearchService,
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
