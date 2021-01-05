package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

fun main() {
    val localHttpClient = FuelManager()

    val stillingsinfoClient = StillingsinfoClient(localHttpClient)
    val elasticSearchClient = ElasticSearchClient(localHttpClient)

    App.start(stillingsinfoClient, elasticSearchClient)
}
