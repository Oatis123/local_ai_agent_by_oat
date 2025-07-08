import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import services.requestToAgent
import java.awt.Dimension

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(messages: MutableList<Map<String, String>>) {
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

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.lastIndex)
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.height(50.dp).weight(1f), contentAlignment = Alignment.CenterStart) {
                        IconButton(onClick = {}, modifier = Modifier.width(50.dp).height(50.dp), enabled = true){
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Меню"
                            )
                        }
                    }
                    Box(Modifier.height(50.dp).weight(1f), contentAlignment = Alignment.Center) {
                        DropdownMenu(
                            expanded = expended,
                            onDismissRequest = {expended = false},
                            modifier = Modifier.height(150.dp).width(200.dp).offset(x = 50.dp)
                        ){
                            models.forEach { selectiondModel ->
                                DropdownMenuItem(onClick = {
                                    selectedModel = selectiondModel
                                    expended = false
                                }){
                                    Text(text = selectiondModel)
                                }
                            }
                        }
                        Button(onClick = { expended = !expended }, modifier = Modifier.height(40.dp).width(200.dp)) {
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
                LazyColumn(Modifier.background(color = Color.White).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), state = listState) {
                    items(messages) { message ->
                        if (message.keys.contains("User")){
                            HumanMessage(text = message["User"].toString(), windowSizeDp)
                        }else{
                            AIMessage(text = message["AI"].toString(), windowSizeDp)
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
                        messages = (messages + mapOf("User" to "$currentMessage")).toMutableList()
                        scope.launch {
                            var aiMessage = withContext(Dispatchers.IO) { requestToAgent(currentMessage) }
                            messages = (messages + mapOf("AI" to "$aiMessage")).toMutableList()
                        }
                        scope.launch(Dispatchers.Default) {
                            withContext(Dispatchers.Main) {
                                buttonState = false
                            }
                            while (true){
                                if (messages.last().keys.contains("AI")){
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
    }
}



@Composable
@Preview
fun AppPreview() {
    App(messages = mutableListOf(mapOf("User" to "Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!Привет!"), mapOf("AI" to "Привет! Чем могу помочь?ривет! Чем могу помочьривет! Чем могу помочьривет! Чем могу помочьривет! Чем могу помочьривет! Чем могу помочьривет! Чем могу помочь")))
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(500, 500)
        App(messages = mutableListOf(), )
    }
}
