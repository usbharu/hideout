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

package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import dev.usbharu.hideout.activitypub.domain.model.*

class ObjectDeserializer : JsonDeserializer<Object>() {
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Object? {
        requireNotNull(p)
        val treeNode: JsonNode = requireNotNull(p.codec?.readTree(p))
        if (treeNode.isValueNode) {
            return ObjectValue(
                emptyList(),
                treeNode.asText()
            )
        } else if (treeNode.isObject) {
            val type = treeNode["type"]
            val activityType = if (type.isArray) {
                type.firstNotNullOf { jsonNode: JsonNode ->
                    ExtendedActivityVocabulary.entries.firstOrNull { it.name.equals(jsonNode.asText(), true) }
                }
            } else if (type.isValueNode) {
                ExtendedActivityVocabulary.entries.firstOrNull { it.name.equals(type.asText(), true) }
            } else {
                null
            }

            return when (activityType) {
                ExtendedActivityVocabulary.Follow -> p.codec.treeToValue(treeNode, Follow::class.java)
                ExtendedActivityVocabulary.Note -> p.codec.treeToValue(treeNode, Note::class.java)
                ExtendedActivityVocabulary.Object -> p.codec.treeToValue(treeNode, Object::class.java)
                ExtendedActivityVocabulary.Link -> null
                ExtendedActivityVocabulary.Activity -> null
                ExtendedActivityVocabulary.IntransitiveActivity -> null
                ExtendedActivityVocabulary.Collection -> null
                ExtendedActivityVocabulary.OrderedCollection -> null
                ExtendedActivityVocabulary.CollectionPage -> null
                ExtendedActivityVocabulary.OrderedCollectionPage -> null
                ExtendedActivityVocabulary.Accept -> p.codec.treeToValue(treeNode, Accept::class.java)
                ExtendedActivityVocabulary.Add -> null
                ExtendedActivityVocabulary.Announce -> p.codec.treeToValue(treeNode, Announce::class.java)
                ExtendedActivityVocabulary.Arrive -> null
                ExtendedActivityVocabulary.Block -> p.codec.treeToValue(treeNode, Block::class.java)
                ExtendedActivityVocabulary.Create -> p.codec.treeToValue(treeNode, Create::class.java)
                ExtendedActivityVocabulary.Delete -> p.codec.treeToValue(treeNode, Delete::class.java)
                ExtendedActivityVocabulary.Dislike -> null
                ExtendedActivityVocabulary.Flag -> null
                ExtendedActivityVocabulary.Ignore -> null
                ExtendedActivityVocabulary.Invite -> null
                ExtendedActivityVocabulary.Join -> null
                ExtendedActivityVocabulary.Leave -> null
                ExtendedActivityVocabulary.Like -> p.codec.treeToValue(treeNode, Like::class.java)
                ExtendedActivityVocabulary.Listen -> null
                ExtendedActivityVocabulary.Move -> null
                ExtendedActivityVocabulary.Offer -> null
                ExtendedActivityVocabulary.Question -> null
                ExtendedActivityVocabulary.Reject -> p.codec.treeToValue(treeNode, Reject::class.java)
                ExtendedActivityVocabulary.Read -> null
                ExtendedActivityVocabulary.Remove -> null
                ExtendedActivityVocabulary.TentativeReject -> null
                ExtendedActivityVocabulary.TentativeAccept -> null
                ExtendedActivityVocabulary.Travel -> null
                ExtendedActivityVocabulary.Undo -> p.codec.treeToValue(treeNode, Undo::class.java)
                ExtendedActivityVocabulary.Update -> null
                ExtendedActivityVocabulary.View -> null
                ExtendedActivityVocabulary.Application -> null
                ExtendedActivityVocabulary.Group -> null
                ExtendedActivityVocabulary.Organization -> null
                ExtendedActivityVocabulary.Person -> p.codec.treeToValue(treeNode, Person::class.java)
                ExtendedActivityVocabulary.Service -> null
                ExtendedActivityVocabulary.Article -> null
                ExtendedActivityVocabulary.Audio -> null
                ExtendedActivityVocabulary.Document -> p.codec.treeToValue(treeNode, Document::class.java)
                ExtendedActivityVocabulary.Event -> null
                ExtendedActivityVocabulary.Image -> p.codec.treeToValue(treeNode, Image::class.java)
                ExtendedActivityVocabulary.Page -> null
                ExtendedActivityVocabulary.Place -> null
                ExtendedActivityVocabulary.Profile -> null
                ExtendedActivityVocabulary.Relationship -> null
                ExtendedActivityVocabulary.Tombstone -> p.codec.treeToValue(treeNode, Tombstone::class.java)
                ExtendedActivityVocabulary.Video -> null
                ExtendedActivityVocabulary.Mention -> null
                ExtendedActivityVocabulary.Emoji -> p.codec.treeToValue(treeNode, Emoji::class.java)
                null -> null
            }
        } else {
            return null
        }
    }
}
