package com.example.elvault.data



import androidx.room.TypeConverter
import com.example.elvault.data.enums.VaultMediaType

class Converters {
    @TypeConverter
    fun fromMediaType(value: VaultMediaType): String {
        return value.name
    }

    @TypeConverter
    fun toMediaType(value: String): VaultMediaType {
        return VaultMediaType.fromString(value)
    }
}