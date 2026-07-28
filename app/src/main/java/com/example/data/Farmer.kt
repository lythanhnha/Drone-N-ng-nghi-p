package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class Farmer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
