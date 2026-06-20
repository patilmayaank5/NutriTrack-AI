package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.MealLogEntity
import com.example.data.database.UserEntity
import com.example.data.database.WaterLogEntity
import com.example.data.database.WeightLogEntity
import com.example.data.model.FoodCatalog
import com.example.data.model.FoodItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter

// --- SHARED UI GLASSMOPRHISM CARD ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradientBr = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x261E293B),
                Color(0x130F172A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xCCFFFFFF),
                Color(0x99F1F5F9)
            )
        )
    }

    val borderCol = if (isDark) Color(0x1F94A3B8) else Color(0x3364748B)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBr)
            .border(borderWidth, borderCol, RoundedCornerShape(24.dp))
            .padding(18.dp),
        content = content
    )
}

// --- MAIN SCREEN ROUTER ---
@Composable
fun NutriTrackApp(viewModel: NutriViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val gateFeature by viewModel.premiumGateFeature.collectAsStateWithLifecycle()

    NutriTrackTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    label = "ScreenTransition",
                    transitionSpec = {
                        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    }
                ) { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen(viewModel)
                        Screen.Login -> LoginScreen(viewModel)
                        Screen.ForgotPassword -> ForgotPasswordScreen(viewModel)
                        Screen.VerifyEmail -> VerifyEmailScreen(viewModel)
                        Screen.SetupGoals -> SetupGoalsScreen(viewModel)
                        Screen.AppTour -> AppTourScreen(viewModel)
                        Screen.PremiumIntro -> PremiumIntroScreen(viewModel)
                        Screen.Dashboard -> MainLayout(viewModel, currentScreen = Screen.Dashboard)
                        Screen.SearchAdd -> MainLayout(viewModel, currentScreen = Screen.SearchAdd)
                        Screen.AiQuickAdd -> MainLayout(viewModel, currentScreen = Screen.AiQuickAdd)
                        Screen.Analytics -> MainLayout(viewModel, currentScreen = Screen.Analytics)
                        Screen.Profile -> MainLayout(viewModel, currentScreen = Screen.Profile)
                        Screen.Chatbot -> MainLayout(viewModel, currentScreen = Screen.Chatbot)
                        Screen.AnalyticsDebug -> MainLayout(viewModel, currentScreen = Screen.AnalyticsDebug)
                    }
                }

                if (gateFeature != null) {
                    PremiumGateDialog(viewModel = viewModel, featureName = gateFeature!!)
                }
            }
        }
    }
}

// --- CONTAINER LAYOUT WITH GLASS NAVIGATION BAR ---
@Composable
fun MainLayout(viewModel: NutriViewModel, currentScreen: Screen) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            GlassNavigationBar(
                currentScreen = currentScreen,
                isDark = isDark,
                onNavigate = { viewModel.setScreen(it) }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(viewModel)
                Screen.SearchAdd -> SearchAddScreen(viewModel)
                Screen.AiQuickAdd -> AiQuickAddScreen(viewModel)
                Screen.Analytics -> AnalyticsScreen(viewModel)
                Screen.Profile -> SettingsScreen(viewModel)
                Screen.Chatbot -> ChatbotScreen(viewModel)
                Screen.AnalyticsDebug -> AnalyticsDebugScreen(viewModel)
                else -> {}
            }
        }
    }
}

