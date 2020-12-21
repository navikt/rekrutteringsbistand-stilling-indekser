package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient

class App {
    companion object {
        fun start() {
            val app = Javalin.create().start(8222)
            val basePath = "/rekrutteringsbistand-stilling-indekser"
            val accessTokenClient = AccessTokenClient()

            accessTokenClient.getAccessToken()

            //    val autentisertHttpClient = FuelManager()
            //    autentisertHttpClient.addRequestInterceptor {{ request ->
            //            val token = accessTokenClient.getAccessToken()
            //            request.authentication().bearer(token.access_token)
            //            it(request)
            //        }
            //    }

            app.routes {
                get("$basePath/internal/isAlive") { ctx -> ctx.status(200) }
                get("$basePath/internal/isReady") { ctx -> ctx.status(200) }
            }
        }
    }
}

fun main() {
    App.start()
}
