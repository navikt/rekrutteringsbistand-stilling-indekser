package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.log

class StillingMottattService {
    fun behandleStilling(stilling: StillingDto) {
        log.info("Mottok stilling ${stilling.id}")
    }
}
