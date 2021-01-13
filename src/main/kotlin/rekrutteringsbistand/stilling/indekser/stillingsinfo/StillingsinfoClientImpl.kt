package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.eclipse.jetty.http.HttpStatus
import rekrutteringsbistand.stilling.indekser.utils.environment
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingsinfoClientImpl(private val httpClient: FuelManager): StillingsinfoClient {
    private val stillingsinfoUrl: String = "${environment().get("REKRUTTERINGSBISTAND_API_URL")}/indekser/stillingsinfo"

    override fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        val (_, response, result) = httpClient
            .get(path = "$stillingsinfoUrl/$stillingsId")
            .responseObject<Stillingsinfo>()

        when (result) {
            is Result.Success -> {
                val stillingsinfo = result.get()
                log.info("Fant stilling med stillingsinfo: ${stillingsinfo.stillingsid}")
                return stillingsinfo
            }

            is Result.Failure -> {
                if (response.statusCode == HttpStatus.NOT_FOUND_404) {
                    return null
                }

                // TODO: Ikke kaste exception her?
                throw Exception(
                    "Kunne ikke hente stillingsinfo for stilling $stillingsId." +
                    "HTTP-status: ${response.statusCode}, responseMessage: ${response.responseMessage}"
                )
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

interface StillingsinfoClient {
    fun getStillingsinfo(stillingsId: String): Stillingsinfo?
}
