package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.utils.environment

fun hentNyesteIndeks(): String {
    return hentIndeksNavn(hentVersjonFraNaisConfig())
}

fun hentVersjonFraNaisConfig(): Int {
    return environment().get("INDEKS_VERSJON").toInt()
}

fun hentVersjon(indeksNavn: String): Int {
    return indeksNavn.split("_").last().toInt()
}

fun hentIndeksNavn(versjon: Int): String {
    return "${stillingAlias}_$versjon"
}