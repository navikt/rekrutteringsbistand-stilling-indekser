package rekrutteringsbistand.stilling.indekser

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.kafka.mockConsumer

class IndekseringTest {

    @Test
    fun `Skal legge stilling i ES når vi får melding på Kafka`() {
//        val elasticSearchServiceMock = mockk<RestHighLevelClient>()
//        val webServer = Javalin.create()

//        val esClientMock = mockk<EsClientMiddleware>()

//        every { esClientMock.indices().exists(any(), any()) } returns true
//        every { esClientMock.indices().getAlias(any(), any()) } returns "enindeks"


//        startLokalApp(esClient = esClientMock)

        // "send" melding på kafka

        // verifiser at stilling ligger i ES

//        verify { elasticSearchServiceMock.indekser(any(), any()) }
//        webServer.stop()
    }

    @Test
    fun `motta melding send i ES`() {
//        val consumer = StillingConsumer(mockConsumer(), )
    }
}
