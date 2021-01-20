package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.errors.WakeupException
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log
import java.time.Duration
import java.time.LocalDateTime

class StillingConsumer(
    private val consumer: Consumer<String, Ad>,
    private val stillingMottattService: StillingMottattService
) {

    suspend fun start(indeksNavn: String, onStillingerBehandlet: () -> Unit) = withContext(Dispatchers.IO) {
        try {
            consumer.subscribe(listOf(stillingEksternTopic))
            log.info("Starter å konsumere StillingEkstern-topic med groupId ${consumer.groupMetadata().groupId()}, " +
                    "indekserer på indeks '$indeksNavn'")

            var før = LocalDateTime.now()
            while (true) {
                val records: ConsumerRecords<String, Ad> = consumer.poll(Duration.ofSeconds(30))
                if (records.count() == 0) continue

                val stillinger = records.map { it.value() }
                stillingMottattService.behandleStillinger(stillinger, indeksNavn)
                consumer.commitSync()
                log.info("Committet offset ${records.last().offset()} til Kafka")

                onStillingerBehandlet()

                val diff = Duration.between(før, LocalDateTime.now()).toMillis()
                før = LocalDateTime.now()
                log.info("Konsumering av ${records.count()} stillinger tok $diff ms. I snitt ${diff / records.count()} ms per stilling")
            }

            // TODO: Retry-mekanismer

        } catch (exception: WakeupException) {
            log.info("Fikk beskjed om å lukke consument med groupId ${consumer.groupMetadata().groupId()}")
        } finally {
            consumer.close()
        }
    }

    fun stopp() {
        consumer.wakeup()
    }
}
