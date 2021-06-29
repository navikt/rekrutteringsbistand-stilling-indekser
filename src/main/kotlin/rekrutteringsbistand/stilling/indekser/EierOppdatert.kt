package rekrutteringsbistand.stilling.indekser

import no.nav.helse.rapids_rivers.*
import rekrutteringsbistand.stilling.indekser.utils.log

class EierOppdatert(rapidsConnection: RapidsConnection): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "stilling_eier_oppdatert") }
            validate { it.demandKey("eier.eierNavident") }
            validate { it.demandKey("eier.eierNavn") }
            validate { it.demandKey("stillingsid") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info("Eierevent mottat: ${packet.toJson()}")
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.warn("Eierevent.onError mottat: ${problems.toExtendedReport()}")
    }


    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        log.error("Eierevent.onSevere mottat: ${error.problems.toExtendedReport()}",error)
    }
}
