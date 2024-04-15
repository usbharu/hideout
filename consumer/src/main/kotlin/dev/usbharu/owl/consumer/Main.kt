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

package dev.usbharu.owl.consumer

import dev.usbharu.dev.usbharu.owl.consumer.ConsumerConfig
import dev.usbharu.owl.AssignmentTaskServiceGrpcKt
import dev.usbharu.owl.SubscribeTaskServiceGrpcKt
import dev.usbharu.owl.TaskResultServiceGrpcKt
import dev.usbharu.owl.common.property.CustomPropertySerializerFactory
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking

fun main() {

    val consumerConfig = ConsumerConfig(20, "localhost", 50051)

    val channel = ManagedChannelBuilder.forAddress(consumerConfig.address, consumerConfig.port).usePlaintext().build()
    val subscribeTaskServiceCoroutineStub = SubscribeTaskServiceGrpcKt.SubscribeTaskServiceCoroutineStub(channel)
    val assignmentTaskServiceCoroutineStub = AssignmentTaskServiceGrpcKt.AssignmentTaskServiceCoroutineStub(channel)
    val taskResultServiceCoroutineStub = TaskResultServiceGrpcKt.TaskResultServiceCoroutineStub(channel)
    val customPropertySerializerFactory = CustomPropertySerializerFactory(emptySet())
    val consumer = Consumer(
        subscribeTaskServiceCoroutineStub, assignmentTaskServiceCoroutineStub, taskResultServiceCoroutineStub,
        emptyMap(), customPropertySerializerFactory, consumerConfig
    )

    runBlocking {
        consumer.start()
    }

}