package rekrutteringsbistand.stilling.indekser.setup

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun getLocalRestHighLevelClient(): RestHighLevelClient {
    val url = Environment.get("ELASTIC_SEARCH_API")

    return RestHighLevelClient(RestClient.builder(HttpHost.create(url)))
}
