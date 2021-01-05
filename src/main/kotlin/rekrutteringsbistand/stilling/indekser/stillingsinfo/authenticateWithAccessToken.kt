package rekrutteringsbistand.stilling.indekser.stillingsinfo

import com.github.kittinunf.fuel.core.FuelManager
import rekrutteringsbistand.stilling.indekser.autentisering.AccessTokenClient
import rekrutteringsbistand.stilling.indekser.autentisering.addBearerToken

fun authenticateWithAccessToken(httpClient: FuelManager, accessTokenClient: AccessTokenClient): FuelManager {
    val rekrutteringsbistandApiClientId = "fe698176-ac44-4260-b8d0-dbf45dd956cf"

    addBearerToken(httpClient) {
        accessTokenClient.getAccessToken(scope = "api://$rekrutteringsbistandApiClientId/.default")
    }

    return httpClient
}
