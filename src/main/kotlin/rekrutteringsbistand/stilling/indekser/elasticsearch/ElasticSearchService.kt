package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest

const val stillingIndeks: String = "stilling"

class ElasticSearchService(private val esClient: RestHighLevelClient) {

    fun opprettIndeksHvisDenIkkeFinnes() {
        val getIndexRequest = GetIndexRequest(stillingIndeks)
        val indeksFinnes = esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)

        if (!indeksFinnes) {
            val request = CreateIndexRequest(stillingIndeks)
            esClient.indices().create(request, RequestOptions.DEFAULT)
        }
    }
}
