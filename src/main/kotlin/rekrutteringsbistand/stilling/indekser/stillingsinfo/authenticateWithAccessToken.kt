package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.log

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val cluster = Environment.get("NAIS_CLUSTER_NAME")
    val clusterTilRekrutteringsbistandApi = if (cluster === "prod-gcp") "prod-fss" else "dev-fss"
    val scope = "api://$clusterTilRekrutteringsbistandApi.arbeidsgiver.rekrutteringsbistand-stilling-api/.default"

    log("authenticateWithAccessToken").info("Bruker scope $scope")

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope)
    }

    return httpClient
}