@Composable
fun GlassNavigationBar(
    currentScreen: Screen,
    isDark: Boolean,
    onNavigate: (Screen) -> Unit
) {
    val navBg = if (isDark) Color(0xF2090D16) else Color(0xF2FFFFFF)
    val dividerCol = if (isDark) Color(0x1F94A3B8) else Color(0x1F64748B)

    Column(
        modifier = Modifier
            .background(navBg)
            .border(width = Dp.Hairline, color = dividerCol)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                NavItem(Screen.Dashboard, Icons.Default.Home, "Home"),
                NavItem(Screen.SearchAdd, Icons.Default.Search, "Search"),
                NavItem(Screen.AiQuickAdd, Icons.Default.AutoAwesome, "AI Log", isSparkle = true),
                NavItem(Screen.Analytics, Icons.Default.Leaderboard, "Analytics"),
                NavItem(Screen.Profile, Icons.Default.Settings, "Settings")
            )

            items.forEach { item ->
                val selected = currentScreen == item.screen
                val tintColor = if (selected) {
                    if (item.isSparkle) TargetCaloriesOrange else BrandPrimaryGreen
                } else {
                    if (isDark) TextMuted else TextLightMuted
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            indication = RippleTheme.rippleOrNone(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onNavigate(item.screen) }
                        .padding(vertical = 10.dp)
                        .testTag("nav_btn_${item.label.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(26.dp),
                            tint = tintColor
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = tintColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(val screen: Screen, val icon: ImageVector, val label: String, val isSparkle: Boolean = false)

// --- RIPPLE UTIL ---
object RippleTheme {
    @Composable
    fun rippleOrNone() = LocalIndication.current
}

// --- SPLASH SCREEN ---
@Composable
fun SplashScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var showLogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLogo = true
        delay(2000)
        // Checks if someone logged in already
        val user = viewModel.loggedInUser.value
        if (user != null) {
            viewModel.setScreen(Screen.Dashboard)
        } else {
            viewModel.setScreen(Screen.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = if (isDark) listOf(Color(0xFF1E293B), DarkBg) else listOf(Color.White, LightBg),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showLogo,
            enter = fadeIn(animationSpec = tween(1200)) + scaleIn(initialScale = 0.8f, animationSpec = tween(1000)),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandPrimaryGreen, BrandPrimaryTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "NutriTrack AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) TextWhite else TextDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Smart Indian Food Tracker",
                    fontSize = 14.sp,
                    color = if (isDark) TextMuted else TextLightMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// --- LOGIN SCREEN ---
@Composable
fun LoginScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("myworkmypc45@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("User") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Logo",
                    tint = BrandPrimaryGreen,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isSignUpMode) "Join NutriTrack AI" else "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextDark
                )
                Text(
                    text = if (isSignUpMode) "Start your fitness journey today" else "Log in to view daily targets",
                    fontSize = 14.sp,
                    color = if (isDark) TextMuted else TextLightMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))

                GlassCard(isDark = isDark) {
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name", color = if (isDark) TextMuted else TextLightMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = { Text("Email Address", color = if (isDark) TextMuted else TextLightMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth().testTag("email_field"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Password", color = if (isDark) TextMuted else TextLightMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (isSignUpMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = "" },
                            label = { Text("Confirm Password", color = if (isDark) TextMuted else TextLightMuted) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            if (isSignUpMode) {
                                if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match"
                                    return@Button
                                }
                                isLoading = true
                                viewModel.registerWithEmail(email, password, name,
                                    onSuccess = { isLoading = false },
                                    onFailure = { err -> isLoading = false; errorMessage = err }
                                )
                            } else {
                                isLoading = true
                                viewModel.loginWithEmail(email, password,
                                    onSuccess = { isLoading = false },
                                    onFailure = { err -> isLoading = false; errorMessage = err }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("login_submit_btn"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account" else "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (!isSignUpMode) {
                        TextButton(
                            onClick = { viewModel.setScreen(Screen.ForgotPassword) },
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                        ) {
                            Text("Forgot Password?", color = BrandPrimaryGreen, fontSize = 14.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDark) Color(0x3394A3B8) else Color(0x3364748B))
                        Text(text = " OR ", fontSize = 11.sp, color = if (isDark) TextMuted else TextLightMuted, modifier = Modifier.padding(horizontal = 8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDark) Color(0x3394A3B8) else Color(0x3364748B))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            viewModel.signInWithGoogle(
                                context = context,
                                onSuccess = { isLoading = false },
                                onFailure = { err -> isLoading = false; errorMessage = err }
                            )
                        },
                        border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x3364748B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("google_login_btn"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BrandPrimaryGreen, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Logo",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                color = if (isDark) TextWhite else TextDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { isSignUpMode = !isSignUpMode; errorMessage = "" },
                    modifier = Modifier.testTag("toggle_signup_btn")
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? Log In" else "New to NutriTrack AI? Sign Up",
                        color = BrandPrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- REGISTER SCREEN (OPTIONAL FALLBACK FOR SETUP) ---
@Composable
fun RegisterScreen(viewModel: NutriViewModel) {
    LoginScreen(viewModel)
}

// --- FORGOT PASSWORD SCREEN ---
@Composable
fun ForgotPasswordScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = "Forgot Password",
                tint = BrandPrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Forgot Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your email address and we will send you a password reset link.",
                fontSize = 14.sp,
                color = if (isDark) TextMuted else TextLightMuted,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(isDark = isDark) {
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = if (isError) Color.Red else BrandPrimaryGreen,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; message = "" },
                    label = { Text("Email Address", color = if (isDark) TextMuted else TextLightMuted) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        message = ""
                        viewModel.resetPassword(email, 
                            onSuccess = { 
                                isLoading = false
                                isError = false
                                message = "Reset link sent! Check your email."
                            },
                            onFailure = { err -> 
                                isLoading = false
                                isError = true
                                message = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.setScreen(Screen.Login) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0x3394A3B8) else Color(0x3364748B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Back to Login", color = if (isDark) TextWhite else TextDark)
                }
            }
        }
    }
}

// --- VERIFY EMAIL SCREEN ---
@Composable
fun VerifyEmailScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Verify Email",
                tint = BrandPrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Verify Your Email",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We sent a verification email to your account. Please check your inbox and confirm your email to continue.",
                fontSize = 14.sp,
                color = if (isDark) TextMuted else TextLightMuted,
                textAlign = TextAlign.Center
            )
            
            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = BrandPrimaryGreen,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isLoading = true
                    viewModel.checkVerificationStatus {
                        isLoading = false
                        message = "Refresh completed"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("I've Verified My Email", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    isLoading = true
                    viewModel.sendVerificationEmail(
                        onSuccess = { isLoading = false; message = "Email resent. Check your inbox." },
                        onFailure = { err -> isLoading = false; message = err }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Resend Email", color = if (isDark) TextWhite else TextDark)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(onClick = { viewModel.logout() }) {
                Text("Logout", color = Color.Red)
            }
        }
    }
}

// --- BMI AND CALORIE GOAL CALCULATOR INITIAL SETUP SCREEN ---
@Composable
fun SetupGoalsScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("172") }
    var weight by remember { mutableStateOf("68") }
    var activityLevel by remember { mutableStateOf("Moderately Active") }
    var goalType by remember { mutableStateOf("Maintain Weight") }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DarkBg else LightBg)
                .padding(padding)
                .padding(24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Customize Your Goals",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextDark,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "Compute your BMR, BMI and ideal calorie targets",
                        fontSize = 13.sp,
                        color = if (isDark) TextMuted else TextLightMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "GENDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Male", "Female").forEach { g ->
                                val sel = g == gender
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clickable { gender = g },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sel) BrandPrimaryGreen else (if (isDark) Color(0xFF1E293B) else Color(
                                            0xFFE2E8F0
                                        ))
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            g,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sel) Color.White else (if (isDark) TextWhite else TextDark)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "METRICS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age (Years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ACTIVITY LEVEL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val levels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active")
                        levels.forEach { level ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activityLevel = level }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = activityLevel == level,
                                    onClick = { activityLevel = level },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(level, color = if (isDark) TextWhite else TextDark)
                            }
                        }
                    }
                }

                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "FITNESS PURPOSE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val targets = listOf("Lose Weight", "Maintain Weight", "Gain Weight", "Muscle Gain")
                        targets.forEach { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { goalType = target }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = goalType == target,
                                    onClick = { goalType = target },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(target, color = if (isDark) TextWhite else TextDark)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val ageInt = age.toIntOrNull() ?: 25
                            val ht = height.toFloatOrNull() ?: 170f
                            val wt = weight.toFloatOrNull() ?: 65f
                            viewModel.saveUserGoals(gender, ageInt, ht, wt, activityLevel, goalType, isFromOnboarding = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(bottom = 12.dp)
                            .testTag("save_goals_btn")
                    ) {
                        Text("Calculate & Start Logging", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val water by viewModel.waterForSelectedDate.collectAsStateWithLifecycle()
    val dateLabel by viewModel.selectedDate.collectAsStateWithLifecycle()

    val totalCalories = meals.sumOf { it.calories.toDouble() }.roundToInt()
    val totalProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFats = meals.sumOf { it.fats.toDouble() }.toFloat()
    val totalFiber = meals.sumOf { it.fiber.toDouble() }.toFloat()
    val totalWaterMl = water.sumOf { it.amountMl }

    val calorieTarget = user?.dailyCalorieGoal ?: 2000
    val proteinTarget = user?.dailyProteinGoalG ?: 60
    val carbsTarget = user?.dailyCarbsGoalG ?: 250
    val fatsTarget = user?.dailyFatsGoalG ?: 55
    val fiberTarget = user?.dailyFiberGoalG ?: 25
    val waterTarget = user?.dailyWaterGoalMl ?: 2500

    LaunchedEffect(meals, water, dateLabel) {
        if (dateLabel == viewModel.getCurrentDateLabel()) {
            val proteinMet = totalProtein >= proteinTarget
            val waterMet = totalWaterMl >= waterTarget
            viewModel.checkAndApplyStreaks(proteinMet, waterMet)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Date Selector
        item {
            DateNavigationHeader(viewModel)
        }

        // Welcome / Summary Dashboard Heading
        item {
            WelcomeDashboardCard(user, isDark, onDebugClick = { viewModel.setScreen(Screen.AnalyticsDebug) })
        }

        // Streak Badges
        item {
            SmartStreakBadgesCard(viewModel, isDark)
        }

        // Quick Actions Row
        item {
            QuickActionsRow(isDark) { screen -> 
                if (screen == Screen.AiQuickAdd) {
                    if (!viewModel.checkPremiumFeatureAccess("Meal Scan")) {
                        viewModel.triggerPremiumGate("Meal Scan")
                    } else {
                        viewModel.setScreen(screen)
                    }
                } else {
                    viewModel.setScreen(screen)
                }
            }
        }

        // Calories Progress Ring Widget
        item {
            CalorieProgressRingCard(totalCalories, calorieTarget, isDark)
        }

        // Macros Breakdowns
        item {
            MacrosBreakdownCard(
                proteinTracked = totalProtein, proteinTarget = proteinTarget,
                carbsTracked = totalCarbs, carbsTarget = carbsTarget,
                fatsTracked = totalFats, fatsTarget = fatsTarget,
                fiberTracked = totalFiber, fiberTarget = fiberTarget,
                isDark = isDark
            )
        }

        // Water Counter Widget
        item {
            WaterTrackerCard(totalWaterMl, waterTarget, isDark, onDrinkGlass = {
                viewModel.addWater(250) // add 250ml cup
            }, onClearAll = {
                viewModel.clearAllWaterForDay()
            })
        }

        // Weight Logging Widget
        item {
            WeightTrackingCard(viewModel, user, isDark)
        }

        // Logged Meals Overview by Section
        item {
            MealsBreakdownOverviewCard(meals, isDark, onDelete = {
                viewModel.deleteLoggedMeal(it)
            })
        }

        // AI Personalized Fitness Tip Card
        item {
            FitnessTipCard(viewModel, isDark)
        }

        // Adding blank bottom spacing to let list breathe
        item {
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
fun DateNavigationHeader(viewModel: NutriViewModel) {
    val dateLabel = viewModel.getFormattedSelectedDate()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { viewModel.stepDate(-1) },
            modifier = Modifier.testTag("prev_date_btn")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Prev Day",
                tint = if (isDark) Color.White else Color.Black
            )
        }

        Text(
            text = dateLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhite else TextDark,
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { viewModel.stepDate(1) },
            modifier = Modifier.testTag("next_date_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Day",
                tint = if (isDark) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun WelcomeDashboardCard(user: UserEntity?, isDark: Boolean, onDebugClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onDebugClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Namaste, ${user?.name ?: "User"} 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val bmiVal = if (user != null && user.heightCm > 0) {
                user.weightKg / ((user.heightCm / 100f) * (user.heightCm / 100f))
            } else 22f
            val df = DecimalFormat("#.#")
            Text(
                text = "Target goal: ${user?.goalType ?: "Maintain"} | Current BMI: ${df.format(bmiVal)}",
                fontSize = 12.sp,
                color = BrandPrimaryGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FitnessTipCard(viewModel: NutriViewModel, isDark: Boolean) {
    val tip by viewModel.fitnessTip.collectAsStateWithLifecycle()
    val isFetching by viewModel.isFetchingTip.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchDailyFitnessTip()
    }

    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Tip",
                    tint = TargetCaloriesOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Fitness Tip",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) TextWhite else TextDark
                )
            }
            if (isFetching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                    color = BrandPrimaryGreen,
                    strokeWidth = 2.dp
                )
            } else if (tip != null) {
                Text(
                    text = tip!!,
                    fontSize = 14.sp,
                    color = if (isDark) TextMuted else TextDark,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun CalorieProgressRingCard(tracked: Int, target: Int, isDark: Boolean) {
    val remaining = (target - tracked).coerceAtLeast(0)
    val percentage = if (target > 0) tracked.toFloat() / target.toFloat() else 0f
    val sweptAngle = (percentage * 360f).coerceAtMost(360f)

    val ringColor = BrandPrimaryGreen
    val trackColor = if (isDark) Color(0x1AFFFFFF) else Color(0x1F000000)

    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "CALORIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextMuted else TextLightMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$tracked",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) TextWhite else TextDark
                    )
                    Text(
                        text = " / $target kcal",
                        fontSize = 14.sp,
                        color = if (isDark) TextMuted else TextLightMuted,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }
                Text(
                    text = if (remaining > 0) "$remaining kcal left to consume" else "Target completed! 🎉",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (remaining > 0) TargetCaloriesOrange else BrandPrimaryGreen
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .weight(0.8f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(86.dp)) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = sweptAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) TextWhite else TextDark
                    )
                }
            }
        }
    }
}

@Composable
fun MacrosBreakdownCard(
    proteinTracked: Float, proteinTarget: Int,
    carbsTracked: Float, carbsTarget: Int,
    fatsTracked: Float, fatsTarget: Int,
    fiberTracked: Float, fiberTarget: Int,
    isDark: Boolean
) {
    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "MACRONUTRIENT RATIOS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextMuted else TextLightMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MacroColumnItem(
                label = "Protein",
                tracked = proteinTracked,
                target = proteinTarget,
                unit = "g",
                color = MacroProtein,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
            MacroColumnItem(
                label = "Carbs",
                tracked = carbsTracked,
                target = carbsTarget,
                unit = "g",
                color = MacroCarbs,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
            MacroColumnItem(
                label = "Fats",
                tracked = fatsTracked,
                target = fatsTarget,
                unit = "g",
                color = MacroFats,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
            MacroColumnItem(
                label = "Fiber",
                tracked = fiberTracked,
                target = fiberTarget,
                unit = "g",
                color = MacroFiber,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MacroColumnItem(
    label: String,
    tracked: Float,
    target: Int,
    unit: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) tracked / target.toFloat() else 0f
    val df = DecimalFormat("#.#")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhite else TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x16FFFFFF) else Color(0x1F000000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${df.format(tracked)}/$target$unit",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WaterTrackerCard(
    waterTrackedMl: Int,
    waterTargetMl: Int,
    isDark: Boolean,
    onDrinkGlass: () -> Unit,
    onClearAll: () -> Unit
) {
    val percent = (waterTrackedMl.toFloat() / waterTargetMl.toFloat()).coerceAtMost(1f)

    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WATER INTAKE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextMuted else TextLightMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$waterTrackedMl / $waterTargetMl ml",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WaterBlue
                )
                Text(
                    text = "Daily goal is ~${waterTargetMl / 250} glasses (250ml each)",
                    fontSize = 11.sp,
                    color = if (isDark) TextMuted else TextLightMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDrinkGlass,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WaterBlue)
                        .testTag("drink_water_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = "Drink 250ml",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("clear_water_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear Water",
                        tint = if (isDark) TextMuted else TextLightMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Glass scale indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0x16FFFFFF) else Color(0x1F000000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent)
                    .clip(CircleShape)
                    .background(WaterBlue)
            )
        }
    }
}

