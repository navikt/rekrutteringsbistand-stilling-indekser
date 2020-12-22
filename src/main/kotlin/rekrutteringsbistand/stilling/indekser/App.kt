package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class App {
    companion object {
        fun start(stillingsinfoClient: StillingsinfoClient, elasticSearchClient: ElasticSearchClient) {
            val app = Javalin.create().start(8222)
            val basePath = "/rekrutteringsbistand-stilling-indekser"

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }
        }
    }
}

fun main() {
    val defaultHttpClient = FuelManager()

    val accessTokenClient = AccessTokenClient(defaultHttpClient)
    val stillingsinfoClient = StillingsinfoClient(
            StillingsinfoClient.authenticateWithAccessToken(defaultHttpClient, accessTokenClient))

    val elasticSearchClient = ElasticSearchClient(
            ElasticSearchClient.authenticateWithElasticSearchCredentials(defaultHttpClient))

    App.start(stillingsinfoClient, elasticSearchClient)
}

