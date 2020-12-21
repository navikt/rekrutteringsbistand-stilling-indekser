package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.eclipse.jetty.http.HttpStatus
import rekrutteringsbistand.stilling.indekser.environment.environment
import java.lang.RuntimeException

class StillingsinfoClient(private val httpClient: FuelManager) {

    fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        val url = "${environment().get("REKRUTTERINGSBISTAND_API")}/indekser/stillingsinfo/$stillingsId"
        val (_, response, result) = httpClient.get(path = url).responseObject<Stillingsinfo>()

        when (result) {
            is Result.Success -> {
                return result.get()
            }

            is Result.Failure -> {
                if (response.statusCode == HttpStatus.NOT_FOUND_404) {
                    return null;
                }

                throw RuntimeException("Kunne ikke hente stillingsinfo for stilling $stillingsId: ", result.getException())
            }
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