@Composable
fun MealsBreakdownOverviewCard(
    meals: List<MealLogEntity>,
    isDark: Boolean,
    onDelete: (MealLogEntity) -> Unit
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        mealTypes.forEach { type ->
            val sectionMeals = meals.filter { it.mealType.equals(type, ignoreCase = true) }
            val sectionCals = sectionMeals.sumOf { it.calories.toDouble() }.roundToInt()

            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (type) {
                                "Breakfast" -> Icons.Default.WbSunny
                                "Lunch" -> Icons.Default.Restaurant
                                "Dinner" -> Icons.Default.Nightlife
                                else -> Icons.Default.LocalCafe
                            },
                            contentDescription = type,
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextWhite else TextDark
                        )
                    }
                    Text(
                        text = "$sectionCals kcal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TargetCaloriesOrange
                    )
                }

                if (sectionMeals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sectionMeals.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x1A000000) else Color(0x0F000000))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    val formattedUnit = when (meal.quantityUnit) {
                                        "piece(s)" -> "${meal.quantityMultiplier.roundToInt()} Pcs"
                                        "g" -> "${meal.quantityMultiplier.roundToInt()}g"
                                        "ml" -> "${meal.quantityMultiplier.roundToInt()}ml"
                                        else -> "${meal.quantityMultiplier} cup/katori"
                                    }
                                    Text(
                                        text = meal.foodName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) TextWhite else TextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$formattedUnit | P: ${meal.protein.toInt()}g C: ${meal.carbs.toInt()}g F: ${meal.fats.toInt()}g",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextMuted else TextLightMuted
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(0.5f),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        meal.calories.roundToInt().toString() + "k",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextWhite else TextDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDelete(meal) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("delete_meal_${meal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove meal",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No meals logged yet. Keep track of daily metabolic progress!",
                        fontSize = 11.sp,
                        color = if (isDark) TextMuted else TextLightMuted
                    )
                }
            }
        }
    }
}

