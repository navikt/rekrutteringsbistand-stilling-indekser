package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://dev-fss.arbeidsgiver.rekrutteringsbistand-api/.default")
    }

    return httpClient
}
