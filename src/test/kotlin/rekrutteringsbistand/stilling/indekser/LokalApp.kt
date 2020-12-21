package rekrutteringsbistand.stilling.indekser

import com.github.kittinunf.fuel.core.FuelManager

fun main() {
    val lokalHttpClient = FuelManager()

    App.start(lokalHttpClient)
}
