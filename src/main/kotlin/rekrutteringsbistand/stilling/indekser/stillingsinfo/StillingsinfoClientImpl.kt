package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import rekrutteringsbistand.stilling.indekser.utils.Environment

class StillingsinfoClientImpl(private val httpClient: FuelManager): StillingsinfoClient {
    private val stillingsinfoUrl: String = "${Environment.get("REKRUTTERINGSBISTAND_API_URL")}/indekser/stillingsinfo"

    override fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo> {
        val body = BulkStillingsinfoOutboundDto(stillingsIder)
        val (_, response, result) = httpClient
            .post(path = "$stillingsinfoUrl/bulk")
            .objectBody(body)
            .responseObject<List<Stillingsinfo>>()

        when (result) {
            is Result.Success -> { return result.get() }
            is Result.Failure -> {
                throw Exception(
                    "Kunne ikke hente stillingsinfo for stillinger." +
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

data class BulkStillingsinfoOutboundDto(
    val uuider: List<String>
)

interface StillingsinfoClient {
    fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo>
}
