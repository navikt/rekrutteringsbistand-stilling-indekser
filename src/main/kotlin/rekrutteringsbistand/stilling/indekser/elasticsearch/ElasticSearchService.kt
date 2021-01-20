package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.utils.log

const val stillingAlias: String = "stilling"

class ElasticSearchService(private val esClient: ElasticSearchClient) {

    fun initialiserIndeksering(indeks: String) {
        val indeksBleOpprettet = opprettIndeksHvisDenIkkeFinnes(indeks)
        if (indeksBleOpprettet) esClient.oppdaterAlias(indeks)
    }

    fun initialiserReindeksering(nyIndeks: String, gjeldendeIndeks: String) {
        if (!esClient.indeksFinnes(nyIndeks)) {
            esClient.opprettIndeks(nyIndeks)
            log.info("Starter reindeksering på ny indeks $nyIndeks")
        } else {
            log.info("Gjenopptar reindeksering på ny indeks $nyIndeks")
        }

        log.info("Fortsetter samtidig konsumering på gjeldende indeks $gjeldendeIndeks")
    }

    fun indekser(rekrutteringsbistandStillinger: List<RekrutteringsbistandStilling>, indeks: String) {
        esClient.indekser(rekrutteringsbistandStillinger, indeks)
    }

    private fun opprettIndeksHvisDenIkkeFinnes(indeksNavn: String): Boolean {
        if (!esClient.indeksFinnes(stillingAlias)) {
            esClient.opprettIndeks(indeksNavn)
            return true
        }
        log.info("Bruker eksisterende indeks '$indeksNavn'")
        return false
    }

    fun skalReindeksere(): Boolean {
        if (!esClient.indeksFinnes(stillingAlias)) return false
        val gjeldendeVersjon = hentGjeldendeIndeksversjon() ?: return false // indeks finnes ikke enda
        val nyIndeksVersjon = hentVersjonFraNaisConfig()
        return nyIndeksVersjon > gjeldendeVersjon
    }

    fun hentGjeldendeIndeksversjon(): Int? {
        val indeks = esClient.hentIndeksAliasPekerPå() ?: return null
        return hentVersjon(indeks)
    }

    fun byttTilNyIndeks() {
        val indeksnavn = hentNyesteIndeks()
        esClient.oppdaterAlias(indeksnavn)
    }

    fun hentGjeldendeIndeks(): String? {
        return esClient.hentIndeksAliasPekerPå()
    }
}
