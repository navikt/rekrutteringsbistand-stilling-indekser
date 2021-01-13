package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.utils.environment

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandApiClientId = environment().get("REKRUTTERINGSBISTAND_API_CLIENT_ID")

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
    }

    return httpClient
}
