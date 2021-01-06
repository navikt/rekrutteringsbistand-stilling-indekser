package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.authenticateWithElasticSearchCredentials
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumerImpl
import rekrutteringsbistand.stilling.indekser.kafka.StillingMottattService
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.authenticateWithAccessToken
import java.lang.Exception

class App {
    companion object {
        fun start(
            stillingsinfoClient: StillingsinfoClient,
            elasticSearchClient: ElasticSearchClient,
            stillingConsumer: StillingConsumer
        ) {
            val app = Javalin.create().start(8222)
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            var isAlive = true

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(if (isAlive) 200 else 500) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }

            try {
                stillingConsumer.start()
            } catch (exception: Exception) {
                log.error("StillingConsumer feilet, Setter isAlive = false", exception)
                isAlive = false
            }
        }
    }
}

fun main() {
    val accessTokenClient = AccessTokenClient(FuelManager())
    val httpClientAutentisertMedAccessToken = authenticateWithAccessToken(FuelManager(), accessTokenClient)
    val stillingsinfoClient = StillingsinfoClient(httpClientAutentisertMedAccessToken)

    val httpClientAutentisertMedEsCredentials = authenticateWithElasticSearchCredentials(FuelManager())
    val elasticSearchClient = ElasticSearchClient(httpClientAutentisertMedEsCredentials)

    val stillingMottattService = StillingMottattService()
    val stillingConsumer = StillingConsumerImpl(stillingMottattService)

    App.start(stillingsinfoClient, elasticSearchClient, stillingConsumer)
}

