package com.rolandlin.shelfie.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Stores List<String> as a JSON array rather than a delimited string, since titles and tags may contain commas. */
internal class Converters {
    private val stringList = ListSerializer(String.serializer())

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(stringList, value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(stringList, value)
}
