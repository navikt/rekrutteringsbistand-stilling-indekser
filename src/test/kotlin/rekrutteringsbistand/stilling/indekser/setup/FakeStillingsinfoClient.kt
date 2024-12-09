package rekrutteringsbistand.stilling.indekser.setup

import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class FakeStillingsinfoClient: StillingsinfoClient {

    override fun getStillingsinfo(stillingsIder: List<String>): List<Stillingsinfo> {
        return listOf(enStillingsinfo)
    }

    override fun sendStillingsId(stillingsid: String) {
        // trenger ikke gjøre noe
    }
}
