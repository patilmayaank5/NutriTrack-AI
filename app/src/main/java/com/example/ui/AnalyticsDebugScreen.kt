package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDebugScreen(viewModel: NutriViewModel) {
    val totalEvents by viewModel.analyticsManager.totalEvents.collectAsStateWithLifecycle()
    val eventLogs by viewModel.analyticsManager.eventLogs.collectAsStateWithLifecycle()
    
    val totalExceptions by viewModel.crashlyticsManager.totalExceptions.collectAsStateWithLifecycle()
    val lastException by viewModel.crashlyticsManager.lastException.collectAsStateWithLifecycle()

    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val bgColor = if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9)
    val cardColor = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Debug", color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setScreen(Screen.Dashboard) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Tracked Events", fontSize = 16.sp, color = textColor)
                Text(totalEvents.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Non-Fatal Exceptions", fontSize = 16.sp, color = textColor)
                Text(totalExceptions.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Firebase Status", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
            Text("Analytics: Active and logging (${eventLogs.size} events in mem buffer)", fontSize = 12.sp, color = if (isDark) Color.Gray else Color.DarkGray)
            Text("Crashlytics: Active (Crash reporting enabled)", fontSize = 12.sp, color = if (isDark) Color.Gray else Color.DarkGray)
            if (lastException != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Last Exception: ${lastException}", fontSize = 12.sp, color = Color(0xFFE53935))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Events (Last 20)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventLogs) { log ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardColor)
                            .padding(12.dp)
                    ) {
                        Text(log, fontSize = 12.sp, color = textColor, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
