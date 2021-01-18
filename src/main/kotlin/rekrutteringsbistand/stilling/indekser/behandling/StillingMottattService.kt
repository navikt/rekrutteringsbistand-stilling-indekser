package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.ad.ext.avro.Ad
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val esService: ElasticSearchService
) {

    fun behandleStillinger(ads: List<Ad>, indeksNavn: String) {
        val rekrutteringsbistandStillinger = ads.map {
            val stilling = konverterTilStilling(it)
            val stillingsinfo = stillingsinfoClient.getStillingsinfo(it.getUuid())
            RekrutteringsbistandStilling(stilling, stillingsinfo)
        }

        esService.indekser(rekrutteringsbistandStillinger, indeksNavn)
    }
}

