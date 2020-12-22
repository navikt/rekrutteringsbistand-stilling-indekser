package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.eclipse.jetty.http.HttpStatus
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.environment
import java.lang.RuntimeException

class StillingsinfoClient(private val httpClient: FuelManager) {

    fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        val (_, response, result) = httpClient
                .get(path = "$stillingsinfoUrl/$stillingsId")
                .responseObject<Stillingsinfo>()

        when (result) {
            is Result.Success -> {
                return result.get()
            }

            is Result.Failure -> {
                if (response.statusCode == HttpStatus.NOT_FOUND_404) {
                    return null;
                }

                throw RuntimeException("Kunne ikke hente stillingsinfo for stilling $stillingsId")
            }
        }
    }

    companion object {
        val stillingsinfoUrl: String = "${environment().get("REKRUTTERINGSBISTAND_API")}/indekser/stillingsinfo"

        fun authenticateWithAzureAdToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
            val rekrutteringsbistandApiClientId = "fe698176-ac44-4260-b8d0-dbf45dd956cf"

            addBearerToken(httpClient) {
                accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
            }

            return httpClient
        }
    }
}

data class Stillingsinfo(
        val eierNavident: String?,
        val eierNavn: String?,
        val notat: String?,
        val stillingsid: String,
        val stillingsinfoid: String,
)
