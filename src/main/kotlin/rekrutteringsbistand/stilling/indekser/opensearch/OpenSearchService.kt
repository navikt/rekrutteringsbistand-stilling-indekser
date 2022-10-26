package rekrutteringsbistand.stilling.indekser.opensearch

import rekrutteringsbistand.stilling.indekser.utils.log

const val stillingAlias: String = "stilling"

class OpenSearchService(private val osClient: OpenSearchClient) {

    fun initialiserIndeksering(indeks: String) {
        val indeksBleOpprettet = opprettIndeksHvisDenIkkeFinnes(indeks)
        if (indeksBleOpprettet) osClient.oppdaterAlias(indeks)
    }

    fun initialiserReindeksering(nyIndeks: String, gjeldendeIndeks: String) {
        if (!osClient.indeksFinnes(nyIndeks)) {
            osClient.opprettIndeks(nyIndeks)
            log.info("Starter reindeksering på ny indeks $nyIndeks")
        } else {
            log.info("Gjenopptar reindeksering på ny indeks $nyIndeks")
        }

        log.info("Fortsetter samtidig konsumering på gjeldende indeks $gjeldendeIndeks")
    }

    fun indekser(rekrutteringsbistandStillinger: List<RekrutteringsbistandStilling>, indeks: String) {
        osClient.indekser(rekrutteringsbistandStillinger, indeks)
    }

    private fun opprettIndeksHvisDenIkkeFinnes(indeksNavn: String): Boolean {
        if (!osClient.indeksFinnes(stillingAlias)) {
            osClient.opprettIndeks(indeksNavn)
            return true
        }
        log.info("Bruker eksisterende indeks '$indeksNavn'")
        return false
    }

    fun skalReindeksere(): Boolean {
        if (!osClient.indeksFinnes(stillingAlias)) return false
        val gjeldendeVersjon = hentGjeldendeIndeksversjon() ?: return false // indeks finnes ikke enda
        val nyIndeksVersjon = hentVersjonFraNaisConfig()
        return nyIndeksVersjon > gjeldendeVersjon
    }

    fun hentGjeldendeIndeksversjon(): Int? {
        val indeks = osClient.hentIndeksAliasPekerPå() ?: return null
        return hentVersjon(indeks)
    }

    fun byttTilNyIndeks() {
        val indeksnavn = hentNyesteIndeks()
        osClient.oppdaterAlias(indeksnavn)
    }

    fun hentGjeldendeIndeks(): String? {
        return osClient.hentIndeksAliasPekerPå()
    }
}
