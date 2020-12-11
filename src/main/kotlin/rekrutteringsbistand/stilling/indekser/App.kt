package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient

fun main() {
    val app = Javalin.create().start(8222)

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
        get("/internal/isAlive") { ctx -> ctx.status(200) }
        get("/internal/isReady") { ctx -> ctx.status(200) }
    }
}
