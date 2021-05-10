package ru.netology

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json as SerializationJson

const val BASE_URL = "https://api.github.com/repos/netology-code/"

val repositories = listOf(
    "andad-code",
    "andin-code",
    "andin-homeworks",
    "kt-code",
)

/**
 * Первым аргументом передаётся токен с правами repo
 */
fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: error("Token is blank")

    runBlocking {
        val client = HttpClient {
            Logging()
            Json {
                serializer = KotlinxSerializer(
                    SerializationJson {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }

        repositories.map { repository ->
            async {
                var page = 0
                while (true) {
                    client.get<List<Pull>>(urlString = "$BASE_URL$repository/pulls") {
                        header("Authorization", "bearer $token")
                        parameter("per_page", 100)
                        parameter("page", page++)
                    }.ifEmpty {
                        return@async
                    }.filter {
                        it.user.login == "dependabot[bot]"
                    }.map {
                        async {
                            client.post<Unit>(urlString = "${it.issueUrl}/comments") {
                                body = IssueComment("""@dependabot merge""")
                                header("Authorization", "bearer $token")
                                header("Content-Type", "application/json")
                            }
                        }
                    }.awaitAll()
                }
            }
        }.awaitAll()
    }
}
