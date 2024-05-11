/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.worker.SpringConsumerConfig
import dev.usbharu.owl.common.property.*
import dev.usbharu.owl.consumer.StandaloneConsumer
import dev.usbharu.owl.consumer.StandaloneConsumerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class WorkerRunner(
    private val springTaskRunnerLoader: SpringTaskRunnerLoader,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
    private val springCConsumerConfig: SpringConsumerConfig,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        GlobalScope.launch(Dispatchers.Default) {
            val consumer = StandaloneConsumer(
                taskRunnerLoader = springTaskRunnerLoader,
                propertySerializerFactory = CustomPropertySerializerFactory(
                    setOf(
                        IntegerPropertySerializer(),
                        StringPropertyValueSerializer(),
                        DoublePropertySerializer(),
                        BooleanPropertySerializer(),
                        LongPropertySerializer(),
                        FloatPropertySerializer(),
                        ObjectPropertySerializer(objectMapper),
                    )
                ),
                config = StandaloneConsumerConfig(
                    springCConsumerConfig.address,
                    springCConsumerConfig.port,
                    springCConsumerConfig.name,
                    springCConsumerConfig.hostname,
                    springCConsumerConfig.concurrency
                )
            )
            consumer.init()
            consumer.start()
        }
    }
}