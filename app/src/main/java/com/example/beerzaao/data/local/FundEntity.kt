package com.example.beerzaao.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fund_entity")
data class FundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fundCode: String,
    val fundName: String,
    val sortOrder: Int = 0,
    val addedTime: Long = System.currentTimeMillis()
)
