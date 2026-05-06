package com.elsewhere.eyris.domain.models

data class User(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val lastOnline: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
