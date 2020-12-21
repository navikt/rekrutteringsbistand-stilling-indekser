package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class App {
    companion object {
        fun start(httpClient: FuelManager) {
            val app = Javalin.create().start(8222)
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            val stillingsinfoClient = StillingsinfoClient(httpClient)

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
                get("$basePath/stilling/:id") { ctx ->
                    val stillingsId = ctx.pathParam("id")

                    try {
                        val stillingsinfo = stillingsinfoClient.getStillingsinfo(stillingsId)
                        ctx.json(stillingsinfo)
                    } catch (error: Error) {
                        println("Klarte ikke å hente stillingsinfo: $error")
                        ctx.status(500)
                    }
                }
            }
        }
    }
}

fun main() {
    val accessTokenClient = AccessTokenClient()
    val autentisertHttpClient = FuelManager()

    autentiserKallMedAccessToken(autentisertHttpClient, accessTokenClient)

    App.start(autentisertHttpClient)
}

fun autentiserKallMedAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient) {
    httpClient.addRequestInterceptor {
        { request ->
            val token = accessTokenClient.getAccessToken()
            request.authentication().bearer(token.access_token)
            it(request)
        }
    }
}
