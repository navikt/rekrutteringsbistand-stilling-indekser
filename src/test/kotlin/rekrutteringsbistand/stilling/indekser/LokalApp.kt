package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.kafka.*

fun main() {
    startLokalApp()
}

fun startLokalApp(
    webServer: Javalin = Javalin.create(),
) {

    val esClient = ElasticSearchClient(getLocalRestHighLevelClient())
//    val esClientMock = mockk<RestHighLevelClient>()

    val elasticSearchService = ElasticSearchService(esClient)
    val stillingsinfoClient = FakeStillingsinfoClient()

    val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val stillingConsumer = StillingConsumer(mockConsumer(), stillingMottattService)

    val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val gammelStillingConsumer = StillingConsumer(mockConsumer(), gammelStillingMottattService)

    App(
        webServer,
        elasticSearchService,
        stillingConsumer,
        gammelStillingConsumer
    ).start()
}
