package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.eclipse.jetty.http.HttpStatus
import rekrutteringsbistand.stilling.indekser.utils.environment

class StillingsinfoClientImpl(private val httpClient: FuelManager): StillingsinfoClient {
    private val stillingsinfoUrl: String = "${environment().get("REKRUTTERINGSBISTAND_API")}/indekser/stillingsinfo"

    override fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        val (_, response, result) = httpClient
            .get(path = "$stillingsinfoUrl/$stillingsId")
            .responseObject<Stillingsinfo>()

        when (result) {
            is Result.Success -> {
                return result.get()
            }

            is Result.Failure -> {
                if (response.statusCode == HttpStatus.NOT_FOUND_404) {
                    return null
                }

                // TODO: Ikke kaste exception her?
                throw Exception("Kunne ikke hente stillingsinfo for stilling $stillingsId")
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