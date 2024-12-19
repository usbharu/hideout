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

package dev.usbharu.hideout.core.infrastructure.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.net.URI

class UriColumnType(val colLength: Int) : ColumnType<URI>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.varcharType(colLength)

    override fun valueFromDB(value: Any): URI? = when (value) {
        is URI -> value
        is String -> URI(value)
        is CharSequence -> URI(value.toString())
        else -> error("Unexpected value of type String: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: URI): Any = value.toString()
}

fun Table.uri(name: String, colLength: Int): Column<URI> = registerColumn(name, UriColumnType(colLength))
