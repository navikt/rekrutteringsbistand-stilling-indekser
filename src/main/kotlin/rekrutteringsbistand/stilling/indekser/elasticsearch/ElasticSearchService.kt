package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest

class ElasticSearchService(private val esClient: RestHighLevelClient) {
    private val esIndex: String = "stilling"

    fun opprettIndeks() {
        val request = CreateIndexRequest(esIndex)
        esClient.indices().create(request, RequestOptions.DEFAULT)
    }
}
