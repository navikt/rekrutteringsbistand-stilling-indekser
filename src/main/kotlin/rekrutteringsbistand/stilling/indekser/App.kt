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

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }

            val stillingsinfoClient = StillingsinfoClient(httpClient)

            try {
                stillingsinfoClient.getStillingsinfo("ad48f1ca-b71d-46a4-b94d-cdfbf5803faf")
            } catch (error: Error) {
                println("Klarte ikke å hente stillingsinfo: $error")
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
            println("Fikk token: $token")
            request.authentication().bearer(token.access_token)
            it(request)
        }
    }
}