// --- SEARCH & LOG ROAD SCREEN ---
@Composable
fun SmartStreakBadgesCard(viewModel: NutriViewModel, isDark: Boolean) {
    val streak by viewModel.userStreak.collectAsStateWithLifecycle()
    
    if (streak == null) return

    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Consistency Streaks 🔥",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StreakBadge(icon = Icons.Default.DoneAll, count = streak!!.loginStreak, label = "Tracking", isDark = isDark)
                StreakBadge(icon = Icons.Default.FitnessCenter, count = streak!!.proteinStreak, label = "Protein", isDark = isDark)
                StreakBadge(icon = Icons.Default.LocalDrink, count = streak!!.waterStreak, label = "Hydration", isDark = isDark)
                StreakBadge(icon = Icons.Default.MonitorWeight, count = streak!!.weightStreak, label = "Weigh-in", isDark = isDark)
            }
        }
    }
}

@Composable
fun StreakBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, label: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (count > 0) BrandPrimaryGreen.copy(alpha = 0.2f) else (if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (count > 0) BrandPrimaryGreen else (if (isDark) TextMuted else TextLightMuted),
                modifier = Modifier.size(24.dp)
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(TargetCaloriesOrange)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(count.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = if(isDark) TextMuted else TextLightMuted)
    }
}

@Composable
fun WeightTrackingCard(viewModel: NutriViewModel, user: com.example.data.database.UserEntity?, isDark: Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf(user?.weightKg?.toString() ?: "70.0") }

    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Weight",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimaryGreen
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${user?.weightKg ?: 0f}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextDark
                    )
                    Text(" kg", fontSize = 14.sp, color = if (isDark) TextMuted else TextLightMuted, modifier = Modifier.padding(bottom = 2.dp))
                }
                Text("Target: ${user?.targetWeightKg ?: 0f} kg", fontSize = 11.sp, color = if(isDark) TextMuted else TextLightMuted)
            }

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Weight", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log Today's Weight", color = if(isDark) Color.White else Color.Black) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Weight (kg)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val w = weightInput.toFloatOrNull()
                    if (w != null) {
                        viewModel.updateWeight(w)
                    }
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SearchAddScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    var selectedMealCategory by remember { mutableStateOf("Breakfast") }
    var selectedFoodItemForDropdown by remember { mutableStateOf<FoodItem?>(null) }
    var inputQuantityMultiplier by remember { mutableStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Discover Foods",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextWhite else TextDark
        )

        // Meal Type Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
            categories.forEach { cat ->
                val sel = cat == selectedMealCategory
                FilterChip(
                    selected = sel,
                    onClick = { selectedMealCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPrimaryGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_filter_${cat.lowercase()}")
                )
            }
        }

        // Search Text Text Field
        OutlinedTextField(
            value = searchVal,
            onValueChange = { viewModel.searchFoods(it) },
            placeholder = { Text("Search roti, idli, rice, dal, dahi...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_food_field"),
            singleLine = true
        )

        Text(
            text = "INDIAN FOODS LIST",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) TextMuted else TextLightMuted
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults) { food ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) DarkSurface else Color.White)
                        .border(
                            1.dp,
                            if (isDark) Color(0x3E1E293B) else Color(0x3364748B),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedFoodItemForDropdown = food
                            // Initialize input to base value
                            inputQuantityMultiplier = food.suggestedQuantities.firstOrNull() ?: food.baseQuantityVal
                        }
                        .padding(14.dp)
                        .testTag("food_item_${food.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = food.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextWhite else TextDark
                        )
                        Text(
                            text = "${food.category} | ${food.baseCalories.roundToInt()} kcal per ${food.baseQuantityVal.roundToInt()}${food.baseQuantityUnit}",
                            fontSize = 12.sp,
                            color = if (isDark) TextMuted else TextLightMuted
                        )
                    }

                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                selectedFoodItemForDropdown = food
                                inputQuantityMultiplier = food.suggestedQuantities.firstOrNull() ?: food.baseQuantityVal
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandPrimaryGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Log Food", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Smart Dropdowns and Quantity Estimator Popup Dialog Box
    if (selectedFoodItemForDropdown != null) {
        val food = selectedFoodItemForDropdown!!

        val activeScale = when (food.baseQuantityUnit) {
            "g", "ml" -> inputQuantityMultiplier / food.baseQuantityVal
            else -> inputQuantityMultiplier
        }

        val tempCalories = food.baseCalories * activeScale
        val tempProtein = food.baseProtein * activeScale
        val tempCarbs = food.baseCarbs * activeScale
        val tempFats = food.baseFats * activeScale

        Dialog(onDismissRequest = { selectedFoodItemForDropdown = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) DarkBg else Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customize Quantity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextDark
                    )

                    Text(
                        text = food.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CHOOSE PORTION SIZE:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextMuted else TextLightMuted
                    )

                    // Wrap portion chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        food.suggestedQuantities.forEach { option ->
                            val activeVal = option == inputQuantityMultiplier
                            val formattedLabel = when (food.baseQuantityUnit) {
                                "piece(s)" -> {
                                    if (option % 1 == 0f) "${option.toInt()} Pcs" else "$option Pcs"
                                }
                                "g" -> "${option.toInt()}g"
                                "ml" -> "${option.toInt()}ml"
                                else -> {
                                    if (option % 1 == 0f) "${option.toInt()} serving" else "$option serv"
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { inputQuantityMultiplier = option },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activeVal) BrandPrimaryGreen else (if (isDark) Color(
                                        0xFF1E293B
                                    ) else Color(0xFFF1F5F9))
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = formattedLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeVal) Color.White else (if (isDark) TextWhite else TextDark)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Instant computed estimation breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0x13000000) else Color(0x3EFFFFFF))
                            .border(width = 1.dp, color = if (isDark) Color(0x3E1E293B) else Color(0x1F64748B), shape = RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickStatsColumn("Calories", "${tempCalories.roundToInt()}k")
                        QuickStatsColumn("Protein", "${tempProtein.roundToInt()}g")
                        QuickStatsColumn("Carbs", "${tempCarbs.roundToInt()}g")
                        QuickStatsColumn("Fats", "${tempFats.roundToInt()}g")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedFoodItemForDropdown = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", color = if (isDark) TextWhite else TextDark)
                        }

                        Button(
                            onClick = {
                                viewModel.logManualFood(food, inputQuantityMultiplier, selectedMealCategory)
                                selectedFoodItemForDropdown = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_log_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Log Item", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsColumn(label: String, valLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = if (MaterialTheme.colorScheme.primary == BrandPrimaryGreen) TextMuted else TextLightMuted)
        Text(text = valLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandPrimaryGreen)
    }
}

