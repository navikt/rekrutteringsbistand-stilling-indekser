package rekrutteringsbistand.stilling.indekser.setup

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import rekrutteringsbistand.stilling.indekser.kafka.stillingsTopic

val topic = TopicPartition(stillingsTopic, 0)

fun mockConsumer(periodiskSendMeldinger: Boolean = true) = MockConsumer<String, Ad>(OffsetResetStrategy.EARLIEST).apply {
    schedulePollTask {
        rebalance(listOf(topic))
        updateBeginningOffsets(mapOf(Pair(topic, 0)))

        if (periodiskSendMeldinger) {
            GlobalScope.launch {
                var offset: Long = 0
                while (!closed()) {
                    addRecord(ConsumerRecord(stillingsTopic, 0, offset++, enAd.getUuid(), enAd))
                    delay(5_000)
                }
            }
        }
    }
}
