package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.client.indices.PutMappingRequest
import org.elasticsearch.common.xcontent.XContentType
import rekrutteringsbistand.stilling.indekser.utils.log

const val stillingAlias: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun indekser(rekrutteringsbistandStillinger: List<RekrutteringsbistandStilling>, indeks: String) {

        val bulkRequest = BulkRequest()
        rekrutteringsbistandStillinger.forEach {
            bulkRequest.add(
                IndexRequest(indeks)
                    .id(it.stilling.uuid)
                    .source(jacksonObjectMapper().writeValueAsString(it), XContentType.JSON)
            )
        }

        esClient.bulk(bulkRequest, RequestOptions.DEFAULT)

        val uuider = rekrutteringsbistandStillinger.map { it.stilling.uuid }
        log.info("Indekserte ${uuider.size} stillinger i indeks '$indeks'. UUIDer: $uuider")
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
        val gjeldendeVersjon = hentGjeldendeIndeksversjon() ?: return false // indeks finnes ikke enda
        return nyIndeksVersjon > gjeldendeVersjon
    }

    fun nyIndeksErIkkeOpprettet(): Boolean {
        val indeksnavn = hentNyesteIndeks()
        val request = GetIndexRequest(indeksnavn)
        return !esClient.indices().exists(request, RequestOptions.DEFAULT)
    }

    private fun ingenIndeksFinnes(): Boolean {
        val getIndexRequest = GetIndexRequest(stillingAlias)
        return !esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)
    }

    fun hentGjeldendeIndeks(): String? {
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

    fun hentGjeldendeIndeksversjon(): Int? {
        val indeks = hentGjeldendeIndeks() ?: return null
        return hentVersjon(indeks)
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

