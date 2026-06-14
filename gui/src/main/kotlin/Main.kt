import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
// Импорты Material 2 заменены на Material 3
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import services.*
import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import java.awt.Dimension // 1. Добавлен необходимый импорт
import java.io.File


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(messages: List<Message>, windowSizeDp: DpSize) {
    var messages by remember { mutableStateOf(messages) }
    var currentValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var buttonState by remember { mutableStateOf(true) }
    val models = listOf("gemini-2.5-flash", "gemma3:4b", "qwen3:4b")
    var expended by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(models[0]) }
    var isOverlayVisible by remember { mutableStateOf(false) }
    var currentChatId by remember { mutableStateOf("") }
    var chatList: List<ChatSummary>? by remember { mutableStateOf(emptyList()) }


    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.lastIndex)
        }
    }

    AppTheme(darkTheme = false){
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        )
        {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TopAppBar(
                        title = {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Button(
                                    onClick = { expended = !expended },
                                    modifier = Modifier.height(40.dp).width(200.dp)
                                ) {
                                    Text(text = selectedModel)
                                }
                                DropdownMenu(
                                    expanded = expended,
                                    onDismissRequest = { expended = false },
                                    modifier = Modifier.height(150.dp).width(200.dp)
                                ) {
                                    models.forEach { selectiondModel ->
                                        DropdownMenuItem(
                                            text = { Text(text = selectiondModel) },
                                            onClick = {
                                                selectedModel = selectiondModel
                                                expended = false
                                            })
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    isOverlayVisible = true
                                    scope.launch(Dispatchers.IO) {
                                        chatList = getAllChats()
                                    }
                                },
                                modifier = Modifier.width(50.dp).height(50.dp),
                                enabled = true
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Меню"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {}, modifier = Modifier.width(50.dp).height(50.dp), enabled = true) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки"
                                )
                            }
                        }
                    )

                    Box(Modifier.padding(horizontal = 10.dp).fillMaxWidth().weight(1f)) {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            state = listState
                        ) {
                            items(messages) { message ->
                                if (message.role == "user") {
                                    HumanMessage(text = message.content, windowSizeDp)
                                } else {
                                    AIMessage(text = message.content, windowSizeDp)
                                }

                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(60.dp)) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { newValue ->
                                    currentValue = newValue
                                },
                                modifier = Modifier.weight(1f).height(55.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(modifier = Modifier.size(55.dp), enabled = buttonState, onClick = {
                                if (currentValue != "") {
                                    val currentMessage = currentValue
                                    messages = (messages + Message("user", currentMessage)).toMutableList()
                                    var aiMessage = ""
                                    scope.launch {
                                        if (currentChatId != "") {
                                            aiMessage = withContext(Dispatchers.IO) {
                                                requestToAgent(
                                                    currentChatId,
                                                    selectedModel,
                                                    currentMessage
                                                )
                                            }
                                        } else {
                                            val response: NewChatResponse? = withContext(Dispatchers.IO) {
                                                createNewChat(
                                                    selectedModel,
                                                    currentMessage
                                                )
                                            }
                                            if (response != null) {
                                                aiMessage = response.agent_response
                                                currentChatId = response.id
                                            } else {
                                                aiMessage = "Ошибка при обращении к серверу"
                                            }
                                        }
                                        messages = (messages + Message("ai", aiMessage)).toMutableList()
                                    }
                                    scope.launch(Dispatchers.Default) {
                                        withContext(Dispatchers.Main) {
                                            buttonState = false
                                        }
                                        while (true) {
                                            if (messages.last().role == "ai") {
                                                break
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            buttonState = true
                                        }
                                    }
                                    currentValue = ""
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Отправить"
                                )
                            }
                        }
                    }
                }
                if (isOverlayVisible) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .clickable(enabled = false) {}
                            .background(color = MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight().widthIn(max = (windowSizeDp.width / 100 * 50))
                                .padding(20.dp)
                        ) {
                            item {
                                Button(
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    onClick = {
                                        messages = listOf()
                                        currentChatId = ""
                                        isOverlayVisible = false
                                    },
                                    shape = RoundedCornerShape(size = 16.dp)
                                ) {
                                    Text(text = "Новый чат")
                                }

                            }
                            chatList?.let { chatList ->
                                items(chatList) { chat ->
                                    var chatIsHovered by remember { mutableStateOf(false) }
                                    Card(
                                        modifier = Modifier.fillMaxWidth().height(60.dp)
                                            .onPointerEvent(PointerEventType.Enter) {
                                                chatIsHovered = true
                                            }.onPointerEvent(PointerEventType.Exit) {
                                                chatIsHovered = false
                                            }.clickable {
                                                isOverlayVisible = false
                                                currentChatId = chat.id
                                                scope.launch {
                                                    val chatData: List<Message>? =
                                                        withContext(Dispatchers.IO) { getChatById(chat.id) }
                                                    if (chatData != null) {
                                                        messages = chatData
                                                    } else {
                                                        println("Какой то кал, не работает тема эта")
                                                    }
                                                }
                                            },
                                        shape = RoundedCornerShape(size = 16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (chatIsHovered) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(text = chat.name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun AppPreview() {
    App(messages = mutableListOf(Message(role = "user", content = "Привет!"), Message(role = "ai", content = "Пока")), windowSizeDp = DpSize(800.dp, 600.dp))
}

fun main() = application {
    val windowState = rememberWindowState()
    try {
        val backendProcess = startBackendApi(
            pythonExecutable = "C:\\Users\\Oat\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
            apiModuleName = "backend.api" // Указываем имя модуля
        )
        if (backendProcess != null) {
            println("Бэкенд успешно запущен в фоновом режиме.")
            // Для тестового запуска, можно подождать немного, чтобы увидеть логи бэкенда
            // Thread.sleep(10000)
            // backendProcess.destroyForcibly()
        } else {
            println("Бэкенд не был запущен.")
        }
    } catch (e: Exception) {
        println("Произошла ошибка при запуске бэкенда: ${e.message}")
    }
    Window(onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(500, 500)
        App(messages = mutableListOf(), windowSizeDp = windowState.size)
    }
}
