package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.environment

class ElasticSearchClient(private val httpClient: FuelManager) {
    private val esUrl: String = environment().get("ELASTIC_SEARCH_API")
    private val esIndex: String = "todo"
    private val esType: String = "todo"

    fun indekserStilling(stilling: Stilling) {
        httpClient.put("$esUrl/$esIndex/$esType")
    }
}

data class Stilling (
    val uuid: String,
    val title: String,
)
