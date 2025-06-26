import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import services.requestToAgent
import java.awt.Dimension

@Composable
fun App(messages: MutableList<String>) {
    var messages by remember { mutableStateOf(messages) }
    var currentValue by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var buttonState by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.lastIndex)
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(30.dp).background(color = Color.Gray), contentAlignment = Alignment.Center){
                Text("Здесь будет меню кнопка для выбора модели и найстроек", color = Color.Black)
            }

            Box(Modifier.padding(10.dp).fillMaxWidth().weight(0.9f)) {
                LazyColumn(Modifier.background(color = Color.White).fillMaxSize(), state = listState) {
                    items(messages) { message ->
                        Text(text=message, color = Color.Black, modifier = Modifier.padding(2.dp))
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
                        messages = (messages + "[User] $currentMessage").toMutableList()
                        scope.launch {
                            var aiMessage = withContext(Dispatchers.IO) { requestToAgent(currentMessage) }
                            messages = (messages + "[gemma3] ${aiMessage}").toMutableList()
                        }
                        scope.launch(Dispatchers.Default) {
                            withContext(Dispatchers.Main) {
                                buttonState = false
                            }
                            while (true){
                                if (messages.last().contains("[gemma3]")){
                                    break
                                }
                            }
                            withContext(Dispatchers.Main) {
                                buttonState = true
                            }
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
    }
}



@Composable
@Preview
fun AppPreview() {
    App(messages = mutableListOf("[User] Привет!", "[gemma3] Привет! Чем могу помочь?"))
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        window.minimumSize = Dimension(300, 300)
        App(messages = mutableListOf())
    }
}
