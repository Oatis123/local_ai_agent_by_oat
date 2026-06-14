import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun HumanMessage(text: String, windowSize: DpSize) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Card(
            // Используем цвета из темы M3 для лучшей адаптивности
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ),
            modifier = Modifier.widthIn(max = (windowSize.width / 100 * 70))
        ) {
            Text(
                text = text,
                // Цвет текста также берется из темы, чтобы обеспечить читаемость
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}


@Composable
fun AIMessage(text: String, windowSize: DpSize) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            // Для сообщений ИИ можно использовать другой цвет из темы, например secondary
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ),
            modifier = Modifier.widthIn(max = (windowSize.width / 100 * 70))
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}