package rekrutteringsbistand.stilling.indekser.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import rekrutteringsbistand.stilling.indekser.utils.Environment
import java.io.File
import java.util.*

const val stillingEksternTopic = "StillingEkstern"

fun consumerConfig(versjon: Int) = Properties().apply {
    put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100)
    put(ConsumerConfig.GROUP_ID_CONFIG, "rekrutteringsbistand-stilling-indekser-$versjon")
    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)

    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, Environment.get("KAFKA_BROKER_URLS"))
    put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, Environment.get("KAFKA_SCHEMA_REGISTRY_URL"))
    put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)

    val serviceuserUsername = Environment.get("SERVICEBRUKER_BRUKERNAVN")
    val serviceuserPassword = Environment.get("SERVICEBRUKER_PASSORD")
    val saslJaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${serviceuserUsername}\" password=\"${serviceuserPassword}\";"
    put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig)
    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
    put(SaslConfigs.SASL_MECHANISM, "PLAIN")

    System.getenv("NAV_TRUSTSTORE_PATH")?.let {
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(it).absolutePath)
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, System.getenv("NAV_TRUSTSTORE_PASSWORD"))
    }

}
