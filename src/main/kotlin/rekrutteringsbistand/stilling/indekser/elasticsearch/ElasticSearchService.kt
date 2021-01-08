package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import rekrutteringsbistand.stilling.indekser.utils.log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val stillingAlias: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun opprettIndeksHvisDenIkkeFinnes(indeksNavn: String) {
        val getIndexRequest = GetIndexRequest(stillingAlias)
        val indeksFinnes = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)

        if (!indeksFinnes) {
            val request = CreateIndexRequest(indeksNavn)
            esClient.indices().create(request, RequestOptions.DEFAULT)
            log.info("Opprettet indeks '$indeksNavn'")
        }
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
}

fun indeksNavnMedTimestamp(): String {
    val dateTimeFormat = DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss")
    return stillingAlias + LocalDateTime.now().format(dateTimeFormat)
}
