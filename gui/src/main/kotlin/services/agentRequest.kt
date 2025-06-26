package services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking

suspend fun requestToAgent(message: String): String {
    val client = HttpClient(CIO)
    try {
        val response: HttpResponse = client.get("http://127.0.0.1:8000/v1") {
            parameter("prompt", message)
        }
        val responseBody: String = response.bodyAsText()
        val agentResponse = responseBody.drop(1).dropLast(1)
        return agentResponse.replace("\\n", "\n")
    } catch (e: Exception) {
        return "Ошибка"
    } finally {
        client.close()
    }
}
