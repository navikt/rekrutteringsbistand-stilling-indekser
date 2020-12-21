package rekrutteringsbistand.stilling.indekser.autentisering

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication

fun authenticateAllRequests(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandApiClientId = "fe698176-ac44-4260-b8d0-dbf45dd956cf"

    addAccessToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
    }

    return httpClient
}

private fun addAccessToken(httpClient: FuelManager, getToken: () -> AccessToken) {
    httpClient.addRequestInterceptor {
        { request ->
            val token = getToken()
            request.authentication().bearer(token.access_token)
            it(request)
        }
    }
}