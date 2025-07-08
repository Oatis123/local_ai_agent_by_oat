import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.desktop.ui.tooling.preview.Preview


@Composable
fun HumanMessage(text: String, windowSize: DpSize){
    MaterialTheme {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
            Card(backgroundColor = Color.Gray, shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ), modifier = Modifier.widthIn(max = (windowSize.width/100 * 70))) {
                Text(text = text, color = Color.White, modifier = Modifier.padding(10.dp))
            }
        }
    }
}


@Composable
fun AIMessage(text: String, windowSize: DpSize){
    MaterialTheme {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start){
            Card(backgroundColor = Color.Gray, shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ), modifier = Modifier.widthIn(max = (windowSize.width/100 * 70))) {
                Text(text = text, color = Color.White, modifier = Modifier.padding(10.dp))
            }
        }
    }
}