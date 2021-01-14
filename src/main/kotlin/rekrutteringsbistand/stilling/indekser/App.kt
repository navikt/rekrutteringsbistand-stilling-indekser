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
import rekrutteringsbistand.stilling.indekser.elasticsearch.indeksNavnMedTimestamp
import rekrutteringsbistand.stilling.indekser.kafka.consumerConfig
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
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
            nyStillingConsumer: StillingConsumer
        ) {
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            webServer.routes {
                get("$basePath/internal/isAlive") { it.status(200) }
                get("$basePath/internal/isReady") { it.status(200) }
                // TODO Not implemented
                get("$basePath/internal/byttIndeks") { it.status(501) }
            }

            val skalReindeksere = true
            if (skalReindeksere) {
                elasticSearchService.opprettIndeks(indeksNavn = "nyindeks")
                // Start ny stillingConsumer med ny config
                nyStillingConsumer.konsumerTopicFraBegynnelse()
                nyStillingConsumer.start()
            }

            webServer.start(8222)
            try {
                elasticSearchService.initialiser()
                stillingConsumer.start()

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

        val kafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig())
        val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
        val stillingConsumer = StillingConsumerImpl(kafkaConsumer, stillingMottattService)

        val nyKafkaConsumer = KafkaConsumer<String, Ad>(consumerConfig(groupId = "rekbis-indekser-ny-konsumer"))
        val nyStillingMottattService = StillingMottattService(
            stillingsinfoClient,
            elasticSearchService,
            indeksNavn = indeksNavnMedTimestamp()
        )
        val nyStillingConsumer = StillingConsumerImpl(nyKafkaConsumer, nyStillingMottattService)

        App.start(
            webServer,
            elasticSearchService,
            stillingConsumer,
            nyStillingConsumer
        )

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde, stopper appen", exception)
        webServer.stop()
        exitProcess(1)
    }
}

