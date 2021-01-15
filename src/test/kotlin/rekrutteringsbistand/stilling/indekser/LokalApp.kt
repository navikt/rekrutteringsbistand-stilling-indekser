package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.kafka.FakeStillingConsumer
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.kafka.FakeStillingsinfoClient
import rekrutteringsbistand.stilling.indekser.kafka.getLocalEsClient

fun main() {
    val webServer = Javalin.create()
    val stillingsinfoClient = FakeStillingsinfoClient()

    val localEsClient = getLocalEsClient()
    val elasticSearchService = ElasticSearchService(localEsClient)

    val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val fakeStillingConsumer = FakeStillingConsumer(stillingMottattService)

    val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val gammelFakeStillingConsumer = FakeStillingConsumer(gammelStillingMottattService)

    App(
        webServer,
        elasticSearchService,
        fakeStillingConsumer,
        gammelFakeStillingConsumer
    ).start()
}
