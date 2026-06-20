package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.example.ui.theme.BrandPrimaryGreen

import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.reminderNotificationsEnabled.collectAsStateWithLifecycle()
    val waterEnabled by viewModel.waterReminderEnabled.collectAsStateWithLifecycle()
    val weightEnabled by viewModel.weightReminderEnabled.collectAsStateWithLifecycle()
    
    val isPasswordUser = remember(user) { 
        FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "password" } == true 
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9))
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Settings & Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                    )
                }

                // 1. Account Section
                item { ProfileSectionHeader("ACCOUNT", isDark) }
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Header profile
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = user?.name ?: "Guest User", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1E293B))
                                    Text(text = user?.email ?: "guest@test.com", fontSize = 12.sp, color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            
                            SettingsActionRow(icon = Icons.Default.Edit, label = "Edit Display Name", isDark = isDark) { }
                            
                            if (isPasswordUser) {
                                SettingsActionRow(icon = Icons.Default.Password, label = "Change Password", isDark = isDark) { }
                            }
                            
                            SettingsActionRow(icon = Icons.Default.Logout, label = "Logout", isDark = isDark, color = Color(0xFF00C853)) {
                                viewModel.logout()
                            }
                        }
                    }
                }

                // 2. Health Profile Section
                item { ProfileSectionHeader("HEALTH PROFILE", isDark) }
                item {
                    var showHealthModal by remember { mutableStateOf(false) }
                    
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Current Health Metrics", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1E293B))
                                IconButton(onClick = { showHealthModal = true }) { Icon(Icons.Default.Edit, "Edit Metrics", tint = Color(0xFF00C853)) }
                            }
                            Text("Age: ${user?.age} • Gender: ${user?.gender}", color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B), fontSize = 13.sp)
                            Text("Height: ${user?.heightCm} cm • Weight: ${user?.weightKg} kg", color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B), fontSize = 13.sp)
                            Text("Target Weight: ${user?.targetWeightKg} kg • Goal: ${user?.goalType}", color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B), fontSize = 13.sp)
                            Text("Activity Level: ${user?.activityLevel}", color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B), fontSize = 13.sp)
                        }
                    }
                    
                    if (showHealthModal) {
                        EditHealthModal(user, isDark, onDismiss = { showHealthModal = false }) { updatedProfile ->
                            viewModel.saveUserGoals(
                                updatedProfile.gender, updatedProfile.age, updatedProfile.heightCm,
                                updatedProfile.weightKg, updatedProfile.activityLevel, updatedProfile.goalType,
                                customTargetWeightKg = updatedProfile.targetWeightKg
                            )
                            showHealthModal = false
                        }
                    }
                }

                // 3. App Preferences Section
                item { ProfileSectionHeader("APP PREFERENCES", isDark) }
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            SettingsToggleRowIcon(icon = Icons.Default.DarkMode, title = "Aesthetic Dark Mode", checked = isDark, onCheckedChange = { viewModel.toggleTheme() }, isDark = isDark)
                            SettingsToggleRowIcon(icon = Icons.Default.Notifications, title = "Reminder Notifications", checked = remindersEnabled, onCheckedChange = { viewModel.toggleReminderNotifications() }, isDark = isDark)
                            SettingsToggleRowIcon(icon = Icons.Default.LocalDrink, title = "Daily Water Reminder", checked = waterEnabled, onCheckedChange = { viewModel.toggleWaterReminder() }, isDark = isDark)
                            SettingsToggleRowIcon(icon = Icons.Default.MonitorWeight, title = "Daily Weight Reminder", checked = weightEnabled, onCheckedChange = { viewModel.toggleWeightReminder() }, isDark = isDark)
                        }
                    }
                }

                // 4. Premium Section
                item { ProfileSectionHeader("PREMIUM & USAGE", isDark) }
                item {
                    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
                    val mealScans by viewModel.monthlyMealScansUsed.collectAsStateWithLifecycle()
                    val weeklyReports by viewModel.weeklyReportsUsed.collectAsStateWithLifecycle()
                    
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = if (isPremium) "Current Plan: PRO" else "Current Plan: FREE", 
                                        color = if (isDark) Color.White else Color(0xFF1E293B), 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 16.sp
                                    )
                                    if (!isPremium) {
                                        Text("Upgrade for unlimited AI insights.", color = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                                if (!isPremium) {
                                    Button(
                                        onClick = { viewModel.setScreen(Screen.PremiumIntro) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Upgrade to PRO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            // Divider
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)))
                            
                            // Usage Stats
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Usage Limits:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color(0xFFA0AABF) else Color.DarkGray)
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Meal Scans (This Month)", fontSize = 13.sp, color = if (isDark) Color.White else Color.Black)
                                    Text(if (isPremium) "Unlimited" else "$mealScans / 10", fontSize = 13.sp, color = BrandPrimaryGreen, fontWeight = FontWeight.Bold)
                                }
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Weekly Reports (This Week)", fontSize = 13.sp, color = if (isDark) Color.White else Color.Black)
                                    Text(if (isPremium) "Unlimited" else "$weeklyReports / 1", fontSize = 13.sp, color = BrandPrimaryGreen, fontWeight = FontWeight.Bold)
                                }
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("AI Coach (Today)", fontSize = 13.sp, color = if (isDark) Color.White else Color.Black)
                                    val coachMessages by viewModel.dailyAiCoachMessagesUsed.collectAsStateWithLifecycle()
                                    Text(if (isPremium) "Unlimited" else "$coachMessages / 2", fontSize = 13.sp, color = if (isPremium) Color(0xFFFF9800) else BrandPrimaryGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 5. Support Section
                item { ProfileSectionHeader("SUPPORT", isDark) }
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            SettingsActionRow(icon = Icons.Default.Policy, label = "Privacy Policy", isDark = isDark) {}
                            SettingsActionRow(icon = Icons.Default.Article, label = "Terms of Service", isDark = isDark) {}
                            SettingsActionRow(icon = Icons.Default.HelpCenter, label = "Contact Support", isDark = isDark) {}
                        }
                    }
                }

                // 6. Danger Zone Section
                item { ProfileSectionHeader("DANGER ZONE", isDark) }
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        SettingsActionRow(icon = Icons.Default.DeleteForever, label = "Delete Account", isDark = isDark, color = Color.Red) {
                            showDeleteDialog = true
                        }
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone and will erase all your logged data, history, and preferences.") },
                    confirmButton = {
                        TextButton(onClick = { 
                            showDeleteDialog = false
                            viewModel.deleteAccount(
                                onSuccess = {},
                                onFailure = { /* Optionally show toast */ }
                            )
                        }) {
                            Text("Delete Everything", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = if (isDark) Color.White else Color(0xFF1E293B))
                        }
                    },
                    containerColor = if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9),
                    titleContentColor = if (isDark) Color.White else Color(0xFF1E293B),
                    textContentColor = if (isDark) Color(0xFFA0AABF) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun ProfileSectionHeader(text: String, isDark: Boolean) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00C853),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun SettingsActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isDark: Boolean, color: Color? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color ?: if (isDark) Color.White else Color.Black, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = color ?: if (isDark) Color.White else Color(0xFF1E293B), fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsToggleRowIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = if (isDark) Color.White else Color(0xFF1E293B), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00C853))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHealthModal(user: com.example.data.database.UserEntity?, isDark: Boolean, onDismiss: () -> Unit, onSave: (com.example.data.database.UserEntity) -> Unit) {
    var age by remember { mutableStateOf(user?.age?.toString() ?: "25") }
    var weight by remember { mutableStateOf(user?.weightKg?.toString() ?: "70") }
    var targetWeight by remember { mutableStateOf(user?.targetWeightKg?.toString() ?: "70") }
    var height by remember { mutableStateOf(user?.heightCm?.toString() ?: "170") }
    var gender by remember { mutableStateOf(user?.gender ?: "Male") }
    var activityLevel by remember { mutableStateOf(user?.activityLevel ?: "Sedentary") }
    var goalType by remember { mutableStateOf(user?.goalType ?: "Maintain Weight") }
    
    var genderExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Health Profile", color = if (isDark) Color.White else Color(0xFF1E293B)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Current Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("Target Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                
                // Gender Dropdown
                item {
                    ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
                        OutlinedTextField(
                            value = gender, onValueChange = {}, readOnly = true,
                            label = { Text("Gender") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            listOf("Male", "Female").forEach { selectionOption ->
                                DropdownMenuItem(text = { Text(selectionOption) }, onClick = { gender = selectionOption; genderExpanded = false })
                            }
                        }
                    }
                }
                
                // Activity Level Dropdown
                item {
                    ExposedDropdownMenuBox(expanded = activityExpanded, onExpandedChange = { activityExpanded = !activityExpanded }) {
                        OutlinedTextField(
                            value = activityLevel, onValueChange = {}, readOnly = true,
                            label = { Text("Activity Level") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = activityExpanded, onDismissRequest = { activityExpanded = false }) {
                            listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Athlete").forEach { selectionOption ->
                                DropdownMenuItem(text = { Text(selectionOption) }, onClick = { activityLevel = selectionOption; activityExpanded = false })
                            }
                        }
                    }
                }
                
                // Goal Type Dropdown
                item {
                    ExposedDropdownMenuBox(expanded = goalExpanded, onExpandedChange = { goalExpanded = !goalExpanded }) {
                        OutlinedTextField(
                            value = goalType, onValueChange = {}, readOnly = true,
                            label = { Text("Goal Type") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = goalExpanded, onDismissRequest = { goalExpanded = false }) {
                            listOf("Lose Weight", "Maintain Weight", "Gain Weight", "Muscle Gain").forEach { selectionOption ->
                                DropdownMenuItem(text = { Text(selectionOption) }, onClick = { goalType = selectionOption; goalExpanded = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val copy = user?.copy(
                    age = age.toIntOrNull() ?: 25,
                    heightCm = height.toFloatOrNull() ?: 170f,
                    weightKg = weight.toFloatOrNull() ?: 70f,
                    targetWeightKg = targetWeight.toFloatOrNull() ?: 70f,
                    gender = gender,
                    activityLevel = activityLevel,
                    goalType = goalType
                ) ?: com.example.data.database.UserEntity(email = "", name = "")
                onSave(copy)
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) { Text("Save & Recalculate", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = if (isDark) Color.White else Color(0xFF1E293B)) }
        },
        containerColor = if (isDark) Color(0xFF090D16) else Color(0xFFF1F5F9)
    )
}
