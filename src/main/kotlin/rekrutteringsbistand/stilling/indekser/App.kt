package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.elasticsearch.getEsClient
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumerImpl
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClientImpl
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.log
import kotlin.Exception
import kotlin.system.exitProcess

class App {
    companion object {
        fun start(
            webServer: Javalin,
            elasticSearchService: ElasticSearchService,
            stillingConsumer: StillingConsumer,
            gammelStillingConsumer: StillingConsumer
        ) {
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            webServer.routes {
                get("$basePath/internal/isAlive") { it.status(200) }
                get("$basePath/internal/isReady") { it.status(200) }
                // TODO Not implemented
                get("$basePath/internal/byttIndeks") { it.status(501) }
            }.start(8222)

            val skalReindeksere = elasticSearchService.skalReindeksere()
            if (skalReindeksere) {


                val indeksNavn = elasticSearchService.hentIndeksNavn(elasticSearchService.hentVersjonFraNaisConfig())
                log.info("Starter eller fortsetter reindeksering med indeks $indeksNavn")
                if (elasticSearchService.nyIndeksErIkkeOpprettet()) {
                    // TODO: Hent indeksNavn fra et sted og refaktorer
                    elasticSearchService.opprettIndeks(indeksNavn)
                }

                // Start gammel stillingConsumer med ny config
                val gammeltIndeksNavn = elasticSearchService.hentIndeksAliasPekerPå()
                gammelStillingConsumer.start(gammeltIndeksNavn)

            } else {

                val indeksNavn = elasticSearchService.hentIndeksNavn(elasticSearchService.hentVersjonFraNaisConfig())
                val indeksBleOpprettet = elasticSearchService.opprettIndeksHvisDenIkkeFinnes(indeksNavn)
                if (indeksBleOpprettet) elasticSearchService.oppdaterAlias(indeksNavn)
            }

            try {
                val indeksNavn = elasticSearchService.hentIndeksNavn(elasticSearchService.hentVersjonFraNaisConfig())
                stillingConsumer.start(indeksNavn)

            } catch (exception: Exception) {
                log.error("Midlertidig feilhåndtering for at appen ikke skal kræsje ved exception", exception)
            }
        }
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

        val kafkaConsumer = KafkaConsumer<String, Ad>(
            consumerConfig(groupId = "rekrutteringsbistand-stilling-indekser-${elasticSearchService.hentVersjonFraNaisConfig()}")
        )
        val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
        val stillingConsumer = StillingConsumerImpl(kafkaConsumer, stillingMottattService)

        val gammelKafkaConsumer = KafkaConsumer<String, Ad>(
            consumerConfig(groupId = "rekrutteringsbistand-stilling-indekser-${elasticSearchService.hentVersjonAliasPekerPå()}")
        )
        val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
        val gammelStillingConsumer = StillingConsumerImpl(gammelKafkaConsumer, gammelStillingMottattService)

        App.start(
            webServer,
            elasticSearchService,
            stillingConsumer,
            gammelStillingConsumer
        )

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde, stopper appen", exception)
        webServer.stop()
        exitProcess(1)
    }
}

