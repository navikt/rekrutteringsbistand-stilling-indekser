package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.addBasicAuthentication

class ElasticSearchClient(private val httpClient: FuelManager) {
    fun indekserStilling(stilling: Stilling) {
        httpClient.put("$esUrl/$esIndex/$esType")
    }

    companion object {
        const val esUrl: String = "todo"
        const val esIndex: String = "todo"
        const val esType: String = "todo"

        fun authenticateWithElasticSearchCredentials(httpClient: FuelManager): FuelManager {
            addBasicAuthentication(httpClient,
                    username = "todo",
                    password = "todo"
            )

            return httpClient
        }
    }
}

data class Stilling (
    val uuid: String,
    val title: String,
)
