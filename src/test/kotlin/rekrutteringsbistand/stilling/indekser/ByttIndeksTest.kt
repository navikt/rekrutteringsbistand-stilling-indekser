package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.Fuel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.opensearch.OpenSearchClient
import rekrutteringsbistand.stilling.indekser.opensearch.hentIndeksNavn
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.utils.Environment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ByttIndeksTest {

    companion object {
        private val osClient = mockk<OpenSearchClient>()
        private val indeksAliasPekerPå = hentIndeksNavn(1)
        const val nyIndeksversjon = 2

        init {
            Environment.set(Environment.indeksversjonKey, nyIndeksversjon.toString())
            every { osClient.hentIndeksAliasPekerPå() } returns indeksAliasPekerPå
            every { osClient.indeksFinnes(any()) } returns true
            every { osClient.opprettIndeks(any()) } returns Unit
            every { osClient.indekser(any(), any()) } returns Unit
            every { osClient.oppdaterAlias(any()) } returns Unit
        }
    }

    @Test
    fun `Kall mot byttIndeks skal bytte indeks alias peker på`() {
        val gammelMockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(gammelMockConsumer = gammelMockConsumer, osClient = osClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)

            val forventaIndeksNavn = hentIndeksNavn(nyIndeksversjon)
            verify { osClient.oppdaterAlias(forventaIndeksNavn) }
        }
    }

    @Test
    fun `Kall mot byttIndeks skal stoppe gammel StilligConsumer`() {
        val gammelMockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(gammelMockConsumer = gammelMockConsumer, osClient = osClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)
            Thread.sleep(500)
            assertTrue(gammelMockConsumer.closed())
        }
    }

    @Test
    fun `Ny StillingConsumer skal fortsette konsumering etter kall mot byttIndeks`() {
        val mockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(mockConsumer = mockConsumer, osClient = osClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)
            assertFalse(mockConsumer.closed())
        }
    }
}
