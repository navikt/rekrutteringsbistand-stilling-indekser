package rekrutteringsbistand.stilling.indekser

import no.nav.helse.rapids_rivers.*
import rekrutteringsbistand.stilling.indekser.utils.log

class EierOppdatert(rapidsConnection: RapidsConnection): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "stilling_eier_oppdatert") }
            validate { it.requireKey("eier.eierNavident") }
            validate { it.requireKey("eier.eierNavn") }
            validate { it.requireKey("stillingsid") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info("Eierevent mottat: ${packet.toJson()}")
        // kall oppdaterEier
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error("Eierevent.onError mottat: ${problems.toExtendedReport()}")
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        log.info("Ignorerer melding: ${error.problems.toExtendedReport()}",error)
    }
}
