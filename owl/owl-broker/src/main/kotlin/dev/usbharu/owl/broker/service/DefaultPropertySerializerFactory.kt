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

package dev.usbharu.owl.broker.service

import dev.usbharu.owl.common.property.*
import org.koin.core.annotation.Singleton

@Singleton(binds = [PropertySerializerFactory::class])
class DefaultPropertySerializerFactory :
    CustomPropertySerializerFactory(
        setOf(
            IntegerPropertySerializer(),
            StringPropertyValueSerializer(),
            DoublePropertySerializer(),
            BooleanPropertySerializer(),
            LongPropertySerializer(),
            FloatPropertySerializer(),
        )
    )