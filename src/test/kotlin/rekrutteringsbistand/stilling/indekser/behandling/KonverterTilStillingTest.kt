package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Contact
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enAdUtenKontaktinformasjon
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KonverterTilStillingTest {

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

fun assertEqualContactLists(adContactList: List<Contact>, stillingContactList: List<rekrutteringsbistand.stilling.indekser.elasticsearch.Contact>) {
    assertEquals(adContactList.size, stillingContactList.size)
    adContactList.forEachIndexed { index, adContact ->
        assertEquals(adContact.getName(), stillingContactList[index].name)
        assertEquals(adContact.getRole(), stillingContactList[index].role)
        assertEquals(adContact.getTitle(), stillingContactList[index].title)
        assertEquals(adContact.getEmail(), stillingContactList[index].email)
        assertEquals(adContact.getPhone(), stillingContactList[index].phone)
    }
}