package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition

val topic = TopicPartition(stillingEksternTopic, 0)

fun mockConsumer() = MockConsumer<String, Ad>(OffsetResetStrategy.EARLIEST).apply {
    schedulePollTask {
        rebalance(listOf(topic))
        updateBeginningOffsets(mapOf(Pair(topic, 0)))

        GlobalScope.launch {
            var offset: Long = 0
            while (!closed()) {
                addRecord(ConsumerRecord(stillingEksternTopic, 0, offset++, enAd.getUuid(), enAd))
                delay(5_000)
            }
        }
    }
}
