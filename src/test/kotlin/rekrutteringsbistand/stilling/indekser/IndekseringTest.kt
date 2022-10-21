package rekrutteringsbistand.stilling.indekser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.ConnectionClosedException
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.behandling.assertEqualContactLists
import rekrutteringsbistand.stilling.indekser.behandling.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.opensearch.OpenSearchClient
import rekrutteringsbistand.stilling.indekser.opensearch.RekrutteringsbistandStilling
import rekrutteringsbistand.stilling.indekser.opensearch.hentIndeksNavn
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enStillingsinfo
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.setup.mottaKafkamelding
import rekrutteringsbistand.stilling.indekser.stillingsinfo.KunneIkkeHenteStillingsinsinfoException
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey
import rekrutteringsbistand.stilling.indekser.utils.Liveness
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class IndekseringTest {

    @Test
    fun `Skal indeksere stillinger i Open Search når vi får melding på Kafka-topic`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()

        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, osClient = osClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                    RekrutteringsbistandStilling(
                            stilling = konverterTilStilling(enAd),
                            stillingsinfo = enStillingsinfo
                    )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }


    @Test
    fun `Skal kun indeksere siste melding per stilling`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()

        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, osClient = osClientMock).use {
            val melding = enAd
            val sisteMelding = enAd

            sisteMelding.setTitle("oppdatertTittel")

            mottaKafkamelding(consumer, melding)
            mottaKafkamelding(consumer, sisteMelding)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(sisteMelding),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }

    @Test
    fun `Skal indeksere mot to ulike indekser i Open Search under reindeksering`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val gammelConsumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()

        val indeksAliasPekerPå = hentIndeksNavn(1)
        val nyIndeksversjon = 2

        Environment.set(indeksversjonKey, nyIndeksversjon.toString())

        every { osClientMock.hentIndeksAliasPekerPå() } returns indeksAliasPekerPå
        every { osClientMock.indeksFinnes(any()) } returns true
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, gammelConsumer, osClientMock).use {
            mottaKafkamelding(gammelConsumer, enAd)
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                    RekrutteringsbistandStilling(
                            stilling = konverterTilStilling(enAd),
                            stillingsinfo = enStillingsinfo
                    )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, indeksAliasPekerPå)
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(nyIndeksversjon))
            }
        }
    }

    @Test
    fun `Skal indeksere på ny hvis kall mot Open Search feiler`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val osClientMock = mockk<OpenSearchClient>()
        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit

        // Skal feile første gang og OK neste gang
        every { osClientMock.indekser(any(), any()) } throws ConnectionClosedException() andThen Unit

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(consumer, osClient = osClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(enAd),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(exactly = 2, timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }

    @Test
    fun `Skal indeksere selv om kall mot rekrutteringsbistand-stilling-api feiler én gang`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val osClientMock = mockk<OpenSearchClient>()
        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        val stillingsinfoClientMock = mockk<StillingsinfoClient>()
        // Feiler første gang, OK neste gang
        every {
            stillingsinfoClientMock.getStillingsinfo(any())
        } throws KunneIkkeHenteStillingsinsinfoException("") andThen listOf(enStillingsinfo)

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(consumer, osClient = osClientMock, stillingsinfoClient = stillingsinfoClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(enAd),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }
        }
    }

    @Test
    fun `Appen skal stoppes hvis indeksering feiler to ganger`() {
        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        val osClientMock = mockk<OpenSearchClient>()
        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every {
            osClientMock.indekser(any(), any())
        } throws ConnectionClosedException() andThenThrows  ConnectionClosedException()

        val consumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(osClient = osClientMock).use {
            mottaKafkamelding(consumer, enAd)

            Thread.sleep(500)
            assertFalse(Liveness.isAlive)
        }
    }

    @Test
    fun `Kontaktinformasjon på Kafka-melding skal indekseres`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()

        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, osClient = osClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(enAd),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }

            assertEqualContactLists(enAd.getContacts(), forventedeStillinger.first().stilling.contacts)
        }
    }

    @Test
    fun `Stillingskategori på Kafka-melding skal indekseres`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()

        val indeksversjon = 1
        Environment.set(indeksversjonKey, indeksversjon.toString())

        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        startLokalApp(consumer, osClient = osClientMock).use {
            mottaKafkamelding(consumer, enAd)

            val forventedeStillinger = listOf(
                RekrutteringsbistandStilling(
                    stilling = konverterTilStilling(enAd),
                    stillingsinfo = enStillingsinfo
                )
            )

            verify(timeout = 3000) {
                osClientMock.indekser(forventedeStillinger, hentIndeksNavn(indeksversjon))
            }

            assertEquals("STILLING", forventedeStillinger.first().stillingsinfo?.stillingskategori)
        }
    }
}
