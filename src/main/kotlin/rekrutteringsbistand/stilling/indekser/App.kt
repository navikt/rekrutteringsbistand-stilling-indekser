package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import org.slf4j.LoggerFactory
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.authenticateWithElasticSearchCredentials
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumerImpl
import rekrutteringsbistand.stilling.indekser.kafka.StillingMottattService
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import kotlin.Exception
import kotlin.system.exitProcess

class App {
    companion object {
        fun start(
            webServer: Javalin,
            stillingsinfoClient: StillingsinfoClient,
            elasticSearchClient: ElasticSearchClient,
            stillingConsumer: StillingConsumer
        ) {
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            webServer.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }

            webServer.start(8222)
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

        val httpClientAutentisertMedEsCredentials = authenticateWithElasticSearchCredentials(FuelManager())
        val elasticSearchClient = ElasticSearchClient(httpClientAutentisertMedEsCredentials)

        val stillingMottattService = StillingMottattService()
        val stillingConsumer = StillingConsumerImpl(stillingMottattService)

        App.start(
            webServer,
            stillingsinfoClient,
            elasticSearchClient,
            stillingConsumer
        )

    } catch (exception: Exception) {
        LoggerFactory.getLogger("main()").error("Noe galt skjedde, stopper appen", exception)
        webServer.stop()
        exitProcess(1)
    }
}