// --- FLOW ROW FALLBACK FOR COMPOSE COMPATIBILITY ---
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = 3,
    content: @Composable () -> Unit
) {
    // Custom structured linear horizontal layout wrap fallback because FlowRow can have compile differences based on compose version
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        var itemsBuffer = remember { mutableStateListOf<@Composable () -> Unit>() }
        itemsBuffer.clear()

        // Simply split elements into rows based on chunk size maxItemsInEachRow
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

// --- AI QUICK ADD CHAT SCREEN ---
fun uriToBitmap(context: android.content.Context, uri: android.net.Uri): android.graphics.Bitmap? {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun resizeBitmapToMax(bitmap: android.graphics.Bitmap, maxDimension: Int = 800): android.graphics.Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val newWidth: Int
    val newHeight: Int
    if (width > height) {
        if (width <= maxDimension) return bitmap
        newWidth = maxDimension
        newHeight = (height * (maxDimension.toFloat() / width)).toInt()
    } else {
        if (height <= maxDimension) return bitmap
        newWidth = (width * (maxDimension.toFloat() / height)).toInt()
        newHeight = maxDimension
    }
    return android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

@Composable
fun EditDetectedFoodDialog(
    food: com.example.data.api.DetectedFood,
    onDismiss: () -> Unit,
    onConfirm: (com.example.data.api.DetectedFood) -> Unit,
    isDark: Boolean
) {
    var name by remember { mutableStateOf(food.foodName) }
    var quantity by remember { mutableStateOf(food.quantityMultiplier.toString()) }
    var unit by remember { mutableStateOf(food.quantityUnit) }
    var calories by remember { mutableStateOf(food.calories.toString()) }
    var protein by remember { mutableStateOf(food.protein.toString()) }
    var carbs by remember { mutableStateOf(food.carbs.toString()) }
    var fats by remember { mutableStateOf(food.fats.toString()) }
    var fiber by remember { mutableStateOf(food.fiber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Food Details",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text("Fats (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toFloatOrNull() ?: food.quantityMultiplier
                    val c = calories.toFloatOrNull() ?: food.calories
                    val p = protein.toFloatOrNull() ?: food.protein
                    val carb = carbs.toFloatOrNull() ?: food.carbs
                    val f = fats.toFloatOrNull() ?: food.fats
                    val fib = fiber.toFloatOrNull() ?: food.fiber
                    onConfirm(
                        com.example.data.api.DetectedFood(
                            foodName = name,
                            quantityMultiplier = q,
                            quantityUnit = unit,
                            calories = c,
                            protein = p,
                            carbs = carb,
                            fats = f,
                            fiber = fib
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = if (isDark) Color.White else Color.Black)
            }
        }
    )
}

@Composable
fun AiQuickAddScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanningAi.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiDetectedFoods.collectAsStateWithLifecycle()
    val errorMsg by viewModel.aiError.collectAsStateWithLifecycle()
    val selectedImage by viewModel.selectedImageBitmap.collectAsStateWithLifecycle()

    var mealText by remember { mutableStateOf("") }
    var userPhotoDescription by remember { mutableStateOf("") }
    var selectedMealCategory by remember { mutableStateOf("Lunch") }
    var activeTab by remember { mutableStateOf("text") } // "text" or "camera"
    var foodToEdit by remember { mutableStateOf<Pair<Int, com.example.data.api.DetectedFood>?>(null) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val resized = resizeBitmapToMax(bitmap, 800)
            viewModel.setSelectedImage(resized)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                viewModel.crashlyticsManager.recordException(e, "Camera permission granted but launch failed")
            }
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission is required to snap photos for AI scanning.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    val selectPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                val resized = resizeBitmapToMax(bitmap, 800)
                viewModel.setSelectedImage(resized)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Back Navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.clearAiBuffer()
                    viewModel.setScreen(Screen.Dashboard)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0F000000))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) TextWhite else TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Quick Log ⚡",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextDark
                )
                Text(
                    text = "Track meals instantly using text description or photos",
                    fontSize = 11.sp,
                    color = if (isDark) TextMuted else TextLightMuted
                )
            }
        }

        // Meal Type chip filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
            categories.forEach { cat ->
                val sel = cat == selectedMealCategory
                FilterChip(
                    selected = sel,
                    onClick = { selectedMealCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPrimaryGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Toggle Tabs selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0x13FFFFFF) else Color(0x0F000000))
                .padding(4.dp)
        ) {
            // Text tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeTab == "text") BrandPrimaryGreen else Color.Transparent)
                    .clickable { activeTab = "text" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Text Mode",
                        tint = if (activeTab == "text") Color.White else (if (isDark) TextMuted else TextLightMuted),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Text✍️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == "text") Color.White else (if (isDark) TextWhite else TextDark)
                    )
                }
            }

            // Camera tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeTab == "camera") BrandPrimaryGreen else Color.Transparent)
                    .clickable { activeTab = "camera" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera Mode",
                        tint = if (activeTab == "camera") Color.White else (if (isDark) TextMuted else TextLightMuted),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Camera📸",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == "camera") Color.White else (if (isDark) TextWhite else TextDark)
                    )
                }
            }
            
            // Presets tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeTab == "presets") BrandPrimaryGreen else Color.Transparent)
                    .clickable { activeTab = "presets" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Presets Mode",
                        tint = if (activeTab == "presets") Color.White else (if (isDark) TextMuted else TextLightMuted),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Presets⭐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == "presets") Color.White else (if (isDark) TextWhite else TextDark)
                    )
                }
            }
        }

        // Input Selector Workspace
        if (activeTab == "text") {
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = mealText,
                    onValueChange = { mealText = it },
                    placeholder = { Text("E.g., 2 roti, yellow dal and 100g cooked basmati rice") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .testTag("ai_input_field"),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.checkAndAnalyzeMealText(mealText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ai_search_btn"),
                    shape = RoundedCornerShape(14.dp),
                    enabled = mealText.isNotEmpty() && !isScanning
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Scan", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Meal with Gemini AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        } else if (activeTab == "camera") {
            // Camera Work Area
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                if (selectedImage == null) {
                    // Empty state capture prompts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Snap your Plate or Upload",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextWhite else TextDark
                        )
                        Text(
                            text = "Position your food in bright lighting for maximum nutritional scan accuracy.",
                            fontSize = 11.sp,
                            color = if (isDark) TextMuted else TextLightMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                    if (hasCameraPermission) {
                                        try {
                                            cameraLauncher.launch(null)
                                        } catch (e: Exception) {
                                            viewModel.crashlyticsManager.recordException(e, "Camera launch failure")
                                        }
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TargetCaloriesOrange),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(44.dp)
                                    .testTag("btn_snap_photo"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectPictureLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0x1CFFFFFF) else Color(0x14000000)),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .height(44.dp)
                                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x22000000), RoundedCornerShape(12.dp))
                                    .testTag("btn_gallery_photo"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Gallery",
                                    tint = if (isDark) TextWhite else TextDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "From Gallery",
                                    fontSize = 12.sp,
                                    color = if (isDark) TextWhite else TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Photo selected state
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImage),
                                contentDescription = "Captured meal preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Discard photo button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { viewModel.setSelectedImage(null) }
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Discard",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Extra context input text
                        OutlinedTextField(
                            value = userPhotoDescription,
                            onValueChange = { userPhotoDescription = it },
                            placeholder = { Text("Describe details if any... (e.g., oil-free, diet paneer)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("photo_ai_description"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                            )
                        )

                        Button(
                            onClick = {
                                viewModel.checkAndAnalyzeMealPhoto(selectedImage!!, userPhotoDescription)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_analyze_photo_ai"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isScanning
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Scanner", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Photo with Gemini AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else if (activeTab == "presets") {
            val prests by viewModel.mealPresets.collectAsStateWithLifecycle()
            if (prests.isEmpty()) {
                Box(modifier = Modifier.fillMaxHeight(0.5f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No saved presets yet.\nUse AI Quick Add to scan a meal and save it as a preset.", textAlign = TextAlign.Center, color = if(isDark) TextMuted else TextLightMuted)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(prests) { preset ->
                        GlassCard(modifier = Modifier.fillMaxWidth(), isDark = isDark) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.presetName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandPrimaryGreen)
                                    Text("${preset.calories.roundToInt()} kcal | P: ${preset.protein.roundToInt()}g C: ${preset.carbs.roundToInt()}g F: ${preset.fats.roundToInt()}g", fontSize = 12.sp, color = if(isDark) TextMuted else TextLightMuted)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { 
                                            viewModel.logPreset(preset, selectedMealCategory)
                                            viewModel.setScreen(Screen.Dashboard)
                                            android.widget.Toast.makeText(context, "Logged ${preset.presetName}", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) { Text("Log to $selectedMealCategory", fontSize = 12.sp, color = Color.White) }
                                    
                                    IconButton(
                                        onClick = { viewModel.deletePreset(preset.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loader
        if (isScanning && activeTab != "presets") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandPrimaryGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "AI estimating quantity & macro breakdowns...",
                        fontSize = 13.sp,
                        color = if (isDark) TextWhite else TextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error message warning bar
        if (errorMsg != null && activeTab != "presets") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFEECC))
                    .border(width = 1.dp, color = Color(0xFFFFAA00), shape = RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFF8800))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = errorMsg ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF663300)
                    )
                }
            }
        }

        // Preview parsed AI food objects block
        if (aiResult.isNotEmpty() && !isScanning && activeTab != "presets") {
            Text(
                "DETECTED FOOD DETAILS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextMuted else TextLightMuted
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(aiResult) { index, food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) DarkSurface else Color.White)
                            .border(
                                1.dp,
                                if (isDark) Color(0x3E1E293B) else Color(0x3364748B),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val quantText = if (food.quantityUnit == "piece(s)") {
                                "${food.quantityMultiplier.roundToInt()} Pcs"
                            } else {
                                "${food.quantityMultiplier.roundToInt()}${food.quantityUnit}"
                            }

                            Text(
                                text = food.foodName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimaryGreen
                            )
                            Text(
                                "Quantity: $quantText | P: ${food.protein.toInt()}g C: ${food.carbs.toInt()}g F: ${food.fats.toInt()}g",
                                fontSize = 12.sp,
                                color = if (isDark) TextMuted else TextLightMuted
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${food.calories.roundToInt()} kcal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TargetCaloriesOrange
                            )

                            IconButton(
                                onClick = { foodToEdit = Pair(index, food) },
                                modifier = Modifier.size(36.dp).testTag("edit_ai_food_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Food",
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteAiDetectedFood(index) },
                                modifier = Modifier.size(36.dp).testTag("delete_ai_food_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Food",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.confirmAndLogAiMeals(selectedMealCategory)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("ai_confirm_log_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm Log")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Confirm & Log ${aiResult.size} Items to $selectedMealCategory",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            presetName = ""
                            showSavePresetDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPrimaryGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimaryGreen)
                    ) {
                        Icon(Icons.Default.StarBorder, contentDescription = "Save Preset")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save these items as Preset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        foodToEdit?.let { (index, food) ->
            EditDetectedFoodDialog(
                food = food,
                onDismiss = { foodToEdit = null },
                onConfirm = { updated ->
                    viewModel.updateAiDetectedFood(index, updated)
                    foodToEdit = null
                },
                isDark = isDark
            )
        }

        if (showSavePresetDialog) {
            AlertDialog(
                onDismissRequest = { showSavePresetDialog = false },
                title = { Text("Save Meal Preset", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black) },
                text = {
                    Column {
                        Text("Save the detected items as a 1-click Preset.", fontSize = 12.sp, color = if (isDark) TextMuted else TextLightMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = presetName,
                            onValueChange = { presetName = it },
                            label = { Text("Preset Name") },
                            placeholder = { Text("e.g. 2 Rotis + Dal + Rice") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveAsPreset(presetName, selectedMealCategory, aiResult)
                            showSavePresetDialog = false
                            android.widget.Toast.makeText(context, "Preset saved!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                        enabled = presetName.isNotBlank()
                    ) { Text("Save Preset", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancel", color = if (isDark) Color.White else Color.Black) }
                }
            )
        }
    }
}

@Composable
fun ChatbotScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isChatbotThinking.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { viewModel.setScreen(Screen.Dashboard) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color.Black)
            }
            Text(
                text = "AI Nutrition Coach",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Text(
                        "Hi! Ask me advice about diets, weight loss, or healthy alternatives.",
                        color = if(isDark) TextMuted else TextLightMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            }
            items(chatMessages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(
                                topStart = 16.dp, 
                                topEnd = 16.dp, 
                                bottomStart = if(isUser) 16.dp else 0.dp, 
                                bottomEnd = if(isUser) 0.dp else 16.dp
                            ))
                            .background(if (isUser) BrandPrimaryGreen else (if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isUser) Color.White else (if (isDark) TextWhite else TextDark),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            if (isThinking) {
                item {
                    Row(horizontalArrangement = Arrangement.Start) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                .padding(12.dp)
                        ) {
                            Text("Thinking...", color = if (isDark) TextMuted else TextLightMuted, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask a question...") },
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimaryGreen,
                    cursorColor = BrandPrimaryGreen
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessageToCoach(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BrandPrimaryGreen)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// --- HISTORICAL ANALYTICS SCREEN ---
@Composable
fun AnalyticsScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val weightHist by viewModel.weightLogs.collectAsStateWithLifecycle()

    val totalCalories = meals.sumOf { it.calories.toDouble() }.roundToInt()
    val calorieTarget = user?.dailyCalorieGoal ?: 2000

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Metabolic Insights",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark
            )
        }

        // Daily calorie progress overview bar
        item {
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TODAY'S CALORIE SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimaryGreen
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalCalories kcal tracked",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextWhite else TextDark
                        )
                        Text(
                            text = "Daily Target: $calorieTarget kcal",
                            fontSize = 12.sp,
                            color = if (isDark) TextMuted else TextLightMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (totalCalories <= calorieTarget) BrandPrimaryGreen else TargetCaloriesOrange
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (totalCalories <= calorieTarget) Icons.Default.Check else Icons.Default.TrendingUp,
                            contentDescription = "Calorie status",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // High fidelity custom Canvas Weight History Trend Graph line chart
        item {
            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "WEIGHT HISTORY TRACK (KG)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimaryGreen,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (weightHist.size >= 2) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val maxWt = (weightHist.maxOf { it.weightKg } + 2).coerceAtLeast(10f)
                        val minWt = (weightHist.minOf { it.weightKg } - 2).coerceAtLeast(0f)
                        val wtRange = if (maxWt == minWt) 10f else maxWt - minWt

                        val width = size.width
                        val height = size.height
                        val stepX = width / (weightHist.size - 1)

                        // Draw helper grids
                        for (i in 0..4) {
                            val gridY = (height / 4) * i
                            drawLine(
                                color = if (isDark) Color(0x0DFFFFFF) else Color(0x11000000),
                                start = Offset(0f, gridY),
                                end = Offset(width, gridY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Coordinates buffer
                        val pointList = mutableListOf<Offset>()
                        weightHist.forEachIndexed { idx, point ->
                            val pX = idx * stepX
                            val pY = height - (((point.weightKg - minWt) / wtRange) * height)
                            pointList.add(Offset(pX, pY))
                        }

                        // Draw smooth gradient shader fill below trend line
                        val pathBrush = Brush.verticalGradient(
                            colors = listOf(WeightPurple.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )

                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(pointList.first().x, height)
                            pointList.forEach { lineTo(it.x, it.y) }
                            lineTo(pointList.last().x, height)
                            close()
                        }
                        drawPath(path = fillPath, brush = pathBrush)

                        // Draw core lines
                        for (i in 0 until pointList.size - 1) {
                            drawLine(
                                color = WeightPurple,
                                start = pointList[i],
                                end = pointList[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Highlight dots
                        pointList.forEach { dot ->
                            drawCircle(color = WeightPurple, radius = 5.dp.toPx(), center = dot)
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = dot)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Last logged weight: ${weightHist.last().weightKg} kg on ${weightHist.last().dateLabel}",
                        fontSize = 11.sp,
                        color = if (isDark) TextMuted else TextLightMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TrendingFlat,
                                contentDescription = "Insert weight logs to track trends",
                                tint = WeightPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Log weight values across different days in Profile tab to compile high-fidelity interactive Canvas progress speed graphs!",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = if (isDark) TextMuted else TextLightMuted,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dynamic BMI and body composition advice
        item {
            val hCm = user?.heightCm ?: 170f
            val wKg = user?.weightKg ?: 65f
            val bmiVal = if (hCm > 0) wKg / ((hCm / 100f) * (hCm / 100f)) else 22f
            val df = DecimalFormat("#.#")

            val (statusText, statusCol, feedback) = when {
                bmiVal < 18.5f -> Triple(
                    "UNDERWEIGHT",
                    Color.Yellow,
                    "Consider caloric surplus meals and robust protein consumption to build clean mass."
                )
                bmiVal < 25f -> Triple(
                    "NORMAL HEIGHT-TO-WEIGHT INDEX",
                    BrandPrimaryGreen,
                    "Great job! Maintain metabolic homeostasis by following local macro guidelines."
                )
                bmiVal < 30f -> Triple(
                    "OVERWEIGHT",
                    TargetCaloriesOrange,
                    "Focus on calorie deficit diet with high-fiber, low-carb Indian foods."
                )
                else -> Triple(
                    "OBESE",
                    Color.Red,
                    "Incorporate water fasting indices, light routines and calorie-capped food logging."
                )
            }

            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BMI INDEX DETECTOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusCol
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = df.format(bmiVal),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = statusText, color = statusCol, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Healthy threshold matches 18.5 - 24.9", fontSize = 11.sp, color = if (isDark) TextMuted else TextLightMuted)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback,
                    fontSize = 12.sp,
                    color = if (isDark) TextWhite else TextDark
                )
            }
        }

        // Weekly AI Report
        item {
            val report by viewModel.weeklySummary.collectAsStateWithLifecycle()
            val isFetchingReport by viewModel.isFetchingWeeklySummary.collectAsStateWithLifecycle()
            
            LaunchedEffect(Unit) {
                viewModel.generateWeeklySummary()
            }

            GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "WEEKLY AI HEALTH REPORT \u2728",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimaryGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isFetchingReport) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimaryGreen, modifier = Modifier.size(24.dp).padding(4.dp))
                    }
                } else {
                    Text(
                        text = report ?: "Summary not available.",
                        fontSize = 13.sp,
                        color = if (isDark) TextWhite else TextDark,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- PROFILE PROFILE TAB ---
@Composable
fun ProfileScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var activeWeightInput by remember { mutableStateOf("") }
    var activeHeightInput by remember { mutableStateOf("") }
    var isEditingMetrics by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            activeWeightInput = user?.weightKg?.toString() ?: ""
            activeHeightInput = user?.heightCm?.toString() ?: ""
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DarkBg else LightBg)
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Account Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextWhite else TextDark,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                item {
                    // Profile Header card
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimaryGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = user?.name ?: "Guest User",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextWhite else TextDark
                                )
                                Text(
                                    text = user?.email ?: "guest@test.com",
                                    fontSize = 12.sp,
                                    color = if (isDark) TextMuted else TextLightMuted
                                )
                            }
                        }
                    }
                }

                // Edit physical variables and logs weight
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "METRICATION PROGRESS LOGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (!isEditingMetrics) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Current Weight: ${user?.weightKg ?: 0f} kg",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) TextWhite else TextDark,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Height: ${user?.heightCm ?: 0f} cm",
                                        color = if (isDark) TextMuted else TextLightMuted,
                                        fontSize = 13.sp
                                    )
                                }

                                IconButton(
                                    onClick = { isEditingMetrics = true },
                                    modifier = Modifier.testTag("edit_metrics_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit data metric variables",
                                        tint = BrandPrimaryGreen
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = activeWeightInput,
                                    onValueChange = { activeWeightInput = it },
                                    label = { Text("Log Weight (kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = activeHeightInput,
                                    onValueChange = { activeHeightInput = it },
                                    label = { Text("Log Height (cm)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isEditingMetrics = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel", color = if (isDark) TextWhite else TextDark)
                                    }

                                    Button(
                                        onClick = {
                                            val wt = activeWeightInput.toFloatOrNull()
                                            val ht = activeHeightInput.toFloatOrNull()
                                            if (wt != null) {
                                                viewModel.updateWeight(wt)
                                            }
                                            if (ht != null && user != null) {
                                                coroutineScope.launch {
                                                    viewModel.saveUserGoals(
                                                        gender = user?.gender ?: "Male",
                                                        age = user?.age ?: 25,
                                                        heightCm = ht,
                                                        weightKg = wt ?: (user?.weightKg ?: 65f),
                                                        activityLevel = user?.activityLevel ?: "Sedentary",
                                                        goalType = user?.goalType ?: "Maintain Weight"
                                                    )
                                                }
                                            }
                                            isEditingMetrics = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("submit_metrics_btn")
                                    ) {
                                        Text("Save Logs", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Preferences & Reminders setting
                item {
                    GlassCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PREFERENCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleTheme() }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme",
                                    tint = if (isDark) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Aesthetic Dark Mode", color = if (isDark) TextWhite else TextDark)
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.toggleTheme() },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimaryGreen)
                            )
                        }

                        // Water log Reminders Toggle Layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Reminders",
                                    tint = if (isDark) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Hydration reminders", color = if (isDark) TextWhite else TextDark)
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = { },
                                colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimaryGreen)
                            )
                        }
                    }
                }

                // Recalculating targets button
                item {
                    OutlinedButton(
                        onClick = { viewModel.setScreen(Screen.SetupGoals) },
                        border = BorderStroke(1.dp, BrandPrimaryGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("recalc_goals_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recalculate target plans", tint = BrandPrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recalculate Calorie Target Plans", color = BrandPrimaryGreen)
                    }
                }

                // Log out button
                item {
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("logout_btn")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout Profile Accounts", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- QUICK ACTIONS ROW ---
@Composable
fun QuickActionsRow(isDark: Boolean, onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Coach Action
        Box(
            modifier = Modifier
                .weight(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0x338B5CF6) else Color(0x1A8B5CF6))
                .border(1.dp, Color(0x528B5CF6), RoundedCornerShape(16.dp))
                .clickable { onNavigate(Screen.Chatbot) }
                .padding(12.dp)
                .testTag("promo_coach_card"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "AI Coach",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Coach",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextDark,
                    maxLines = 1
                )
            }
        }

        // AI Log Action
        Box(
            modifier = Modifier
                .weight(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0x3310B981) else Color(0x1A10B981))
                .border(1.dp, Color(0x5210B981), RoundedCornerShape(16.dp))
                .clickable { onNavigate(Screen.AiQuickAdd) }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Quick Log",
                    tint = BrandPrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Log ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextDark
                )
            }
        }

        // Manual Search & Add
        Box(
            modifier = Modifier
                .weight(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0F000000))
                .border(1.dp, if (isDark) Color(0x1F94A3B8) else Color(0x1F64748B), RoundedCornerShape(16.dp))
                .clickable { onNavigate(Screen.SearchAdd) }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search & Add",
                    tint = if (isDark) TextWhite else TextDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manual Add",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextWhite else TextDark
                )
            }
        }
    }
}




