package rekrutteringsbistand.stilling.indekser.autentisering

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import rekrutteringsbistand.stilling.indekser.environment.environment
import java.lang.RuntimeException

class AccessTokenClient {

    fun getAccessToken(scope: String): AccessToken {
        val formData = listOf(
            "grant_type" to "client_credentials",
            "client_secret" to azureClientSecret,
            "client_id" to azureClientId,
            "scope" to scope
        )

        println("Henter access_token for request")
        val (_, _, result) = Fuel
            .post("https://login.microsoftonline.com/$azureTenantId/oauth2/v2.0/token", formData)
            .responseObject<AccessToken>()

        when (result) {
            is Result.Success -> {
                val accessToken = result.get()
                println("Fikk access_token med lengde ${accessToken.access_token.length}")
                return accessToken
            }

            is Result.Failure -> {
                throw RuntimeException("Noe feil skjedde ved henting av access_token: ", result.getException())
            }
        }
    }

    companion object {
        val azureClientSecret: String = environment().get("AZURE_APP_CLIENT_SECRET")
        val azureClientId: String = environment().get("AZURE_APP_CLIENT_ID")
        val azureTenantId: String = environment().get("AZURE_APP_TENANT_ID")
    }
}

data class AccessToken(
    val token_type: String,
    val expires_in: Int,
    val ext_expires_in: Int,
    val access_token: String
)
