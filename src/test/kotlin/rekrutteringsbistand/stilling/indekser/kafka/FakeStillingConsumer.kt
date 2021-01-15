package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override suspend fun start(indeksNavn: String) = withContext(Dispatchers.IO) {
        while (true) {
            stillingMottattService.behandleStilling(enAd, indeksNavn)
            delay(10_000)
        }
    }

    override fun stopp() {
        log.info("Stopper Kafka-consumer ...")
    }
}
