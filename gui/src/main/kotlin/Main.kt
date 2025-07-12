import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import services.*
import java.awt.Dimension
import kotlin.collections.mutableListOf as mutableListOf

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(messages: List<Message>) {
    var messages by remember { mutableStateOf(messages) }
    var currentValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var buttonState by remember { mutableStateOf(true) }
    val windowInfo = LocalWindowInfo.current
    val windowSize = windowInfo.containerSize
    val density = LocalDensity.current
    val windowSizeDp = with(density) {windowSize.toSize().toDpSize()}
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

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.height(50.dp).weight(1f), contentAlignment = Alignment.CenterStart) {
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
                        }
                        Box(Modifier.height(50.dp).weight(1f), contentAlignment = Alignment.Center) {
                            DropdownMenu(
                                expanded = expended,
                                onDismissRequest = { expended = false },
                                modifier = Modifier.height(150.dp).width(200.dp)
                            ) {
                                models.forEach { selectiondModel ->
                                    DropdownMenuItem(onClick = {
                                        selectedModel = selectiondModel
                                        expended = false
                                    }) {
                                        Text(text = selectiondModel)
                                    }
                                }
                            }
                            Button(
                                onClick = { expended = !expended },
                                modifier = Modifier.height(40.dp).width(200.dp)
                            ) {
                                Text(text = selectedModel)
                            }
                        }
                        Box(Modifier.height(50.dp).weight(1f), contentAlignment = Alignment.CenterEnd) {
                            IconButton(onClick = {}, modifier = Modifier.width(50.dp).height(50.dp), enabled = true) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки"
                                )
                            }
                        }
                    }
                }

                Box(Modifier.padding(10.dp).fillMaxWidth().weight(0.9f)) {
                    LazyColumn(
                        Modifier.background(color = Color.White).fillMaxSize(),
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
                Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.BottomCenter) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = currentValue,
                            onValueChange = { newValue ->
                                currentValue = newValue
                            },
                            modifier = Modifier.weight(0.8f).padding(10.dp).height(50.dp)
                        )
                        IconButton(modifier = Modifier.height(50.dp).width(50.dp), enabled = buttonState, onClick = {
                            var currentMessage = currentValue
                            messages = (messages + Message("user", "$currentMessage")).toMutableList()
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
                                }else {
                                    var response: NewChatResponse? = withContext(Dispatchers.IO){
                                        createNewChat(
                                            selectedModel,
                                            currentMessage
                                        )
                                    }
                                    if (response != null) {
                                        aiMessage = response.agent_response
                                        currentChatId = response.id
                                    }else {
                                        aiMessage = "Ошибка при обращении к серверу"
                                    }
                                }
                                messages = (messages + Message("ai", "$aiMessage")).toMutableList()
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
                        .background(color = Color.White)
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    LazyColumn(modifier = Modifier.fillMaxHeight().widthIn(max = ((windowSizeDp.width / 100) * 50)).padding(20.dp)) {
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
                                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                    Card(
                                        modifier = Modifier.fillMaxSize()
                                        //.background(color = if (chatIsHovered) Color.LightGray else Color.White)
                                        .onPointerEvent(PointerEventType.Enter) {
                                            chatIsHovered = true
                                        }.onPointerEvent(PointerEventType.Exit) {
                                        chatIsHovered = false
                                    }.clickable {
                                        isOverlayVisible = false
                                        scope.launch {
                                            var chatData: List<Message>? = withContext(Dispatchers.IO) { getChatById(chat.id) }
                                            if (chatData != null) {
                                                messages = chatData
                                            } else {
                                                println("Какой то кал, не работает тема эта")
                                            }
                                        }
                                    }, shape = RoundedCornerShape(size = 16.dp),
                                        backgroundColor = if (chatIsHovered) Color.LightGray else Color.White,
                                        elevation = 0.dp
                                    ) {
                                        Box(
                                            modifier = Modifier.weight(0.8f).padding(8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(text = chat.name)
                                        }

                                        //Box(
                                        //    modifier = Modifier.weight(0.2f).padding(8.dp)){
                                        //    IconButton(onClick = {
                                        //        scope.launch {
                                        //            val successfullyDeleted = withContext(Dispatchers.IO) {
                                        //                deleteChatById(chat.id)
                                        //            }

                                        //            if (successfullyDeleted) {
                                        //                chatList = chatList - chat
                                        //            } else {
                                        //                println("Ошибка при удалении чата на сервере")
                                        //            }
                                        //        }
                                        //    }) {
                                        //        Icon(
                                        //            imageVector = Icons.Filled.Delete,
                                        //            contentDescription = "Удалить чат"
                                        //        )
                                        //    }
                                        //        }
                                        //    }
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
    App(messages = mutableListOf(Message(role = "user", content = "Привет!"), Message(role = "ai", content = "Да пошёл ты нахуй!")))
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(500, 500)
        App(messages = mutableListOf(), )
    }
}
