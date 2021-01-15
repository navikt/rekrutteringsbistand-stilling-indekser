package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override fun start(indeksNavn: String) {
        stillingMottattService.behandleStilling(enAd, indeksNavn)
    }

    override fun stopp() {
        log.info("Stopper Kafka-consumer ...")
    }
}
