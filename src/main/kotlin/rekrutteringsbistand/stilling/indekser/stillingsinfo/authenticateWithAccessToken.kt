package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val cluster = Environment.get("NAIS_CLUSTER_NAME")
    val clusterTilRekrutteringsbistandApi = if (cluster === "prod-gcp") "prod-fss" else "dev-fss"

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(
            scope = "api://$clusterTilRekrutteringsbistandApi.arbeidsgiver.rekrutteringsbistand-stilling-api/.default"
        )
    }

    return httpClient
}
