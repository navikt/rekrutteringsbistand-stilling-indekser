package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager
import io.javalin.Javalin
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.kafka.FakeStillingConsumer
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.kafka.getLocalEsClient
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

fun main() {
    val webServer = Javalin.create()

    val localHttpClient = FuelManager()
    val stillingsinfoClient = StillingsinfoClient(localHttpClient)

    val localEsClient = getLocalEsClient()
    val elasticSearchService = ElasticSearchService(localEsClient)

    val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val fakeStillingConsumer = FakeStillingConsumer(stillingMottattService)

    App.start(
        webServer,
        stillingsinfoClient,
        elasticSearchService,
        fakeStillingConsumer
    )
}
