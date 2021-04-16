package ru.netology

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueComment(@SerialName("body") val body: String)