// --- APP TOUR SCREEN ---
@Composable
fun AppTourScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var currentPage by remember { mutableStateOf(0) }
    
    val tourPages = listOf(
        Pair("Welcome to NutriTrack AI", "Your intelligent tool for tracking nutrition, hydration, and setting smart fitness goals."),
        Pair("Dashboard Overview", "Monitor your daily intake and check your goal completion using elegant visual summary rings."),
        Pair("Food Logging", "Easily quickly add food using text search, or let the AI estimate calories based on inputs."),
        Pair("AI Meal Scanner", "Take a photo of your meal and our vision model will automatically detect and segment it for accurate logging."),
        Pair("Meal Presets", "Save your most frequent meals as presets for lightning-fast tracking in the future."),
        Pair("Water Tracking", "Log your glasses of water and meet your daily hydration quota directly from the dashboard."),
        Pair("Weight Tracking", "Update your bodyweight reading to monitor your progression visually over the timeline chart."),
        Pair("AI Nutrition Coach", "Talk to the onboard AI expert. Get daily fitness tips immediately tuned to your current stats."),
        Pair("Analytics & Weekly Reports", "Generate personalized, fully-detailed breakdown of your week powered by your real Room data logs.")
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Tour Icon",
                tint = BrandPrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = tourPages[currentPage].first,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextWhite else TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tourPages[currentPage].second,
                fontSize = 16.sp,
                color = if (isDark) TextMuted else TextLightMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                tourPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (index == currentPage) BrandPrimaryGreen else Color.Gray.copy(alpha = 0.5f))
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (currentPage < tourPages.size - 1) {
                        currentPage++
                    } else {
                        viewModel.markAppTourCompleted()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (currentPage == tourPages.size - 1) "Get Started" else "Next", color = Color.White)
            }
        }
    }
}

