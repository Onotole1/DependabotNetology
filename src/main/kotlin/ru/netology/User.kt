package ru.netology

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(@SerialName("login") val login: String)