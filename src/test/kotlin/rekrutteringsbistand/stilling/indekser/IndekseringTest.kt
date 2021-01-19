package rekrutteringsbistand.stilling.indekser

import io.javalin.Javalin
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.kafka.stillingEksternTopic
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.getLocalRestHighLevelClient
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer

class IndekseringTest {

    @Test
    fun `Skal indeksere stillinger i Elastic Search når vi får melding på Kafka-topic`(): Unit = runBlocking {
        // Start applikasjon
        val webServer = Javalin.create()
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        val app = lagLokalApp(webServer, consumer, esClientMock)
        launch {
            app.start()
        }

        // Send melding på Kafka
        consumer.schedulePollTask {
            consumer.addRecord(ConsumerRecord(stillingEksternTopic, 0, 0, enAd.getUuid(), enAd))
        }

        // TODO: Blæh
        delay(1_000)

        // Verify at indekser()-metode er kjørt med riktige parametre
        verify { esClientMock.indekser(any(), any()) }

        app.stop()
    }

    @Test
    fun `motta melding send i ES`() {
//        val consumer = StillingConsumer(mockConsumer(), )
    }
}
