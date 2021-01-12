package rekrutteringsbistand.stilling.indekser.kafka

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import rekrutteringsbistand.stilling.indekser.utils.environment

fun getLocalEsClient(): RestHighLevelClient {
    val url = environment().get("ELASTIC_SEARCH_API")

    return RestHighLevelClient(RestClient.builder(HttpHost.create(url)))
}