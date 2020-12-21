package rekrutteringsbistand.stilling.indekser.autentisering

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication

fun authenticateWithAzureAdToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandApiClientId = "fe698176-ac44-4260-b8d0-dbf45dd956cf"

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
    }

    return httpClient
}

fun authenticateWithElasticSearchCredentials(httpClient: FuelManager): FuelManager {
    addBasicAuthentication(httpClient,
            username = "todo",
            password = "todo"
    )

    return httpClient
}

private fun addBearerToken(httpClient: FuelManager, getToken: () -> AccessToken) {
    httpClient.addRequestInterceptor {
        { request ->
            val token = getToken()
            request.authentication().bearer(token.access_token)
            it(request)
        }
    }
}

private fun addBasicAuthentication(httpClient: FuelManager, username: String, password: String) {
    httpClient.addRequestInterceptor {
        { request ->
            request.authentication().basic(username, password)
            it(request)
        }
    }
}
