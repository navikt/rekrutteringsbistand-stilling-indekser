package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.Consumer
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.kafka.*
import rekrutteringsbistand.stilling.indekser.setup.FakeStillingsinfoClient
import rekrutteringsbistand.stilling.indekser.setup.getLocalRestHighLevelClient
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer

fun main() {
    val app = lagLokalApp()
    app.start()
}

fun lagLokalApp(
    webServer: Javalin = Javalin.create(),
    mockConsumer: Consumer<String, Ad> = mockConsumer(periodiskSendMeldinger = true),
    esClient: ElasticSearchClient = ElasticSearchClient(getLocalRestHighLevelClient()),
): App {

    val stillingsinfoClient = FakeStillingsinfoClient()

    val elasticSearchService = ElasticSearchService(esClient)
    val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val stillingConsumer = StillingConsumer(mockConsumer, stillingMottattService)

    val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val gammelStillingConsumer = StillingConsumer(mockConsumer(), gammelStillingMottattService)

    return App(
        webServer,
        elasticSearchService,
        stillingConsumer,
        gammelStillingConsumer,
    )
}
