package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {
    private var skalKjøre = true

    override suspend fun start(indeksNavn: String) = withContext(Dispatchers.IO) {
        while (skalKjøre) {
            stillingMottattService.behandleStillinger(listOf(enAd), indeksNavn)
            delay(10_000)
        }
    }

    override fun stopp() {
        log.info("Stopper Kafka-consumer ...")
        skalKjøre = false
    }
}
