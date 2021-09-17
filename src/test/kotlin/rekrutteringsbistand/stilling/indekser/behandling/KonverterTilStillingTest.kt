package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Contact
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import kotlin.test.assertEquals
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
        assertEqualContactLists(enAd.getContactList(), resultat.contactList)
    }

    // TODO: Nødvendig? Denne er duplisert
    private fun assertEqualContactLists(adContactList: List<Contact>, stillingContactList: List<rekrutteringsbistand.stilling.indekser.elasticsearch.Contact>) {
        assertEquals(adContactList.size, stillingContactList.size)
        adContactList.forEachIndexed { index, adContact ->
            assertEquals(adContact.getContactperson(), stillingContactList[index].contactPersonName)
            assertEquals(adContact.getContactpersontitle(), stillingContactList[index].contactPersonTitle)
            assertEquals(adContact.getContactpersonemail(), stillingContactList[index].contactPersonEmail)
            assertEquals(adContact.getContactpersonphone(), stillingContactList[index].contactPersonPhone)
        }
    }
}