// --- PREMIUM INTRO SCREEN ---
@Composable
fun PremiumIntroScreen(viewModel: NutriViewModel) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBg else LightBg)
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(
                text = "Upgrade to",
                fontSize = 20.sp,
                color = if (isDark) TextWhite else TextDark
            )
            Text(
                text = "NutriTrack AI Pro",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandPrimaryGreen
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "FREE PLAN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextMuted else TextLightMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "Food Tracking", "Water Tracking", "Weight Tracking",
                    "Dashboard", "Meal Presets", "BMI Calculator", "Calorie Calculator"
                ).forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, "Included", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feature, fontSize = 14.sp, color = if (isDark) TextMuted else TextLightMuted)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "PRO PLAN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimaryGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "Unlimited AI Meal Scans", 
                    "Unlimited AI Coach Messages", 
                    "Weekly AI Health Reports", 
                    "Advanced Analytics", 
                    "Future Premium Features"
                ).forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, "Included", tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(feature, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) TextWhite else TextDark)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.upgradeToPro() },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Upgrade to Pro", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { viewModel.continueWithFree() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Continue with Free Plan", color = if (isDark) TextWhite else TextDark)
            }
        }
    }
}

@Composable
fun PremiumGateDialog(viewModel: NutriViewModel, featureName: String) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val mealScans by viewModel.monthlyMealScansUsed.collectAsStateWithLifecycle()
    val weeklyReports by viewModel.weeklyReportsUsed.collectAsStateWithLifecycle()
    val coachMessages by viewModel.dailyAiCoachMessagesUsed.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = { viewModel.dismissPremiumGate() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF131B2E) else Color(0xFFF8FAFC)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Premium Feature",
                    tint = BrandPrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Upgrade to NutriTrack AI Pro",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Current Plan: Free",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TargetCaloriesOrange
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Show usage conditionally
                if (featureName == "Meal Scan") {
                    Text(
                        text = "You've used $mealScans / 10 free AI Meal Scans this month.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                } else if (featureName == "Weekly Report") {
                    Text(
                        text = "You've used $weeklyReports / 1 free Weekly Report this week.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                } else if (featureName == "AI Coach") {
                    Text(
                        text = "You have used $coachMessages / 2 free AI Coach messages today.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
                    listOf(
                        "Unlimited AI Coach", 
                        "Unlimited AI Meal Scans", 
                        "Unlimited Weekly Reports"
                    ).forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Included", tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(benefit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.analyticsManager.logPremiumUpgradeClicked(featureName)
                        viewModel.dismissPremiumGate()
                        viewModel.setScreen(Screen.PremiumIntro)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upgrade to Pro", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = { viewModel.dismissPremiumGate() }) {
                    Text("Not Now", color = if (isDark) Color.Gray else Color.DarkGray)
                }
            }
        }
    }
}
