package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.utils.Environment

fun hentNyesteIndeks(): String {
    return hentIndeksNavn(hentVersjonFraNaisConfig())
}

fun hentVersjonFraNaisConfig(): Int {
    return Environment.get("INDEKS_VERSJON")?.toInt() ?: throw Exception("INDEKS_VERSJON er ikke definert")
}

fun hentVersjon(indeksNavn: String): Int {
    return indeksNavn.split("_").last().toInt()
}

fun hentIndeksNavn(versjon: Int): String {
    return "${stillingAlias}_$versjon"
}