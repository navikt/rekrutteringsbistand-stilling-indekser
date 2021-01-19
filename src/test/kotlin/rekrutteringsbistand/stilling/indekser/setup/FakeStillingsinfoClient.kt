package rekrutteringsbistand.stilling.indekser.setup

import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class FakeStillingsinfoClient: StillingsinfoClient {

    override fun getStillingsinfo(stillingsId: String): Stillingsinfo? {
        return enStillingsinfo
    }

    override fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo> {
        return listOf(enStillingsinfo)
    }
}
