package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.elasticsearch.getEsClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.indeksNavnMedTimestamp
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumerImpl
import rekrutteringsbistand.stilling.indekser.kafka.StillingMottattService
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import rekrutteringsbistand.stilling.indekser.utils.log
import kotlin.Exception
import kotlin.system.exitProcess

class App {
    companion object {
        fun start(
                webServer: Javalin,
                stillingsinfoClient: StillingsinfoClient,
                elasticSearchService: ElasticSearchService,
                stillingConsumer: StillingConsumer
        ) {
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            webServer.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }

            webServer.start(8222)

            val indeksNavn = indeksNavnMedTimestamp()
            elasticSearchService.opprettIndeksHvisDenIkkeFinnes(indeksNavn)
            elasticSearchService.oppdaterAlias(indeksNavn)

            stillingConsumer.start()
        }
    }
}

fun main() {
    val webServer = Javalin.create()
    try {
        val accessTokenClient = AccessTokenClient(FuelManager())
        val httpClientAutentisertMedAccessToken = authenticateWithAccessToken(FuelManager(), accessTokenClient)
        val stillingsinfoClient = StillingsinfoClient(httpClientAutentisertMedAccessToken)

        val esClient = getEsClient()
        val elasticSearchService = ElasticSearchService(esClient)

        val stillingMottattService = StillingMottattService()
        val stillingConsumer = StillingConsumerImpl(stillingMottattService)

        App.start(
            webServer,
            stillingsinfoClient,
            elasticSearchService,
            stillingConsumer
        )

    } catch (exception: Exception) {
        log("main()").error("Noe galt skjedde, stopper appen", exception)
        webServer.stop()
        exitProcess(1)
    }
}

