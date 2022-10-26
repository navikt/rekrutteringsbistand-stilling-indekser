package rekrutteringsbistand.stilling.indekser

import no.nav.pam.stilling.ext.avro.Ad
import org.apache.kafka.clients.consumer.Consumer
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.opensearch.OpenSearchClient
import rekrutteringsbistand.stilling.indekser.opensearch.OpenSearchService
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.setup.FakeStillingsinfoClient
import rekrutteringsbistand.stilling.indekser.setup.getLocalRestHighLevelClient
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey

fun main() {
    Environment.set("REKRUTTERINGSBISTAND_STILLING_API_URL", "http://localhost:9501")
    Environment.set("OPEN_SEARCH_API", "http://localhost:9200")
    Environment.set(indeksversjonKey, "1")

    startLokalApp()
}

fun startLokalApp(
    mockConsumer: Consumer<String, Ad> = mockConsumer(periodiskSendMeldinger = true),
    gammelMockConsumer: Consumer<String, Ad> = mockConsumer(periodiskSendMeldinger = true),
    osClient: OpenSearchClient = OpenSearchClient(getLocalRestHighLevelClient()),
    stillingsinfoClient: StillingsinfoClient = FakeStillingsinfoClient()
): App {

    val openSearchService = OpenSearchService(osClient)
    val stillingMottattService = StillingMottattService(stillingsinfoClient, openSearchService)
    val stillingConsumer = StillingConsumer(mockConsumer, stillingMottattService)

    val gammelStillingMottattService = StillingMottattService(stillingsinfoClient, openSearchService)
    val gammelStillingConsumer = StillingConsumer(gammelMockConsumer, gammelStillingMottattService)

    val app = App(
        openSearchService,
        stillingConsumer,
        gammelStillingConsumer
    )

    app.start()

    return app
}
