package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.addBasicAuthentication
import rekrutteringsbistand.stilling.indekser.environment
import com.github.kittinunf.result.Result

class ElasticSearchClient(private val httpClient: FuelManager) {
    fun indekserStilling(stilling: Stilling) {
        httpClient.put("$esUrl/$esIndex/$esType")
    }

    fun printElasticSearchInfo() {
        val (_, response, result) = httpClient.get(esUrl).response()


        when (result) {
            is Result.Success -> {
                println("ES-info: $result $response")
            }

            is Result.Failure -> {
                println("Klarte ikke å hente ES-info")
            }
        }
    }

    companion object {
        val esUrl: String = environment().get("ELASTIC_SEARCH_API")
        const val esIndex: String = "todo"
        const val esType: String = "todo"

        fun authenticateWithElasticSearchCredentials(httpClient: FuelManager): FuelManager {
            addBasicAuthentication(httpClient,
                    username = environment().get("ES_USERNAME"),
                    password = environment().get("ES_PASSWORD")
            )

            return httpClient
        }
    }
}

data class Stilling (
    val uuid: String,
    val title: String,
)
