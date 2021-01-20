package rekrutteringsbistand.stilling.indekser.utils

object Environment {
    private val miljøvariabler: MutableMap<String, String> = HashMap()

    fun get(s: String): String? {
        return miljøvariabler[s] ?: System.getenv(s)
    }

    fun set(s: String, value: String) {
        miljøvariabler[s] = value
    }
}
