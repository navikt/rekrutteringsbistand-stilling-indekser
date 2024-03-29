package rekrutteringsbistand.stilling.indekser.autentisering

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.log
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class AccessTokenClient(private val httpClient: FuelManager) {
    private val azureClientSecret = Environment.get("AZURE_APP_CLIENT_SECRET")!!
    private val azureClientId = Environment.get("AZURE_APP_CLIENT_ID")!!
    private val tokenEndpoint = Environment.get("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")!!
    private val tokenCache: LoadingCache<String, AccessToken>

    init {
        tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(59, TimeUnit.MINUTES)
                .build { scope ->
                    refreshAccessToken(scope)
                }
    }

    fun getAccessToken(scope: String): AccessToken {
        return tokenCache.get(scope) ?: throw RuntimeException("Token-cache klarte ikke å beregne key")
    }

    private fun refreshAccessToken(scope: String): AccessToken {
        val formData = listOf(
                "grant_type" to "client_credentials",
                "client_secret" to azureClientSecret,
                "client_id" to azureClientId,
                "scope" to scope
        )

        val (_, _, result) = httpClient
                .post(tokenEndpoint, formData)
                .responseObject<AccessToken>()

        when (result) {
            is Result.Success -> return result.get()
            is Result.Failure -> throw RuntimeException("Noe feil skjedde ved henting av access_token: ", result.getException()).also {
                log.error("Noe feil skjedde ved henting av access_token: ", result.getException())
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
