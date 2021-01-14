package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.ad.ext.avro.Ad
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.kafka.konverterTilStilling
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val esService: ElasticSearchService,
    private val indeksNavn: String = stillingAlias
) {

    fun behandleStilling(ad: Ad) {
        val stilling = konverterTilStilling(ad)
        val stillingsinfo = stillingsinfoClient.getStillingsinfo(stilling.uuid)
        val rekrutteringsbistandStilling = RekrutteringsbistandStilling(stilling, stillingsinfo)
        esService.indekser(rekrutteringsbistandStilling, indeksNavn)
    }
}

