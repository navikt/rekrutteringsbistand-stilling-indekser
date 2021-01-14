package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override fun start() {
        stillingMottattService.behandleStilling(enAd)
    }

    override fun konsumerTopicFraBegynnelse() {
    }
}
