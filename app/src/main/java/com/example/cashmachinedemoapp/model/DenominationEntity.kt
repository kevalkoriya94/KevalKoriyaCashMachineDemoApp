package com.example.cashmachinedemoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "denomination")
data class DenominationEntity(
    @PrimaryKey val value: Int,
    val count: Int,
    )