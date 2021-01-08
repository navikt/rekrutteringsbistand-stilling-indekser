package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val stillingIndeks: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun opprettIndeksHvisDenIkkeFinnes() {
        val indeksNavn = indeksNavnMedTimestamp()
        val getIndexRequest = GetIndexRequest(indeksNavn)
        val indeksFinnes = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)

        if (!indeksFinnes) {
            val request = CreateIndexRequest(indeksNavn)
            esClient.indices().create(request, RequestOptions.DEFAULT)
        }
    }
}

private fun indeksNavnMedTimestamp(): String {
    val dateTimeFormat = DateTimeFormatter.ofPattern("_yyyyMMdd_HHmmss")
    return stillingIndeks + LocalDateTime.now().format(dateTimeFormat)
}
