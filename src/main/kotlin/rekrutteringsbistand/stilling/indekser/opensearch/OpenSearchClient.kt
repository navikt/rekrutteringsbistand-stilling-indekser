package rekrutteringsbistand.stilling.indekser.opensearch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.client.indices.PutMappingRequest
import org.opensearch.common.xcontent.XContentType
import rekrutteringsbistand.stilling.indekser.utils.log

class OpenSearchClient(private val restHighLevelClient: RestHighLevelClient) {

    fun indekser(rekrutteringsbistandStillinger: List<RekrutteringsbistandStilling>, indeks: String) {
        val bulkRequest = BulkRequest()
        rekrutteringsbistandStillinger.forEach {
            bulkRequest.add(
                IndexRequest(indeks)
                    .id(it.stilling.uuid)
                    .source(jacksonObjectMapper().writeValueAsString(it), XContentType.JSON)
            )
        }

        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT)

        val uuider = rekrutteringsbistandStillinger.map { it.stilling.uuid }
        log.info("Indekserte ${uuider.size} stillinger i indeks '$indeks'. UUIDer: $uuider")
    }

    fun opprettIndeks(indeksNavn: String) {
        val request = CreateIndexRequest(indeksNavn).source(osSettings, XContentType.JSON)
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT)

        val stillingMappingRequest = PutMappingRequest(indeksNavn)
            .source(stillingMapping, XContentType.JSON)
        restHighLevelClient.indices().putMapping(stillingMappingRequest, RequestOptions.DEFAULT)

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
        restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT)
        log.info("Oppdaterte alias '$stillingAlias' til å peke på '$indeksNavn'")
    }

    fun indeksFinnes(indeksnavn: String): Boolean {
        val request = GetIndexRequest(indeksnavn)
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT)
    }

    fun hentIndeksAliasPekerPå(): String? {
        val request = GetAliasesRequest(stillingAlias).indices("$stillingAlias*")
        val response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT)

        return when (response.aliases.size) {
            0 -> null
            1 -> response.aliases.keys.first()
            else -> throw Exception(
                "Klarte ikke hente indeks for alias, fikk mer enn én indeks. " +
                "Antall indekser: ${response.aliases.size}"
            )
        }
    }

    companion object {
        private val osSettings = OpenSearchService::class.java
            .getResource("/stilling-common.json").readText()
        private val stillingMapping = OpenSearchService::class.java
            .getResource("/stilling-mapping.json").readText()
    }
}

