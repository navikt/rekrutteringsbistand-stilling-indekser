package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.Fuel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.elasticsearch.ElasticSearchClient
import rekrutteringsbistand.stilling.indekser.elasticsearch.hentIndeksNavn
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.utils.Environment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ByttIndeksTest {

    companion object {
        private val esClient = mockk<ElasticSearchClient>()
        private val indeksAliasPekerPå = hentIndeksNavn(1)
        const val nyIndeksversjon = 2

        init {
            Environment.set(Environment.indeksversjonKey, nyIndeksversjon.toString())
            every { esClient.hentIndeksAliasPekerPå() } returns indeksAliasPekerPå
            every { esClient.indeksFinnes(any()) } returns true
            every { esClient.opprettIndeks(any()) } returns Unit
            every { esClient.indekser(any(), any()) } returns Unit
            every { esClient.oppdaterAlias(any()) } returns Unit
        }
    }

    @Test
    fun `Kall mot byttIndeks skal bytte indeks alias peker på`() {
        val gammelMockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(gammelMockConsumer = gammelMockConsumer, esClient = esClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)

            val forventaIndeksNavn = hentIndeksNavn(nyIndeksversjon)
            verify { esClient.oppdaterAlias(forventaIndeksNavn) }
        }
    }

    @Test
    fun `Kall mot byttIndeks skal stoppe gammel StilligConsumer`() {
        val gammelMockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(gammelMockConsumer = gammelMockConsumer, esClient = esClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)
            assertTrue(gammelMockConsumer.closed())
        }
    }

    @Test
    fun `Ny StillingConsumer skal fortsette konsumering etter kall mot byttIndeks`() {
        val mockConsumer = mockConsumer(periodiskSendMeldinger = false)

        startLokalApp(mockConsumer = mockConsumer, esClient = esClient).use {
            val (_, response, _) = Fuel.get("http://localhost:8222/internal/byttIndeks").response()
            assertEquals(200, response.statusCode)
            assertFalse(mockConsumer.closed())
        }
    }
}
