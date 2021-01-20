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
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.log

class IndekseringTest {

    @Test
    fun `Skal indeksere stillinger i Elastic Search når vi får melding på Kafka-topic`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, esClient = esClientMock)
        mottaKafkamelding(consumer, enAd, offset = 0)

        val listeMedStillinger = listOf(RekrutteringsbistandStilling(
                stilling = konverterTilStilling(enAd),
                stillingsinfo = enStillingsinfo
        ))

        verify(timeout = 3000) {
            esClientMock.indekser(eq(listeMedStillinger), any())
        }
    }

    @Test
    fun `Skal indeksere mot to ulike indekser i Elastic Search under reindeksering`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val gammelConsumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        Environment.set("NAIS_VERSJON", "2");

        every { esClientMock.hentIndeksAliasPekerPå() } returns "1"
        every { esClientMock.indeksFinnes(any()) } returns true
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, gammelConsumer, esClientMock)

        mottaKafkamelding(gammelConsumer, enAd, offset = 0)
        verify(timeout = 3000) {
            esClientMock.indekser(any(), any())
        }

        mottaKafkamelding(consumer, enAd, offset = 0)
        verify(timeout = 3000) {
            esClientMock.indekser(any(), any())
        }
    }

    private fun mottaKafkamelding(consumer: MockConsumer<String, Ad>, ad: Ad, offset: Long) {
        val melding = ConsumerRecord(stillingEksternTopic, 0, offset, ad.getUuid(), ad)
        consumer.schedulePollTask {
            consumer.addRecord(melding)
        }
    }
}
