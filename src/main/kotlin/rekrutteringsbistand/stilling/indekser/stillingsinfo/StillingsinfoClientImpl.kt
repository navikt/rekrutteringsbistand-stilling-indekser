package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingsinfoClientImpl(private val httpClient: FuelManager): StillingsinfoClient {
    private val stillingsinfoUrl: String = "${Environment.get("REKRUTTERINGSBISTAND_STILLING_API_URL")}/indekser/stillingsinfo"
    private val internStillingUrl: String = "${Environment.get("REKRUTTERINGSBISTAND_STILLING_API_URL")}/rekrutteringsbistandstilling/lagre"

    init {
        log.info("Setter opp stillinginfo-klient til å gå mot $stillingsinfoUrl")
    }

    override fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo> {
        val body = BulkStillingsinfoOutboundDto(stillingsIder)
        val (_, response, result) = httpClient
            .post(path = "$stillingsinfoUrl/bulk")
            .objectBody(body)
            .responseObject<List<Stillingsinfo>>()

        when (result) {
            is Result.Success -> { return result.get() }
            is Result.Failure -> {
                throw KunneIkkeHenteStillingsinsinfoException(
                    "Kunne ikke hente stillingsinfo for stillinger." +
                    "HTTP-status: ${response.statusCode}, responseMessage: ${response.responseMessage}"
                )
            }
        }
    }

    override fun sendStillingsId(stillingsid: String) {
        val (_, response, result) = httpClient
            .post(path = internStillingUrl)
            .body(stillingsid)
            .response()

        log.info("Sender stillingsid $stillingsid til stilling-api fordi stilling er oppdatert")

        when (result) {
            is Result.Success -> { log.info("Sendte oppdatering til stiling-api for $stillingsid"); return }
            is Result.Failure -> {
                log.warn("Kunne ikke sende oppdatering til stilling-api for $stillingsid" +
                        "HTTP-status: ${response.statusCode}, responseMessage: ${response.responseMessage}")
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
    val stillingskategori: String?
)

data class BulkStillingsinfoOutboundDto(
    val uuider: List<String>
)

interface StillingsinfoClient {
    fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo>
    fun sendStillingsId(stillingsid: String)
}

class KunneIkkeHenteStillingsinsinfoException(melding: String) : Exception(melding)
class KunneIkkeSendeStillingsOppdateringException(melding: String) : Exception(melding)
