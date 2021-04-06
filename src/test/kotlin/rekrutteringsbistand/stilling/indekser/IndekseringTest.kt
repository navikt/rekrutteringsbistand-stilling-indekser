package rekrutteringsbistand.stilling.indekser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.ConnectionClosedException
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.behandling.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.RekrutteringsbistandStilling
import rekrutteringsbistand.stilling.indekser.elasticsearch.hentIndeksNavn
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enStillingsinfo
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.setup.mottaKafkamelding
import rekrutteringsbistand.stilling.indekser.stillingsinfo.KunneIkkeHenteStillingsinsinfoException
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey
import rekrutteringsbistand.stilling.indekser.utils.Liveness
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `Skal indeksere på ny hvis kall mot Elastic Search feiler`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val esClientMock = mockk<ElasticSearchClient>()
        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit

        // Skal feile første gang og OK neste gang
        every { esClientMock.indekser(any(), any()) } throws ConnectionClosedException() andThen Unit

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(consumer, esClient = esClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(enAd),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(exactly = 2, timeout = 3000) {
                esClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }

    @Test
    fun `Skal indeksere selv om kall mot rekrutteringsbistand-stilling-api feiler én gang`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val esClientMock = mockk<ElasticSearchClient>()
        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every { esClientMock.indekser(any(), any()) } returns Unit

        val stillingsinfoClientMock = mockk<StillingsinfoClient>()
        // Feiler første gang, OK neste gang
        every {
            stillingsinfoClientMock.getStillingsinfo(any())
        } throws KunneIkkeHenteStillingsinsinfoException("") andThen listOf(enStillingsinfo)

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(consumer, esClient = esClientMock, stillingsinfoClient = stillingsinfoClientMock).use {
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
    fun `Appen skal stoppes hvis indeksering feiler to ganger`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val esClientMock = mockk<ElasticSearchClient>()
        every { esClientMock.indeksFinnes(any()) } returns false
        every { esClientMock.opprettIndeks(any()) } returns Unit
        every { esClientMock.oppdaterAlias(any()) } returns Unit
        every {
            esClientMock.indekser(any(), any())
        } throws ConnectionClosedException() andThenThrows  ConnectionClosedException()

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(esClient = esClientMock).use {
            mottaKafkamelding(consumer, enAd)

            Thread.sleep(500)
            assertFalse(Liveness.isAlive)
        }
    }
}
