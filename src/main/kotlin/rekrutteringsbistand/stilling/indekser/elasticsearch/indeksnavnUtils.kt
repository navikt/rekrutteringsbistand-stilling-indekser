package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.utils.Environment
import rekrutteringsbistand.stilling.indekser.utils.Environment.Keys.indeksVersjon

fun hentNyesteIndeks(): String {
    return hentIndeksNavn(hentVersjonFraNaisConfig())
}

fun hentVersjonFraNaisConfig(): Int {
    return Environment.get(indeksVersjon)?.toInt() ?: throw NullPointerException("Miljøvariabel $indeksVersjon")
}

fun hentVersjon(indeksNavn: String): Int {
    return indeksNavn.split("_").last().toInt()
}

fun hentIndeksNavn(versjon: Int): String {
    return "${stillingAlias}_$versjon"
}
