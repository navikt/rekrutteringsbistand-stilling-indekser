package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override fun start(indeksNavn: String) {
        stillingMottattService.behandleStilling(enAd, indeksNavn)
    }
}
