package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken
import rekrutteringsbistand.stilling.indekser.utils.Environment

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandStillingApiClientId = Environment.get("REKRUTTERINGSBISTAND_STILLING_API_CLIENT_ID")

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandStillingApiClientId/.default")
    }

    return httpClient
}
