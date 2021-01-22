package rekrutteringsbistand.stilling.indekser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.behandling.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.RekrutteringsbistandStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.hentIndeksNavn
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enStillingsinfo
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.setup.mottaKafkamelding
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey

class IndekseringTest {

    @Test
    fun `Skal indeksere stillinger i Elastic Search når vi får melding på Kafka-topic`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, esClient = esClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                    RekrutteringsbistandStilling(
                            stilling = konverterTilStilling(enAd),
                            stillingsinfo = enStillingsinfo
                    )
            )

            verify(timeout = 3000) {
                esClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }

    @Test
    fun `Skal indeksere mot to ulike indekser i Elastic Search under reindeksering`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val gammelConsumer = mockConsumer(periodiskSendMeldinger = false)
        val esClientMock = mockk<ElasticSearchClient>()

        val indeksAliasPekerPå = hentIndeksNavn(1)
        val nyIndeksversjon = 2

        Environment.set(indeksversjonKey, nyIndeksversjon.toString())

        every { esClientMock.hentIndeksAliasPekerPå() } returns indeksAliasPekerPå
        every { esClientMock.indeksFinnes(any()) } returns true
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, gammelConsumer, esClientMock).use {
            mottaKafkamelding(gammelConsumer, enAd)
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                    RekrutteringsbistandStilling(
                            stilling = konverterTilStilling(enAd),
                            stillingsinfo = enStillingsinfo
                    )
            )

            verify(timeout = 3000) {
                esClientMock.indekser(forventedeStillinger, indeksAliasPekerPå)
                esClientMock.indekser(forventedeStillinger, hentIndeksNavn(nyIndeksversjon))
            }
        }
    }
}
