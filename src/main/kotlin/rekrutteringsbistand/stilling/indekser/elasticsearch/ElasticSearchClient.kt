package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager

class ElasticSearchClient(private val httpClient: FuelManager) {
    fun indekserStilling(stilling: Stilling) {
        httpClient.put("$esUrl/$esIndex/$esType")
    }

    companion object {
        const val esUrl: String = "todo"
        const val esIndex: String = "todo"
        const val esType: String = "todo"
    }
}

// TODO: Ekspander type
data class Stilling (
    val uuid: String,
    val title: String,
)
