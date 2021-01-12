package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.client.indices.PutMappingRequest
import org.elasticsearch.common.xcontent.XContentType
import rekrutteringsbistand.stilling.indekser.utils.log
import rekrutteringsbistand.stilling.indekser.utils.objectMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val stillingAlias: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun initialiser() {
        val indeksNavn = indeksNavnMedTimestamp()
        val indeksBleOpprettet = opprettIndeksHvisDenIkkeFinnes(indeksNavn)
        if (indeksBleOpprettet) oppdaterAlias(indeksNavn)
    }

    fun indekser(stilling: Stilling, counter: Int) {
        val indexRequest = IndexRequest(stillingAlias)
            .id(stilling.uuid)
            .source(objectMapper.writeValueAsString(stilling), XContentType.JSON)
        esClient.index(indexRequest, RequestOptions.DEFAULT)
        log.info("Indekserte stilling #$counter med UUID: ${stilling.uuid}")
    }

    private fun opprettIndeksHvisDenIkkeFinnes(indeksNavn: String): Boolean {
        val getIndexRequest = GetIndexRequest(stillingAlias)
        val indeksFinnes = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)

        if (!indeksFinnes) {
            val request = CreateIndexRequest(indeksNavn).source(INTERNALAD_COMMON_SETTINGS, XContentType.JSON)
            esClient.indices().create(request, RequestOptions.DEFAULT)

            val putMappingRequest = PutMappingRequest(indeksNavn)
                    .source(INTERNALAD_MAPPING, XContentType.JSON)
            esClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT)

            log.info("Opprettet indeks '$indeksNavn'")
            return true
        }
        return false
    }

    private fun oppdaterAlias(indeksNavn: String) {
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

    companion object {
        private val INTERNALAD_COMMON_SETTINGS = ElasticSearchService::class.java
                .getResource("/stilling-common.json").readText()
        private val INTERNALAD_MAPPING = ElasticSearchService::class.java
                .getResource("/stilling-mapping.json").readText()
    }
}

fun indeksNavnMedTimestamp(): String {
    val dateTimeFormat = DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss")
    return stillingAlias + LocalDateTime.now().format(dateTimeFormat)
}
