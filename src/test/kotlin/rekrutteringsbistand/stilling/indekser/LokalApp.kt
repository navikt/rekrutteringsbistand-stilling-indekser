package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.kafka.FakeStillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.StillingMottattService
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

fun main() {
    val localHttpClient = FuelManager()

    val stillingsinfoClient = StillingsinfoClient(localHttpClient)
    val elasticSearchClient = ElasticSearchClient(localHttpClient)

    val stillingMottattService = StillingMottattService()
    val fakeStillingConsumer = FakeStillingConsumer(stillingMottattService)

    App.start(
        stillingsinfoClient,
        elasticSearchClient,
        fakeStillingConsumer
    )
}
