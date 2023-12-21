package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.stilling.ext.avro.Ad
import org.apache.http.ConnectionClosedException
import rekrutteringsbistand.stilling.indekser.opensearch.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.KunneIkkeHenteStillingsinsinfoException
import rekrutteringsbistand.stilling.indekser.stillingsinfo.LittStillingData
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val osService: OpenSearchService
) {

    fun behandleStillingerMedRetry(ads: List<Ad>, indeksNavn: String) {
        try {
            behandleStillinger(ads, indeksNavn)
        } catch (exception: ConnectionClosedException) {
            log.warn("Feil ved kall mot Open Search, prøver igjen", exception)
            behandleStillinger(ads, indeksNavn)
        } catch (exception: KunneIkkeHenteStillingsinsinfoException) {
            log.warn("Feil ved henting av stillingsinfo, prøver igjen", exception)
            behandleStillinger(ads, indeksNavn)
        }
    }

    private fun behandleStillinger(ads: List<Ad>, indeksNavn: String) {
        val alleMeldinger = ads.map { konverterTilStilling(it) }
        val stillinger = beholdSisteMeldingPerStilling(alleMeldinger)
        val stillingsinfo = stillingsinfoClient.postStilling(stillinger.map {
            /*
             Felter som sannsnligvis er relevante:
              status: (ACTIVE, INACTIVE, STOPPED, DELETED, REJECTED)
              expires: "2023-12-20T11:00:00",
              updated: "2023-12-20T10:40:06.819288",


             Felter som KANSKJE er relevante
              published: "2023-12-20T10:37:22.503486",
              publishedByAdmin: "2023-12-20T10:37:22.503486",
              administration.status: (RECEIVED, PENDING, DONE)
              created: "2023-12-20T10:28:51.487363",
             */


            LittStillingData(
                stillingReferanse = it.uuid,
            )
        })

        val rekrutteringsbistandStillinger = stillinger.map { stilling ->
            RekrutteringsbistandStilling(stilling, stillingsinfo.find { info -> info.stillingsid == stilling.uuid })
        }

        osService.indekser(rekrutteringsbistandStillinger, indeksNavn)
    }

    private fun beholdSisteMeldingPerStilling(stillinger: List<Stilling>) =
        stillinger.associateBy { it.uuid }.values.toList()
}
