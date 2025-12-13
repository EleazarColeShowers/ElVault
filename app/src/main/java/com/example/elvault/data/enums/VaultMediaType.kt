package com.example.elvault.data.enums

enum class VaultMediaType(val poeticName: String) {
    IMAGE("A Captured Moment"),
    VIDEO("Moving Memories"),
    AUDIO("Echoes of Sound"),
    DOCUMENT("Written Legacy"),
    PASSWORD("Secret Whispers"),
    NOTE("Thoughts Preserved");

    companion object {
        fun fromString(value: String): VaultMediaType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: NOTE
        }
    }
}