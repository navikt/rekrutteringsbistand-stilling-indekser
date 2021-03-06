package rekrutteringsbistand.stilling.indekser

import no.nav.pam.stilling.ext.avro.Ad
import org.apache.kafka.clients.consumer.Consumer
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchService
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.setup.FakeStillingsinfoClient
import rekrutteringsbistand.stilling.indekser.setup.getLocalRestHighLevelClient
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey

fun main() {
    Environment.set("REKRUTTERINGSBISTAND_STILLING_API_URL", "http://localhost:9501")
    Environment.set("ELASTIC_SEARCH_API", "http://localhost:9200")
    Environment.set(indeksversjonKey, "1")

    startLokalApp()
}

fun startLokalApp(
        mockConsumer: Consumer<String, Ad> = mockConsumer(periodiskSendMeldinger = true),
        gammelMockConsumer: Consumer<String, Ad> = mockConsumer(periodiskSendMeldinger = true),
        esClient: ElasticSearchClient = ElasticSearchClient(getLocalRestHighLevelClient()),
        stillingsinfoClient: StillingsinfoClient = FakeStillingsinfoClient()
): App {

    val elasticSearchService = ElasticSearchService(esClient)
    val stillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val stillingConsumer = StillingConsumer(mockConsumer, stillingMottattService)

    val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, elasticSearchService)
    val gammelStillingConsumer = StillingConsumer(gammelMockConsumer, gammelStillingMottattService)

    val app = App(
        elasticSearchService,
        stillingConsumer,
        gammelStillingConsumer
    )

    app.start()

    return app
}
