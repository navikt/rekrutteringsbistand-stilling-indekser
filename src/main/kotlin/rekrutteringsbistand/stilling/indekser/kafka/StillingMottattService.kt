package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.utils.log

class StillingMottattService {
    fun behandleStilling(stilling: StillingDto) {
        log.info("Mottok stilling med id: ${stilling.id}")
    }
}
