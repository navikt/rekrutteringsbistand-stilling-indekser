package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log
import java.time.Duration

class StillingConsumerImpl(
    private val kafkaConsumer: KafkaConsumer<String, Ad>,
    private val stillingMottattService: StillingMottattService
): StillingConsumer {

    override fun start() {
        kafkaConsumer.use { consumer ->
            consumer.subscribe(listOf("StillingEkstern"))

            while (true) {
                val records: ConsumerRecords<String, Ad> = consumer.poll(Duration.ofSeconds(30))
                failHvisMerEnnEnRecord(records)
                if (records.count() == 0) continue
                val melding = records.first()
                stillingMottattService.behandleStilling(melding.value())
                consumer.commitSync()
                log.info("Committet offset ${melding.offset()} til Kafka")
            }

            // TODO: Retry-mekanismer
        }
    }

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, Ad>) {
        if (records.count() > 1) {
            throw Exception("""
                Kafka konsumer er konfigurert til å kun motta én melding om gangen.
                Får vi flere meldinger om gangen kan vi miste meldinger pga. feil committa offset.
            """.trimIndent())
        }
    }

    // TODO: Trenger ikke denne på "hoved"-consumeren?
    //  Trenger kun én consumer nr. 2 som alltid leser fra start
    override fun konsumerTopicFraBegynnelse() {
        log.info("Spoler Kafka-offset tilbake til begynnelsen")
        kafkaConsumer.seekToBeginning(kafkaConsumer.assignment())
    }
}

interface StillingConsumer {
    fun start()
    fun konsumerTopicFraBegynnelse()
}
