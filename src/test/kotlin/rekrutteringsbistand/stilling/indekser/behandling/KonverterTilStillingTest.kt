package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Contact
import no.nav.pam.stilling.ext.avro.StyrkCategory
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enAdMed
import rekrutteringsbistand.stilling.indekser.setup.enAdUtenKontaktinformasjon
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KonverterTilStillingTest {

    // Gitt en annonse for en direktemeldt stilling med flere styrk-koder
    // når konverterer
    // så skal styrkEllerTittel-feltet være styrknavnet til styrk-oden med 6 siffer (fordi det er bare Rekbis som bruker 6 siffer)
    @Test
    fun `Skal mappe STYRK-navn til tittel for direktemeldt stilling`() {
        val forventetNavn = "navn666666"
        val styrkkodePåRekbisFormat = "6666.66"
        val styrk = listOf(StyrkCategory("1234", "aaa"), StyrkCategory("4567", "bbb"), StyrkCategory(styrkkodePåRekbisFormat, forventetNavn))

        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"
        val resultat = konverterTilStilling(enAdMed(
            source = "DIR",
            categories = styrk,
            title = tittelFraArbeidsplassen
        ))

        assertEquals(forventetNavn, resultat.styrkEllerTittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title) // NB: assert byttet ut med null-ish når migrering er ferdig
        assertEquals("DIR", resultat.source)
    }

    // Gitt en annonse for en ekstern stilling med flere styrk-koder
    // når konverterer
    // så skal tittelfeltet være arbeidsplassen-tittelen
    @Test
    fun `Skal mappe arbeidsplassentittel for ekstern stilling`() {
        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"
        val styrkkodePåRekbisFormat = "6666.66"
        val styrk = listOf(StyrkCategory("1234", "aaa"), StyrkCategory("4567", "bbb"), StyrkCategory(styrkkodePåRekbisFormat, tittelFraArbeidsplassen))
        val kilde = "ekstern"

        val resultat = konverterTilStilling(enAdMed(source = kilde, categories = styrk, title = tittelFraArbeidsplassen))

        assertEquals(tittelFraArbeidsplassen, resultat.styrkEllerTittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title)
        assertEquals(kilde, resultat.source)
    }


    // Gitt en annonse for en direktemeldt stilling med flere gylde styrk-koder
    // når konverterer
    // så skal tittelfeltet være ???


    // Gitt en annonse for en direktemeldt stilling uten styrk
    // når konverterer
    // så skal ???

    // Gitt en annonse for en direktemeldt stilling med styrk som har feil format
    // når konverterer
    // så skal tittelfeltet inneholde en standardtekst (TODO: hva?)

    // Denne venter vi med til altt annet er gjort
    // Gitt en annonse for en direktemeldt stilling, som selvfølgelig har tittel
    // når konverterer
    // så skal ikke tittel finnes


    @Test
    fun `Skal mappe felter riktig`() {
        val resultat = konverterTilStilling(enAd)
        assertEquals(enAd.getPublishedByAdmin(), resultat.publishedByAdmin)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(enAd.getCreated(), resultat.created)
        assertEquals(enAd.getPublished(), resultat.published)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(
            resultat.properties["tags"],
            jacksonObjectMapper().readTree("[\"INKLUDERING__ARBEIDSTID\", \"TILTAK_ELLER_VIRKEMIDDEL__LÆRLINGPLASS\"]")
        )
        assertEqualContactLists(enAd.getContacts(), resultat.contacts)
    }

    @Test
    fun `Skal mappe stilling uten kontaktinformasjon`() {
        val resultat = konverterTilStilling(enAdUtenKontaktinformasjon)
        assertTrue(resultat.contacts.isEmpty())
    }
}

fun assertEqualContactLists(adContactList: List<Contact>, stillingContactList: List<rekrutteringsbistand.stilling.indekser.opensearch.Contact>) {
    assertEquals(adContactList.size, stillingContactList.size)
    adContactList.forEachIndexed { index, adContact ->
        assertEquals(adContact.getName(), stillingContactList[index].name)
        assertEquals(adContact.getRole(), stillingContactList[index].role)
        assertEquals(adContact.getTitle(), stillingContactList[index].title)
        assertEquals(adContact.getEmail(), stillingContactList[index].email)
        assertEquals(adContact.getPhone(), stillingContactList[index].phone)
    }
}
