package rekrutteringsbistand.stilling.indekser.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import rekrutteringsbistand.stilling.indekser.environment
import java.io.File
import java.util.*

fun consumerConfig() = Properties().apply {
    put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1)
    put(ConsumerConfig.GROUP_ID_CONFIG, "rekrutteringsbistand-stilling-indekser")
    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)

    val bootstrapServers = when (environment().get("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> "b27apvl00045.preprod.local:8443, b27apvl00046.preprod.local:8443, b27apvl00047.preprod.local:8443"
        "prod-gcp" -> "a01apvl00145.adeo.no:8443, a01apvl00146.adeo.no:8443, a01apvl00147.adeo.no:8443, a01apvl00148.adeo.no:8443, a01apvl00149.adeo.no:8443, a01apvl00150.adeo.no:8443"
        else -> throw Exception("Kunne ikke hente Kafka bootstrap server URLer")
    }
    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)

    val schemaRegistryUrl = when (environment().get("NAIS_CLUSTER_NAME")) {
        "dev-gcp" -> "https://kafka-schema-registry.nais-q.adeo.no"
        "prod-gcp" -> "https://kafka-schema-registry.nais.adeo.no"
        else -> throw Exception("Kunne ikke hente Schema Registry URL")
    }
    put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

    val serviceuserUsername: String = environment().get("SERVICEBRUKER_BRUKERNAVN")
    val serviceuserPassword: String = environment().get("SERVICEBRUKER_PASSORD")
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
