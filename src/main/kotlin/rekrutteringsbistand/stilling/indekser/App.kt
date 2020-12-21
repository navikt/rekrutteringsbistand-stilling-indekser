package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.NotFoundResponse
import org.eclipse.jetty.http.HttpStatus
import rekrutteringsbistand.stilling.indekser.autentisering.AccessToken
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
        }
    }
}

fun main() {
    val accessTokenClient = AccessTokenClient()
    val defaultHttpClient = FuelManager()
    val authenticatedClient = authenticateAllRequests(defaultHttpClient, accessTokenClient)

    App.start(authenticatedClient)
}

fun authenticateAllRequests(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandApiClientId = "fe698176-ac44-4260-b8d0-dbf45dd956cf"

    addAccessToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
    }

    return httpClient
}

fun addAccessToken(httpClient: FuelManager, getToken: () -> AccessToken) {
    httpClient.addRequestInterceptor {
        { request ->
            val token = getToken()
            request.authentication().bearer(token.access_token)
            it(request)
        }
    }
}
