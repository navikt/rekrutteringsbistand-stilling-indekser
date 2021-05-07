package rekrutteringsbistand.stilling.indekser.setup

import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import rekrutteringsbistand.stilling.indekser.kafka.stillingstopic

fun mottaKafkamelding(consumer: MockConsumer<String, Ad>, ad: Ad, offset: Long = 0) {
    val melding = ConsumerRecord(stillingstopic, 0, offset, ad.getUuid(), ad)
    consumer.schedulePollTask {
        consumer.addRecord(melding)
    }
}
