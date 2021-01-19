package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.ad.ext.avro.Ad
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val esService: ElasticSearchService
) {

    fun behandleStillinger(ads: List<Ad>, indeksNavn: String) {
        val stillinger = ads.map { konverterTilStilling(it) }
        val stillingsinfo = stillingsinfoClient.getStillingsinfo(stillinger.map { it.uuid })

        val rekrutteringsbistandStillinger = stillinger.map { stilling ->
            RekrutteringsbistandStilling(stilling, stillingsinfo.find { info -> info.stillingsid == stilling.uuid })
        }

        esService.indekser(rekrutteringsbistandStillinger, indeksNavn)
    }
}
