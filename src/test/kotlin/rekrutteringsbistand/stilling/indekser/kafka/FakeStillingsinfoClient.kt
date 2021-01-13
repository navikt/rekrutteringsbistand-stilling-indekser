package rekrutteringsbistand.stilling.indekser.kafka

import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class FakeStillingsinfoClient: StillingsinfoClient {

    override fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        return enStillingsinfo
    }
}
