package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import rekrutteringsbistand.stilling.indekser.utils.environment

fun getEsClient(): RestHighLevelClient {
    val url = environment().get("ELASTIC_SEARCH_API")
    val username = environment().get("ES_USERNAME")
    val password = environment().get("ES_PASSWORD")

    val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY,
        UsernamePasswordCredentials(username, password))

    return RestHighLevelClient(RestClient
            .builder(HttpHost.create(url))
            .setHttpClientConfigCallback { httpAsyncClientBuilder: HttpAsyncClientBuilder ->
                httpAsyncClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
            }
    )
}