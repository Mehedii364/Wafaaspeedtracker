package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiMonitorScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val aiService = app.aiService

    val trips by tripRepo.completedTrips.collectAsStateWithLifecycle(emptyList())

    var queryInput by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "ai",
                text = "Welcome to Wafa AI Monitor. I analyze your completed journeys and driving patterns using privacy-safe, aggregated telemetry. You can ask me questions about your driving habits, longest trips, or driving efficiency."
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wafa AI Monitor",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Privacy-first trip analysis & insights",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyanNeon.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(text = "LOCAL INTELLIGENCE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = CyanNeon)
            }
        }

        // Quick suggested questions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(label = "Longest Trip?") {
                val ans = aiService.answerQuery("Show my longest trips", trips)
                messages.add(ChatMessage("user", "Show my longest trips"))
                messages.add(ChatMessage("ai", ans))
            }
            QuickChip(label = "Top Speed?") {
                val ans = aiService.answerQuery("Which trip had the highest recorded speed?", trips)
                messages.add(ChatMessage("user", "Which trip had the highest recorded speed?"))
                messages.add(ChatMessage("ai", ans))
            }
            QuickChip(label = "Total Distance?") {
                val ans = aiService.answerQuery("How far did I travel?", trips)
                messages.add(ChatMessage("user", "How far did I travel?"))
                messages.add(ChatMessage("ai", ans))
            }
        }

        // Chat message history
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .background(if (isUser) CyanNeon else Color(0xFF131B2F))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 13.sp,
                            color = if (isUser) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                            fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Query Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                placeholder = { Text("Ask about your driving stats...", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (queryInput.isNotBlank()) {
                        val q = queryInput
                        queryInput = ""
                        messages.add(ChatMessage("user", q))
                        val ans = aiService.answerQuery(q, trips)
                        messages.add(ChatMessage("ai", ans))
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyanNeon)
            ) {
                Icon(
                    imageVector = WafaIcons.Send,
                    contentDescription = "Send",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131B2F))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = CyanNeon,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
