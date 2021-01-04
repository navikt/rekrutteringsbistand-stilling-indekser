package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.addBasicAuthentication
import rekrutteringsbistand.stilling.indekser.environment

class ElasticSearchClient(private val httpClient: FuelManager) {
    fun indekserStilling(stilling: Stilling) {
        httpClient.put("$esUrl/$esIndex/$esType")
    }

    fun getElasticSearchInfo() {
        httpClient.get(esUrl)
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
