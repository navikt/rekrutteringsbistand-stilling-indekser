package rekrutteringsbistand.stilling.indekser.elasticsearch

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.addBasicAuthentication
import rekrutteringsbistand.stilling.indekser.environment

fun authenticateWithElasticSearchCredentials(httpClient: FuelManager): FuelManager {
    addBasicAuthentication(httpClient,
            username = environment().get("ES_USERNAME"),
            password = environment().get("ES_PASSWORD")
    )

    return httpClient
}