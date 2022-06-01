package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val cluster = Environment.get("NAIS_CLUSTER_NAME")
    val scope = "api://${cluster}.toi.rekrutteringsbistand-stilling-api/.default"

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope)
    }

    return httpClient
}
