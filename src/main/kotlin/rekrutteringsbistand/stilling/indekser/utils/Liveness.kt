package rekrutteringsbistand.stilling.indekser.utils

import java.lang.Exception

object Liveness {
    var isAlive = true
        private set

    fun kill(årsak: String, exception: Exception) {
        log.error(årsak, exception)
        isAlive = false
    }
}
