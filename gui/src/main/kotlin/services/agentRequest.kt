package services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID // Для UUID, если нужно генерировать на клиенте, хотя FastAPI генерирует его

// --- Data Classes для соответствия JSON-структурам FastAPI ---

@Serializable
data class ChatResponse(
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

// КЛАСС УДАЛЕН: ChatData больше не нужен, так как API возвращает список напрямую.
// @Serializable
// data class ChatData(
//     val messages: List<Message>
// )

@Serializable
data class NewChatResponse(
    val id: String, // UUID будет строкой
    val agent_response: String
)

@Serializable
data class ChatSummary(
    val name: String,
    val id: String
)

// --- Инициализация HttpClient с настройками JSON ---
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true // Игнорировать поля, которых нет в наших дата-классах
            isLenient = true // Разрешить более свободный парсинг JSON
        })
    }
}

// --- Функции для взаимодействия с FastAPI эндпоинтами ---

/**
 * Отправляет новое сообщение агенту и получает его ответ.
 */
suspend fun requestToAgent(chat_id: String, model_name: String, new_msg: String): String {
    try {
        val response: HttpResponse = httpClient.put("http://127.0.0.1:8000/new_message") {
            parameter("chat_id", chat_id)
            parameter("model_name", model_name)
            parameter("new_msg", new_msg)
        }
        val responseBody: String = response.bodyAsText()
        val agentResponse = responseBody.drop(1).dropLast(1)
        return agentResponse.replace("\\n", "\n")
    } catch (e: Exception) {
        println("Ошибка при отправке сообщения агенту: ${e.message}")
        e.printStackTrace()
        return "Ошибка: ${e.message}"
    }
}

/**
 * ИСПРАВЛЕНО: Получает историю сообщений чата по его ID.
 * Соответствует эндпоинту FastAPI: GET /chat_by_id
 *
 * @param chat_id ID чата для получения.
 * @return Список объектов Message или null в случае ошибки.
 */
suspend fun getChatById(chat_id: String): List<Message>? {
    try {
        val response: HttpResponse = httpClient.get("http://127.0.0.1:8000/chat_by_id") {
            parameter("chat_id", chat_id)
        }
        // 1. Сначала десериализуем ответ в новый класс ChatResponse
        val chatResponse = response.body<ChatResponse>()

        // 2. Возвращаем из него список сообщений
        return chatResponse.messages

    } catch (e: Exception) {
        println("Ошибка при получении чата по ID $chat_id: ${e.message}")
        e.printStackTrace()
        return null
    }
}

/**
 * Создает новый чат с первым сообщением и выбранной моделью.
 */
suspend fun createNewChat(model_name: String, new_message: String): NewChatResponse? {
    try {
        val response: HttpResponse = httpClient.put("http://127.0.0.1:8000/new_chat") {
            parameter("model_name", model_name)
            parameter("new_message", new_message)
        }
        return response.body<NewChatResponse>()
    } catch (e: Exception) {
        println("Ошибка при создании нового чата: ${e.message}")
        e.printStackTrace()
        return null
    }
}

/**
 * Получает список всех чатов (только ID и имя).
 */
suspend fun getAllChats(): List<ChatSummary> {
    try {
        val response: HttpResponse = httpClient.get("http://127.0.0.1:8000/all_chats")
        return response.body<List<ChatSummary>>()
    } catch (e: Exception) {
        println("Ошибка при получении списка всех чатов: ${e.message}")
        e.printStackTrace()
        return emptyList()
    }
}

/**
 * Удаляет чат по его ID.
 * Соответствует эндпоинту FastAPI: GET /delet_chat_by_id
 *
 * @param id ID чата для удаления.
 * @return `true` если удаление прошло успешно, `false` в случае ошибки.
 */
suspend fun deleteChatById(id: String): Boolean {
    return try {
        val response: HttpResponse = httpClient.get("http://127.0.0.1:8000/delet_chat_by_id") {
            parameter("id", id)
        }
        // Проверяем, что сервер вернул статус 200 OK и тело ответа "succesfull"
        response.status.value == 200 && response.bodyAsText() == "succesfull"
    } catch (e: Exception) {
        println("Ошибка при удалении чата по ID $id: ${e.message}")
        e.printStackTrace()
        false
    }
}