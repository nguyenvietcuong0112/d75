package com.widget.themes.controlcenter.core.data.database.converter

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Type converters supporting UUID type persistence for Room database.
 */
class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidStr: String?): UUID? {
        return uuidStr?.let { UUID.fromString(it) }
    }
}
