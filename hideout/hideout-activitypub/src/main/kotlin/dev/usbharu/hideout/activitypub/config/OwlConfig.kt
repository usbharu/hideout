package dev.usbharu.hideout.activitypub.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.owl.common.property.*
import dev.usbharu.owl.producer.api.OWL
import dev.usbharu.owl.producer.api.OwlProducer
import dev.usbharu.owl.producer.embedded.EMBEDDED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OwlConfig {
    @Bean
    fun owlProducer(objectMapper: ObjectMapper): OwlProducer {
        return OWL(EMBEDDED) {
            this.propertySerializerFactory = CustomPropertySerializerFactory(
                setOf(
                    IntegerPropertySerializer(),
                    StringPropertyValueSerializer(),
                    DoublePropertySerializer(),
                    BooleanPropertySerializer(),
                    LongPropertySerializer(),
                    FloatPropertySerializer(),
                    ObjectPropertySerializer(objectMapper),
                )
            )
        }
    }
}