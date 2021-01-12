package rekrutteringsbistand.stilling.indekser.kafka

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override fun start() {
        stillingMottattService.behandleStilling(enAd)
    }

    override fun konsumerTopicFraBegynnelse() {}
}
