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
import rekrutteringsbistand.stilling.indekser.behandling.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.RekrutteringsbistandStilling
import rekrutteringsbistand.stilling.indekser.kafka.stillingEksternTopic
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enStillingsinfo
import rekrutteringsbistand.stilling.indekser.setup.getLocalRestHighLevelClient
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.utils.log

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
            app.start {
                val forventetArgument = listOf(RekrutteringsbistandStilling(
                        stilling = konverterTilStilling(enAd),
                        stillingsinfo = enStillingsinfo
                ))

                verify { esClientMock.indekser(forventetArgument, "stilling_1") }
                app.stop()
            }
        }

        consumer.schedulePollTask {
            consumer.addRecord(ConsumerRecord(stillingEksternTopic, 0, 0, enAd.getUuid(), enAd))
        }
    }

    @Test
    fun `motta melding send i ES`() {
//        val consumer = StillingConsumer(mockConsumer(), )
    }
}
