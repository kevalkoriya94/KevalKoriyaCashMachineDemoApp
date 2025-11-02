package com.example.cashmachinedemoapp.model

import androidx.room.TypeConverter

class TransactionConverters {

    @TypeConverter
    fun fromString(value: String): Map<Int, Int> {
        if (value.isEmpty()) return emptyMap()

        return value.split(";").associate { entry ->
            val parts = entry.split(":")
            parts[0].toInt() to parts[1].toInt()
        }
    }

    @TypeConverter
    fun fromMap(map: Map<Int, Int>): String {
        return map.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return TransactionType.valueOf(name)
    }
}