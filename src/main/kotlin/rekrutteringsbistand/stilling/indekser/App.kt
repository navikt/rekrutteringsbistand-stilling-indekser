package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.Context
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumerImpl
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClientImpl
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.log
import kotlin.Exception

class App(
        private val webServer: Javalin,
        private val elasticSearchService: ElasticSearchService,
        private val stillingConsumer: StillingConsumer,
        private val gammelStillingConsumer: StillingConsumer?
) {
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
        val gjeldendeIndeks = elasticSearchService.hentGjeldendeIndeks()
                ?: kanIkkeStarteReindeksering()

        if (elasticSearchService.nyIndeksErIkkeOpprettet()) {
            elasticSearchService.opprettIndeks(nyIndeks)
            log.info("Starter reindeksering på ny indeks $nyIndeks")
        } else {
            log.info("Gjenopptar reindeksering på ny indeks $nyIndeks")
        }

        log.info("Fortsetter samtidig konsumering på gjeldende indeks $gjeldendeIndeks")

        gammelStillingConsumer!!.start(gjeldendeIndeks)
        stillingConsumer.start(nyIndeks)
    }

    private fun startIndeksering() {
        val indeks = hentNyesteIndeks()
        val indeksBleOpprettet = elasticSearchService.opprettIndeksHvisDenIkkeFinnes(indeks)
        if (indeksBleOpprettet)
            elasticSearchService.oppdaterAlias(indeks)

        stillingConsumer.start(indeks)
    }
}

fun main() {
    val webServer = Javalin.create()
    try {
        val accessTokenClient = AccessTokenClient(FuelManager())
        val httpClientAutentisertMedAccessToken = authenticateWithAccessToken(FuelManager(), accessTokenClient)
        val stillingsinfoClient = StillingsinfoClientImpl(httpClientAutentisertMedAccessToken)

        val esClient = getEsClient()
        val elasticSearchService = ElasticSearchService(esClient)

        val versjonTilStillingConsumer = hentVersjonFraNaisConfig()
        val kafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilStillingConsumer))
        val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
        val stillingConsumer = StillingConsumerImpl(kafkaConsumer, stillingMottattService)

        val skalReindeksere = elasticSearchService.skalReindeksere()
        val gammelStillingConsumer = if (skalReindeksere) {
            val versjonTilGammelConsumer = elasticSearchService.hentGjeldendeIndeksversjon()
                    ?: kanIkkeStarteReindeksering()
            val gammelKafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(versjonTilGammelConsumer))

            val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
            StillingConsumerImpl(gammelKafkaConsumer, gammelStillingMottattService)
        } else null

        App(
            webServer,
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