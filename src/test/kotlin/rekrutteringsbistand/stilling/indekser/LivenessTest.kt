package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.Fuel
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.opensearch.OpenSearchClient
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.mockConsumer
import rekrutteringsbistand.stilling.indekser.setup.mottaKafkamelding
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.Environment
import kotlin.test.assertEquals

class LivenessTest {

    @Test
    fun `Liveness-endepunkt skal returnere HTTP 500 hvis StillingConsumer stopper ved ukjent feil`() {
        val consumer = mockConsumer(periodiskSendMeldinger = false)
        val osClientMock = mockk<OpenSearchClient>()
        val stillingsinfoClient = mockk<StillingsinfoClient>()

        Environment.set(Environment.indeksversjonKey, "1")

        every { osClientMock.indeksFinnes(any()) } returns false
        every { osClientMock.opprettIndeks(any()) } returns Unit
        every { osClientMock.oppdaterAlias(any()) } returns Unit
        every { osClientMock.indekser(any(), any()) } returns Unit

        every { stillingsinfoClient.getStillingsinfo(any()) } throws Exception("Fail")

        startLokalApp(mockConsumer = consumer, osClient = osClientMock, stillingsinfoClient = stillingsinfoClient).use {
            mottaKafkamelding(consumer, enAd, 0)
            Thread.sleep(500)

            val (_, response, _) = Fuel.get("http://localhost:8222/internal/isAlive").response()
            assertEquals(500, response.statusCode)
        }
    }
}
