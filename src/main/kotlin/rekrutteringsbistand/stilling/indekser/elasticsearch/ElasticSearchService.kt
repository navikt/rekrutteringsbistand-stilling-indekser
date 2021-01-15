package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.client.indices.PutMappingRequest
import org.elasticsearch.common.xcontent.XContentType
import rekrutteringsbistand.stilling.indekser.utils.environment
import rekrutteringsbistand.stilling.indekser.utils.log
import rekrutteringsbistand.stilling.indekser.utils.objectMapper

const val stillingAlias: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun indekser(rekrutteringsbistandStilling: RekrutteringsbistandStilling, indeks: String) {
        val indexRequest = IndexRequest(indeks)
            .id(rekrutteringsbistandStilling.stilling.uuid)
            .source(objectMapper.writeValueAsString(rekrutteringsbistandStilling), XContentType.JSON)
        esClient.index(indexRequest, RequestOptions.DEFAULT)
        log.info("Indekserte stilling med UUID: ${rekrutteringsbistandStilling.stilling.uuid} i indeks '$indeks'")
    }

    fun opprettIndeksHvisDenIkkeFinnes(indeksNavn: String): Boolean {
        if (ingenIndeksFinnes()) {
            opprettIndeks(indeksNavn)
            return true
        }
        log.info("Bruker eksisterende indeks '$indeksNavn'")
        return false
    }

    fun opprettIndeks(indeksNavn: String) {
        val request = CreateIndexRequest(indeksNavn).source(INTERNALAD_COMMON_SETTINGS, XContentType.JSON)
        esClient.indices().create(request, RequestOptions.DEFAULT)

        val stillingMappingRequest = PutMappingRequest(indeksNavn)
            .source(INTERNALAD_MAPPING, XContentType.JSON)
        esClient.indices().putMapping(stillingMappingRequest, RequestOptions.DEFAULT)

        log.info("Opprettet indeks '$indeksNavn'")
    }

    fun oppdaterAlias(indeksNavn: String) {
        val remove = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
            .index("$stillingAlias*")
            .alias(stillingAlias)
        val add = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
            .index(indeksNavn)
            .alias(stillingAlias)
        val request = IndicesAliasesRequest()
            .addAliasAction(remove)
            .addAliasAction(add)
        esClient.indices().updateAliases(request, RequestOptions.DEFAULT)
        log.info("Oppdaterte alias '$stillingAlias' til å peke på '$indeksNavn'")
    }

    fun skalReindeksere(): Boolean {
        if (ingenIndeksFinnes()) return false

        val nyIndeksVersjon = hentVersjonFraNaisConfig()
        val gjeldendeVersjon = hentVersjonAliasPekerPå() ?: return false // indeks finnes ikke enda
        return nyIndeksVersjon > gjeldendeVersjon
    }

    fun nyIndeksErIkkeOpprettet(): Boolean {
        val nyIndeksVersjon = hentVersjonFraNaisConfig()
        val request = GetIndexRequest(hentIndeksNavn(nyIndeksVersjon))
        return !esClient.indices().exists(request, RequestOptions.DEFAULT)
    }

    private fun ingenIndeksFinnes(): Boolean {
        val getIndexRequest = GetIndexRequest(stillingAlias)
        return !esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)
    }

    fun hentVersjonAliasPekerPå(): Int? {
        val indeks = hentIndeksAliasPekerPå() ?: return null
        return hentVersjon(indeks)
    }

    fun hentIndeksAliasPekerPå(): String? {
        val request = GetAliasesRequest(stillingAlias).indices("$stillingAlias*")
        val response = esClient.indices().getAlias(request, RequestOptions.DEFAULT)

        return when (response.aliases.size) {
            0 -> null
            1 -> response.aliases.keys.first()
            else -> throw Exception(
                "Klarte ikke hente indeks for alias, fikk mer enn én indeks. " +
                        "Antall indekser: ${response.aliases.size}"
            )
        }
    }

    private fun hentVersjon(indeksNavn: String): Int {
        return indeksNavn.split("_").last().toInt()
    }

    fun hentVersjonFraNaisConfig(): Int {
        return environment().get("INDEKS_VERSJON").toInt()
    }

    fun hentIndeksNavn(versjon: Int): String {
        return "${stillingAlias}_$versjon"
    }

    fun byttTilNyIndeks() {
        oppdaterAlias(hentIndeksNavn(hentVersjonFraNaisConfig()))
    }

    companion object {
        private val INTERNALAD_COMMON_SETTINGS = ElasticSearchService::class.java
                .getResource("/stilling-common.json").readText()
        private val INTERNALAD_MAPPING = ElasticSearchService::class.java
                .getResource("/stilling-mapping.json").readText()
    }
}

