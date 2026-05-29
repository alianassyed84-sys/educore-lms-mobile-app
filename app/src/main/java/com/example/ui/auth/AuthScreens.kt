package com.example.ui.auth

import android.widget.Toast
import com.example.util.showToast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ━━━━━━━ SCREEN 1: SPLASH SCREEN ━━━━━━━
@Composable
fun SplashScreen(
    viewModel: MainViewModel,
    onNavigateNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Ambient deep indigo glowing circle in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IndigoGlow, Color.Transparent),
                            radius = size.width / 2
                        )
                    )
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon / Logo Representation with neat gradient border
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(IndigoPrimary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "EduCore Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "EduCore",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = HeadingText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Learn. Build. Grow.",
                style = MaterialTheme.typography.bodyLarge,
                color = EmeraldSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateNext()
    }
}

// ━━━━━━━ SCREEN 2: ONBOARDING SCREEN ━━━━━━━
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }
    val slides = listOf(
        OnboardingSlideData(
            title = "Learn World-Class Skills",
            body = "Access 500+ courses in coding, design, and business with active feedback trackers.",
            icon = Icons.Default.Laptop
        ),
        OnboardingSlideData(
            title = "Teach & Earn Revenue",
            body = "Create structured video courses, compile quizzes, go live, and build your student base.",
            icon = Icons.Default.School // represent instructor with custom avatar
        ),
        OnboardingSlideData(
            title = "Track Every Metric",
            body = "Admins get full control over platform performance, revenue details, and student engagement.",
            icon = Icons.Default.Analytics
        )
    )

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    color = BodyText,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onFinished() }
                        .padding(8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Screen center presentation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, CircleShape)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val slide = slides[currentSlide]
                    Icon(
                        imageVector = if (currentSlide == 1) Icons.Default.Groups else slide.icon,
                        contentDescription = "Slide Icon",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = slides[currentSlide].title,
                    style = MaterialTheme.typography.displayMedium,
                    color = HeadingText,
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = slides[currentSlide].body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BodyText,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            // Bottom elements
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // Indicators inside row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (index == currentSlide) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (index == currentSlide) IndigoPrimary else MutedText)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                val buttonText = if (currentSlide == 2) "Get Started" else "Next"
                Button(
                    onClick = {
                        if (currentSlide < 2) {
                            currentSlide++
                        } else {
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        if (currentSlide < 2) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Arrow",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class OnboardingSlideData(
    val title: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// ━━━━━━━ SCREEN 3: ROLE SELECTION SCREEN ━━━━━━━
@Composable
fun RoleSelectionScreen(
    viewModel: MainViewModel,
    onRoleSelected: () -> Unit
) {
    Scaffold(
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Who are you?",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 28.sp,
                    color = HeadingText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select your role to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BodyText
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Learner Card
                RoleCard(
                    title = "Learner",
                    subtitle = "Explore online courses and grow your professional skills",
                    icon = Icons.Default.School,
                    isSelected = viewModel.selectedRole.value == "Learner"
                ) {
                    viewModel.selectedRole.value = "Learner"
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instructor Card
                RoleCard(
                    title = "Instructor",
                    subtitle = "Build awesome curriculums, teach live classes and earn revenues",
                    icon = Icons.Default.WorkHistory, // Chalkboard representation
                    isSelected = viewModel.selectedRole.value == "Instructor"
                ) {
                    viewModel.selectedRole.value = "Instructor"
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Admin Card
                RoleCard(
                    title = "Admin",
                    subtitle = "Moderate entire platform, manage users, and process payouts",
                    icon = Icons.Default.Security,
                    isSelected = viewModel.selectedRole.value == "Admin"
                ) {
                    viewModel.selectedRole.value = "Admin"
                }
            }

            Button(
                onClick = { onRoleSelected() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                color = if (isSelected) IndigoPrimary else CardBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkCardBg else DarkBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) IndigoPrimary else DarkCardBg)
                    .border(1.dp, CardBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else IndigoPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = HeadingText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BodyText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isSelected) IndigoPrimary else MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ━━━━━━━ SCREEN 4: LOGIN SCREEN ━━━━━━━
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onOtpRequired: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EduCore",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Selected Role indicator badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(IndigoGlow)
                    .border(1.dp, IndigoPrimary, RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Logging in as ${viewModel.selectedRole.value}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IndigoPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = BodyText
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Forgot Password Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        context.showToast("Please enter email and password.")
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        viewModel.login(email, password) { success, code ->
                            isLoading = false
                            if (success) {
                                // Handle both legacy and new Firebase verification codes
                                if (code == "OTP_VERIFICATION_REQUIRED" || code == "EMAIL_NOT_VERIFIED") {
                                    context.showToast("Please verify your email first.", Toast.LENGTH_LONG)
                                    onOtpRequired()
                                } else {
                                    context.showToast("Sign in successful!")
                                    onLoginSuccess()
                                }
                            } else {
                                context.showToast(code, Toast.LENGTH_LONG)
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider "or continue with"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(CardBorderColor))
                Text(
                    text = "or continue with",
                    color = MutedText,
                    modifier = Modifier.padding(horizontal = 14.dp),
                    fontSize = 12.sp
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(CardBorderColor))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        context.showToast("Social integration simulated.")
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeadingText),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Google", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        context.showToast("Social integration simulated.")
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeadingText),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneIphone, contentDescription = "Apple", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apple", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Don't have an account yet link
            if (viewModel.selectedRole.value != "Admin") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Don't have an account? ", color = BodyText, fontSize = 14.sp)
                    Text(
                        text = "Sign Up",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onNavigateToSignup() }
                    )
                }
            } else {
                Text(
                    text = "Admin profiles cannot be publicly created.",
                    color = MutedText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ━━━━━━━ SCREEN 5: SIGN UP SCREEN ━━━━━━━
@Composable
fun SignUpScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val strength = remember(password) { viewModel.evaluatePasswordStrength(password) }
    val strengthColor = when (strength) {
        "Weak" -> RedDanger
        "Fair" -> AmberWarning
        "Strong" -> IndigoPrimary
        else -> EmeraldSecondary
    }

    Scaffold(
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pre-filled Role: ${viewModel.selectedRole.value}",
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = BodyText
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Password Strength Indicator Bar
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Password Strength:", fontSize = 11.sp, color = BodyText)
                        Text(text = strength, fontSize = 11.sp, color = strengthColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MutedText)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    when (strength) {
                                        "Weak" -> 0.25f
                                        "Fair" -> 0.5f
                                        "Strong" -> 0.75f
                                        else -> 1.0f
                                    }
                                )
                                .fillMaxHeight()
                                .background(strengthColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = CardBorderColor,
                    focusedLabelColor = IndigoPrimary,
                    unfocusedLabelColor = BodyText
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // T&C Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsChecked,
                    onCheckedChange = { termsChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = IndigoPrimary)
                )
                Text(
                    text = "I agree to the Terms of Service & Privacy Policies",
                    color = BodyText,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { termsChecked = !termsChecked }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Account Button
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        context.showToast("All fields are required.")
                        return@Button
                    }
                    if (password != confirmPassword) {
                        context.showToast("Passwords do not match.")
                        return@Button
                    }
                    if (!termsChecked) {
                        context.showToast("You must accept the Terms and Conditions.")
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        viewModel.register(name, email, password) { success, msg ->
                            isLoading = false
                            if (success) {
                                context.showToast("Verification code sent to $email", Toast.LENGTH_LONG)
                                onSubmitSuccess()
                            } else {
                                context.showToast(msg, Toast.LENGTH_LONG)
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ", color = BodyText, fontSize = 14.sp)
                Text(
                    text = "Login",
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ━━━━━━━ SCREEN 6: EMAIL VERIFICATION SCREEN ━━━━━━━
@Composable
fun EmailVerificationScreen(
    viewModel: MainViewModel,
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember { List(6) { FocusRequester() } }

    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    var secondsLeft by remember { mutableStateOf(60) }
    var shakeTrigger by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    // Start 60 second countdown resend timer
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    // Shake animation logic
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger) {
            repeat(4) {
                shakeOffset.animateTo(
                    targetValue = if (shakeOffset.value == 0f) 15f else -15f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh)
                )
            }
            shakeOffset.animateTo(0f)
            shakeTrigger = false
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { onCancel() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = HeadingText)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify Email",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We sent a code to",
                style = MaterialTheme.typography.bodyLarge,
                color = BodyText
            )

            Text(
                text = viewModel.verificationEmail.value,
                style = MaterialTheme.typography.bodyLarge,
                color = EmeraldSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated OTP visual reveal for testing
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Simulated Code: ${viewModel.generatedOtp.value}",
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = AmberWarning
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // OTP Input digits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationX = shakeOffset.value),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..5) {
                    OutlinedTextField(
                        value = otpDigits[i],
                        onValueChange = { newValue: String ->
                            if (newValue.length <= 1) {
                                // Strip to only digit
                                if (newValue.isEmpty()) {
                                    otpDigits[i] = ""
                                    // Move back on deletion
                                    if (i > 0) {
                                        runCatching { focusRequesters[i - 1].requestFocus() }
                                    }
                                } else if (newValue.all { char: Char -> char.isDigit() }) {
                                    otpDigits[i] = newValue
                                    // Move to next field
                                    if (i < 5) {
                                        runCatching { focusRequesters[i + 1].requestFocus() }
                                    } else {
                                        focusManager.clearFocus() // Finished last digit
                                    }
                                }
                            } else if (newValue.length == 2 && otpDigits[i].isNotEmpty()) {
                                val nextChar = newValue.last().toString()
                                if (nextChar.all { char: Char -> char.isDigit() }) {
                                    otpDigits[i] = nextChar
                                    if (i < 5) {
                                        runCatching { focusRequesters[i + 1].requestFocus() }
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeadingText,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .size(50.dp)
                            .focusRequester(focusRequesters[i]),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorderColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Verify Button
            Button(
                onClick = {
                    val code = otpDigits.joinToString("")
                    if (code.length < 6) {
                        context.showToast("Please enter all 6 digits.")
                        shakeTrigger = true
                        return@Button
                    }

                    coroutineScope.launch {
                        viewModel.verifyOtp(code) { success, route ->
                            if (success) {
                                context.showToast("Verification successful!")
                                onSuccess(route)
                            } else {
                                context.showToast(route)
                                shakeTrigger = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Verify Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Countdown
            if (secondsLeft > 0) {
                Text(
                    text = "Resend OTP in $secondsLeft second(s)",
                    color = MutedText,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    text = "Resend Verification Code",
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        secondsLeft = 60
                        viewModel.generatedOtp.value = (100000..999999).random().toString()
                        context.showToast("New verification code triggered!")
                    }
                )
            }
        }
    }
}

// ━━━━━━━ SCREEN 7: FORGOT PASSWORD SCREEN ━━━━━━━
@Composable
fun ForgotPasswordScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1: Email, 2: OTP, 3: Password, 4: Success
    var email by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    if (step > 1) step-- else onNavigateBack()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = HeadingText)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                1 -> {
                    Text(text = "Forgot Password", style = MaterialTheme.typography.displayMedium, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Enter your verified email to receive a password recovery verification code", color = BodyText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(30.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (email.isBlank()) return@Button
                            coroutineScope.launch {
                                viewModel.forgotPasswordRequest(email) { success, error ->
                                    if (success) {
                                        context.showToast("OTP Sent code!")
                                        step = 2
                                    } else {
                                        context.showToast(error)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Send Reset Code", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                2 -> {
                    Text(text = "Enter Code", style = MaterialTheme.typography.displayMedium, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Enter the 6-digit verification code sent to your email", color = BodyText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(DarkCardBg).border(1.dp, CardBorderColor).padding(8.dp)
                    ) {
                        Text(text = "Simulated Code: ${viewModel.generatedOtp.value}", color = AmberWarning)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it },
                        label = { Text("6-Digit Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (enteredOtp == viewModel.generatedOtp.value) {
                                step = 3
                            } else {
                                context.showToast("Invalid validation code.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Verify Code", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                3 -> {
                    Text(text = "New Password", style = MaterialTheme.typography.displayMedium, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Create a strong new password with at least 8 characters", color = BodyText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(30.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (newPassword.length < 8) {
                                context.showToast("Password must be at least 8 characters.")
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                context.showToast("Passwords do not match.")
                                return@Button
                            }
                            coroutineScope.launch {
                                viewModel.resetPassword(newPassword) { success, err ->
                                    if (success) {
                                        step = 4
                                    } else {
                                        context.showToast(err)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Save New Password", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                4 -> {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = EmeraldSecondary, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Password Reset!", style = MaterialTheme.typography.displayMedium, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Your password has been changed successfully. You can now login with your new credentials.", color = BodyText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = { onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Back to Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
