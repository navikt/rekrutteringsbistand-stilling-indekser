package rekrutteringsbistand.stilling.indekser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.behandling.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.RekrutteringsbistandStilling
import rekrutteringsbistand.stilling.indekser.kafka.stillingEksternTopic
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enStillingsinfo
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer

class IndekseringTest {

    @Test
    fun `Skal indeksere stillinger i Elastic Search når vi får melding på Kafka-topic`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, esClientMock)
        sendKafkamelding(consumer, enAd, offset = 0)

        val listeMedStillinger = listOf(RekrutteringsbistandStilling(
                stilling = konverterTilStilling(enAd),
                stillingsinfo = enStillingsinfo
        ))

        verify(timeout = 3000) {
            esClientMock.indekser(eq(listeMedStillinger), any())
        }
    }

    private fun sendKafkamelding(consumer: MockConsumer<String, Ad>, ad: Ad, offset: Long) {
        val melding = ConsumerRecord(stillingEksternTopic, 0, offset, ad.getUuid(), ad)
        consumer.schedulePollTask {
            consumer.addRecord(melding)
        }
    }
}
