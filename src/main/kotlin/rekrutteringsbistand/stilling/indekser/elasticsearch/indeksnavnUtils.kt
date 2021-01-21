package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.indeksversjonKey

fun hentNyesteIndeks(): String {
    return hentIndeksNavn(hentVersjonFraNaisConfig())
}

fun hentVersjonFraNaisConfig(): Int {
    return Environment.get(indeksversjonKey)?.toInt() ?: throw NullPointerException("Miljøvariabel $indeksversjonKey")
}

fun hentVersjon(indeksNavn: String): Int {
    return indeksNavn.split("_").last().toInt()
}

fun hentIndeksNavn(versjon: Int): String {
    return "${stillingAlias}_$versjon"
}
