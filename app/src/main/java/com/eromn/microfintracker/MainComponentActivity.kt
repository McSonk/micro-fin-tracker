package com.eromn.microfintracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.ui.theme.AppTheme

class MainComponentActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface (modifier = Modifier.fillMaxSize()) {
                    MessageCard(Message(
                        "Test",
                        "This is a message from the main activity"
                    ))
                }
            }
        }
    }

    data class Message(val author: String, val body: String)

    @Composable
    fun MessageCard(msg: Message){
        Row (Modifier.padding(all = 8.dp)){
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                "Todo fine",
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))

            var isExpanded by remember { mutableStateOf(false) }
            val surfaceColor by animateColorAsState(
                if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            )
            Column (modifier = Modifier.clickable{ isExpanded = !isExpanded } ) {
                Text(
                    text = msg.author,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp,
                        color = surfaceColor,
                        modifier = Modifier.animateContentSize().padding(1.dp)
                    ){
                        Text(
                            text = msg.body,
                            modifier = Modifier.padding(all=4.dp),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


            }
        }
    }

    @Composable
    fun Conversation(messages: List<Message>){
        LazyColumn {
            items(messages){ message ->
                MessageCard(message)
            }
        }
    }

    @Preview(name = "Light mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark mode"
    )
    @Composable
    fun PreviewMessage(){
        AppTheme {
            Surface {
                MessageCard(
                    Message("Test", "This is a test message")
                )
            }
        }
    }

    @Preview(name="Default")
    @Composable
    fun PreviewConversation(){
        val samples = listOf(
            Message(
                "Sonk",
                "This is a test message"
            ),
            Message(
                "Sonk",
                "This is a test response"
            )
        )
        AppTheme {
            Conversation(samples)
        }
    }

}