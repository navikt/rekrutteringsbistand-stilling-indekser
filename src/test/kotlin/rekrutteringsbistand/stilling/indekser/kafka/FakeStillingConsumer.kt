package rekrutteringsbistand.stilling.indekser.kafka

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {
    override fun start() {
        val melding = StillingDto(id = "1234")
        stillingMottattService.behandleStilling(melding)
    }
}
