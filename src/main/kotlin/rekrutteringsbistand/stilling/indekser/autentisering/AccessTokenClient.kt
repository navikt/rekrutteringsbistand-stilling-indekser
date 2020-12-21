package rekrutteringsbistand.stilling.indekser.autentisering

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import io.github.cdimascio.dotenv.dotenv
import java.lang.RuntimeException

class AccessTokenClient {

    fun getAccessToken(): AccessToken {
        val environment = dotenv()

        val formData = listOf(
            "grant_type" to "client_credentials",
            "client_secret" to environment.get("AZURE_APP_CLIENT_SECRET"),
            "client_id" to environment.get("AZURE_APP_CLIENT_ID"),
            "scope" to "api://fe698176-ac44-4260-b8d0-dbf45dd956cf/.default"
        )

        val tenant = environment.get("AZURE_APP_TENANT_ID")
        val (_, response, result) = Fuel
            .post("https://login.microsoftonline.com/$tenant/oauth2/v2.0/token", formData)
            .responseObject<AccessToken>()

        when (result) {
            is Result.Success -> {
                val accessToken = result.get()
                println("access_token lengde: ${accessToken.access_token.length}")
                return accessToken
            }
            is Result.Failure -> {
                println("Feilmelding: " + String(response.data))
                throw RuntimeException("Noe feil skjedde: ", result.getException())
            }
        }
    }
}

data class AccessToken(
    val token_type: String,
    val expires_in: Int,
    val ext_expires_in: Int,
    val access_token: String
)
