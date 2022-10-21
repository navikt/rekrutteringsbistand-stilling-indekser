package rekrutteringsbistand.stilling.indekser.elasticsearch

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.RestHighLevelClientBuilder
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun getRestHighLevelClient(): RestHighLevelClient {
    val url = Environment.get("OPEN_SEARCH_URI")
    val username = Environment.get("OPEN_SEARCH_USERNAME")
    val password = Environment.get("OPEN_SEARCH_PASSWORD")

    val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
    credentialsProvider.setCredentials(
        AuthScope.ANY,
        UsernamePasswordCredentials(username, password)
    )

    val httpClient: RestClient = RestClient
        .builder(HttpHost.create(url))
        .setRequestConfigCallback {
            it
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(10000)
                .setSocketTimeout(20000)
        }
        .setHttpClientConfigCallback {
            it.setDefaultCredentialsProvider(credentialsProvider)
        }
        .build()

    return RestHighLevelClientBuilder(httpClient)
        .setApiCompatibilityMode(true)
        .build()
}
