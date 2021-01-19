package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.kafka.*

fun main() {
    val webServer = Javalin.create()
    val stillingsinfoClient = FakeStillingsinfoClient()

    val elasticSearchService = ElasticSearchService(getLocalEsClient())

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

