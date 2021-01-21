package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun getRestHighLevelClient(): RestHighLevelClient {
    val url = Environment.get("ELASTIC_SEARCH_API")
    val username = Environment.get("ES_USERNAME")
    val password = Environment.get("ES_PASSWORD")

    val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY,
        UsernamePasswordCredentials(username, password))

    return RestHighLevelClient(RestClient
            .builder(HttpHost.create(url))
            .setRequestConfigCallback { requestConfigBuilder: RequestConfig.Builder ->
                requestConfigBuilder
                        .setConnectionRequestTimeout(5000)
                        .setConnectTimeout(10000)
                        .setSocketTimeout(20000)
            }
            .setHttpClientConfigCallback { httpAsyncClientBuilder: HttpAsyncClientBuilder ->
                httpAsyncClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
            }
    )
}
