package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.log
import java.lang.RuntimeException
import java.time.Duration

// TODO: Injecte KafkaConsumer her? Da kan den også brukes i konsumerTopicFraStart()
class StillingConsumerImpl(private val stillingMottattService: StillingMottattService): StillingConsumer {

    override fun start() {
        KafkaConsumer<String, Ad>(consumerConfig()).use { consumer ->
            consumer.subscribe(listOf("StillingEkstern"))

            val records: ConsumerRecords<String, Ad> = consumer.poll(Duration.ofMillis(100))
            log.info("Mottok melding på Kafka: ${records.first()}")

//            while (true) {
//                // Poll, behandle, repeat
//                val records = consumer.poll(Duration.ofMillis(100))
//                failHvisMerEnnEnRecord(records)
//                stillingMottattService.behandleStilling(records.first().value())
//            }
        }
    }

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, StillingDto>) {
        if (records.count() > 1) {
            throw RuntimeException("""
                Kafka konsumer er konfigurert til å kun motta én melding om gangen.
                Får vi flere meldinger om gangen kan vi miste meldinger pga. feil committa offset.
            """.trimIndent())
        }
    }

    fun konsumerTopicFraStart() {
        // TODO Implementer denne
        //  Brukes for reindeksering av hele indeks
//        val consumer = KafkaConsumer<String, StillingDto>(consumerConfig)
//        val partitions = consumer.partitionsFor("StillingEkstern")
//        consumer.seekToBeginning(listOf())
    }
}

data class StillingDto(
    val id: String,
)

interface StillingConsumer {
    fun start()
}
