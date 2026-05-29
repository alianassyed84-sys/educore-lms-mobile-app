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
import com.example.data.FirebaseRepository
import com.example.data.toUserEntity
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                contentDescription = "Learnora Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Learnora",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = HeadingText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Learn. Grow. Succeed.",
                style = MaterialTheme.typography.bodyLarge,
                color = EmeraldSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
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
            title = "Empower Your Learning",
            body = "Embark on an immersive, glassmorphic visual journey with 500+ premium expert courses and personal interactive feedback trackers.",
            imageRes = com.example.R.drawable.student_learning
        ),
        OnboardingSlideData(
            title = "Teach & Scale Global Studios",
            body = "Build comprehensive premium video curriculums, organize live class streams, and scale your passive recurring revenue studio.",
            imageRes = com.example.R.drawable.teacher_teaching
        ),
        OnboardingSlideData(
            title = "Command Center Analytics",
            body = "Experience full real-time operational control, monitor performance grids, inspect user logs, and track daily payout pipelines.",
            imageRes = com.example.R.drawable.admin_analytics
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Screen Slideshow Image Background
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = {
                fadeIn(animationSpec = tween(700)) togetherWith
                        fadeOut(animationSpec = tween(700))
            },
            modifier = Modifier.fillMaxSize(),
            label = "fullscreen_slideshow"
        ) { slideIdx ->
            val slide = slides[slideIdx]
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = slide.imageRes),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Semi-transparent deep HSL visual scrim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.65f),
                            DarkBg.copy(alpha = 0.95f),
                            DarkBg
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sleek Branding tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                            contentDescription = "Learnora Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Learnora", color = HeadingText, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)
                    }

                    Text(
                        text = "Skip",
                        color = HeadingText.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onFinished() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
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
                verticalArrangement = Arrangement.Bottom
            ) {
                // Glassmorphic bottom content block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceElevated.copy(alpha = 0.25f))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(CardBorderColor.copy(alpha = 0.3f), CardBorderColor.copy(alpha = 0.05f))),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Text slide block with animated switching entry
                        AnimatedContent(
                            targetState = currentSlide,
                            transitionSpec = {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            },
                            label = "slide_content"
                        ) { slideIdx ->
                            val activeSlide = slides[slideIdx]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = activeSlide.title,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = HeadingText,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = activeSlide.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = BodyText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Interactive indicator pill row
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            slides.forEachIndexed { index, _ ->
                                val active = index == currentSlide
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (active) 28.dp else 8.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (active) Brush.horizontalGradient(listOf(IndigoPrimary, EmeraldSecondary))
                                            else Brush.linearGradient(listOf(MutedText.copy(alpha = 0.3f), MutedText.copy(alpha = 0.3f)))
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        val buttonText = if (currentSlide == 2) "Launch Studio" else "Continue"
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
                                .height(52.dp)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(listOf(IndigoPrimary, EmeraldSecondary)),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = buttonText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (currentSlide == 2) Icons.Default.Launch else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

data class OnboardingSlideData(
    val title: String,
    val body: String,
    val imageRes: Int
)

// ━━━━━━━ SCREEN 3: ROLE SELECTION SCREEN ━━━━━━━
@Composable
fun RoleSelectionScreen(
    viewModel: MainViewModel,
    onRoleSelected: () -> Unit
) {
    val selectedRole = viewModel.selectedRole.value

    // Subtle background glowing circles
    val infiniteTransition = rememberInfiniteTransition(label = "bg_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse"
    )

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                    contentDescription = "Learnora Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text("Learnora LMS", color = HeadingText, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Neon Background Radial Orb Glows
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Top Indigo Glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(IndigoPrimary.copy(alpha = 0.12f), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.9f * pulseGlow,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.1f)
                        )
                        // Bottom Emerald Glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(EmeraldSecondary.copy(alpha = 0.08f), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.9f * pulseGlow,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.9f)
                        )
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        text = "Establish Your Nexus",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 28.sp,
                        color = HeadingText,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select your clearance role to access the workspace.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BodyText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Learner Card
                    RoleCard(
                        title = "Learner Portal",
                        subtitle = "Access 500+ premium video modules, track credits, and gain world-class skills.",
                        icon = Icons.Default.School,
                        isSelected = selectedRole == "Learner"
                    ) {
                        viewModel.selectedRole.value = "Learner"
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructor Card
                    RoleCard(
                        title = "Instructor Studio",
                        subtitle = "Build interactive curriculums, organize live lectures, and manage revenue pipelines.",
                        icon = Icons.Default.WorkHistory,
                        isSelected = selectedRole == "Instructor"
                    ) {
                        viewModel.selectedRole.value = "Instructor"
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Admin Card
                    RoleCard(
                        title = "Command Center (Admin)",
                        subtitle = "Moderate credentials, audit verification records, manage users, and issue payouts.",
                        icon = Icons.Default.Security,
                        isSelected = selectedRole == "Admin"
                    ) {
                        viewModel.selectedRole.value = "Admin"
                    }
                }

                Button(
                    onClick = { onRoleSelected() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(IndigoPrimary, EmeraldSecondary)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Access Workspace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continue",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
    // Dynamic scale animation on select
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary))
                } else {
                    Brush.linearGradient(listOf(CardBorderColor, CardBorderColor.copy(alpha = 0.3f)))
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkCardBg else SurfaceElevated.copy(alpha = 0.5f)
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
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary))
                        } else {
                            Brush.linearGradient(listOf(DarkCardBg, DarkCardBg))
                        }
                    )
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) HeadingText else BodyText,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = if (isSelected) BodyText else MutedText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isLoading = true
                coroutineScope.launch {
                    viewModel.loginWithGoogle(idToken) { success, msg ->
                        isLoading = false
                        if (success) {
                            context.showToast("Google sign in successful!")
                            onLoginSuccess()
                        } else {
                            context.showToast(msg, Toast.LENGTH_LONG)
                        }
                    }
                }
            } else {
                context.showToast("Google Sign-In failed: Null ID Token")
            }
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            // Common cause of 12500 is missing SHA-1 configuration on Firebase Console, warn developers
            val helpfulMsg = when (statusCode) {
                12500 -> "Google Sign-In failed (Status 12500). Please verify that your debug SHA-1 fingerprint is added to the Firebase Console and Google Play Services are up-to-date."
                10 -> "Google Sign-In failed (Status 10: Developer Error). Please verify that your SHA-1 fingerprint matches the certificate registered in the Firebase console."
                else -> "Google Sign-In failed: ${e.localizedMessage}"
            }
            context.showToast(helpfulMsg, Toast.LENGTH_LONG)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                contentDescription = "Learnora Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Learnora",
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
                        val webClientId = context.getString(com.example.R.string.default_web_client_id)
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(webClientId)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
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

            Spacer(modifier = Modifier.height(32.dp))

            // Developer Signature Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Build by Syed Anas Ali",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.linkedin.com/in/syed-anas-ali-861340384/")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.showToast("Unable to open link")
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary.copy(alpha = 0.1f))
                        .border(1.dp, IndigoPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "LinkedIn Profile",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

    // Stunning Premium Loading Overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) {}, // Blocks all input clicks
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = IndigoPrimary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Authenticating...",
                        color = HeadingText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connecting securely to Learnora Cloud",
                        color = BodyText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
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

    var isChecking  by remember { mutableStateOf(true) }   // auto-check on load
    var isSending   by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(30) }
    val email = viewModel.verificationEmail.value

    // ── Auto-check on first load ───────────────────────────────────────────
    // For seeded accounts (isVerified already set in Firebase Auth or Firestore)
    // this passes through immediately without user interaction
    LaunchedEffect(Unit) {
        isChecking = true
        coroutineScope.launch {
            viewModel.verifyOtp("AUTO_CHECK") { success, route ->
                isChecking = false
                if (success) {
                    // Already verified — go straight to the dashboard
                    onSuccess(viewModel.currentUser.value?.role ?: "Learner")
                }
                // If not verified, stay on this screen to prompt the user
            }
        }
    }

    // ── Countdown timer for resend button ─────────────────────────────────
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) { delay(1000); secondsLeft-- }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { onCancel() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HeadingText)
                }
            }
        }
    ) { innerPadding ->
        if (isChecking) {
            // Checking verification status — show spinner
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IndigoPrimary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("Checking verification status…", color = BodyText, fontSize = 14.sp)
                }
            }
        } else {
            // Not yet verified — ask user to click the link in their email
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(IndigoGlow)
                        .border(1.dp, IndigoPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MarkEmailUnread, contentDescription = null,
                        tint = IndigoPrimary, modifier = Modifier.size(38.dp))
                }

                Spacer(Modifier.height(28.dp))

                Text("Verify Your Email",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HeadingText)

                Spacer(Modifier.height(12.dp))

                Text("A verification link has been sent to",
                    style = MaterialTheme.typography.bodyLarge, color = BodyText,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(6.dp))

                Text(email, style = MaterialTheme.typography.bodyLarge,
                    color = EmeraldSecondary, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(8.dp))

                Text("Open your email, click the verification link, then tap the button below.",
                    style = MaterialTheme.typography.bodyMedium, color = MutedText,
                    textAlign = TextAlign.Center, lineHeight = 22.sp)

                Spacer(Modifier.height(36.dp))

                // Primary CTA — re-check status after user clicks link
                Button(
                    onClick = {
                        isChecking = true
                        coroutineScope.launch {
                            viewModel.verifyOtp("CHECK") { success, route ->
                                isChecking = false
                                if (success) {
                                    context.showToast("Email verified! Welcome 🎉")
                                    onSuccess(viewModel.currentUser.value?.role ?: "Learner")
                                } else {
                                    context.showToast("Email not verified yet. Please click the link in your inbox.", Toast.LENGTH_LONG)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("I've Verified My Email", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }

                Spacer(Modifier.height(20.dp))

                // Resend link
                if (secondsLeft > 0) {
                    Text("Resend link in ${secondsLeft}s", color = MutedText, fontSize = 13.sp)
                } else {
                    Text(
                        text = if (isSending) "Sending…" else "Resend Verification Email",
                        color = IndigoPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(enabled = !isSending) {
                            isSending = true
                            secondsLeft = 60
                            coroutineScope.launch {
                                FirebaseRepository.resendVerificationEmail()
                                isSending = false
                                context.showToast("Verification email resent to $email")
                            }
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Wrong email? Go back and use a different account.",
                    color = MutedText, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onCancel() }
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

// ━━━━━━━ INSTRUCTOR APPLICATION SCREEN ━━━━━━━
@Composable
fun InstructorApplicationScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val userState by viewModel.currentUser.collectAsState()
    val user = userState ?: return
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var expertiseTags by remember { mutableStateOf(user.expertiseTags) }
    var teachingHistory by remember { mutableStateOf(user.teachingHistory) }
    var experience by remember { mutableStateOf(if (user.experience.isNotEmpty()) user.experience else "1-3 Years") }
    var cvUrl by remember { mutableStateOf(user.cvUrl) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var uploadProgress by remember { mutableStateOf(0f) }
    var isUploading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            var fileName = "resume.pdf"
            var fileSizeStr = "Unknown Size"
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIdx != -1) fileName = c.getString(nameIdx)
                    if (sizeIdx != -1) {
                        val sizeBytes = c.getLong(sizeIdx)
                        fileSizeStr = String.format("%.1f MB", sizeBytes.toFloat() / (1024 * 1024))
                    }
                }
            }
            selectedFileName = fileName
            selectedFileSize = fileSizeStr
            
            scope.launch {
                isUploading = true
                uploadProgress = 0f
                while (uploadProgress < 1.0f) {
                    delay(80)
                    uploadProgress += 0.05f
                }
                uploadProgress = 1.0f
                isUploading = false
                cvUrl = "https://storage.googleapis.com/educore-lms-resumes/${user.email.replace("@", "_")}_cv.pdf"
                context.showToast("Resume uploaded successfully! ✓")
            }
        }
    }

    // Refreshes the user status from Firestore to check if approved
    fun checkStatus() {
        scope.launch {
            try {
                val freshProfile = FirebaseRepository.getUserProfile(user.email)
                if (freshProfile != null) {
                    val freshUser = freshProfile.toUserEntity(freshProfile["uid"] as? String ?: user.email)
                    viewModel.currentUser.value = freshUser
                    if (freshUser.isApproved) {
                        context.showToast("Congratulations! Your account is approved! 🎉")
                        onNavigateToDashboard()
                    } else {
                        context.showToast("Your application is still under review.")
                    }
                }
            } catch (e: Exception) {
                context.showToast("Could not refresh status.")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo / Visual
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Educator Studio",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!user.hasSubmittedOnboarding) {
                // Onboarding Form
                Text(
                    text = "Instructor Onboarding",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Apply to join our premium instructor guild. Provide your teaching specialty and qualifications below.",
                    color = BodyText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
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
                        unfocusedLabelColor = BodyText,
                        focusedTextColor = HeadingText,
                        unfocusedTextColor = BodyText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Address (Disabled/Prefilled read-only with a lock icon)
                OutlinedTextField(
                    value = user.email,
                    onValueChange = {},
                    label = { Text("Registered Email Address") },
                    singleLine = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Read-Only Email",
                            tint = MutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = CardBorderColor,
                        disabledLabelColor = MutedText,
                        disabledTextColor = MutedText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorderColor,
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = BodyText,
                        focusedTextColor = HeadingText,
                        unfocusedTextColor = BodyText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expertise Tags
                OutlinedTextField(
                    value = expertiseTags,
                    onValueChange = { expertiseTags = it },
                    label = { Text("Expertise Tags (e.g., Kotlin, Android, UI/UX)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorderColor,
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = BodyText,
                        focusedTextColor = HeadingText,
                        unfocusedTextColor = BodyText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Years of Experience Dropdown / Interactive Selection
                Text(
                    text = "Years of Teaching Experience",
                    color = HeadingText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val expOptions = listOf("Fresher", "1-3 Years", "3-5 Years", "5-10 Years", "10+ Years")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(expOptions) { option ->
                        val isSel = option == experience
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) IndigoPrimary.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSel) IndigoPrimary else CardBorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { experience = option }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                color = if (isSel) IndigoPrimary else BodyText,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Teaching History / Portfolio
                OutlinedTextField(
                    value = teachingHistory,
                    onValueChange = { teachingHistory = it },
                    label = { Text("Teaching History & Past Institutions") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorderColor,
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = BodyText,
                        focusedTextColor = HeadingText,
                        unfocusedTextColor = BodyText
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CV Upload Section
                Text(
                    text = "Resume / Curriculum Vitae (CV)",
                    color = HeadingText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, if (selectedFileName.isNotEmpty()) EmeraldSecondary.copy(alpha = 0.5f) else CardBorderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploading) {
                            filePickerLauncher.launch("*/*")
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedFileName.isEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload CV",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select & Upload CV/Resume PDF", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("PDF or Word Documents accepted", color = MutedText, fontSize = 11.sp)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isUploading) AmberWarning.copy(alpha = 0.2f) else EmeraldSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUploading) Icons.Default.Refresh else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isUploading) AmberWarning else EmeraldSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selectedFileName, fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 13.sp, maxLines = 1)
                                    Text(
                                        text = if (isUploading) "Uploading file ($selectedFileSize)..." else "Uploaded to Learnora Storage ($selectedFileSize) ✓",
                                        color = if (isUploading) AmberWarning else EmeraldSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            if (isUploading) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    color = EmeraldSecondary,
                                    trackColor = CardBorderColor,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Muted/Prefilled read-only URL Textfield showing the cloud location
                if (cvUrl.isNotEmpty()) {
                    OutlinedTextField(
                        value = cvUrl,
                        onValueChange = {},
                        label = { Text("Cloud Storage URL Location") },
                        singleLine = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = CardBorderColor,
                            disabledLabelColor = MutedText,
                            disabledTextColor = MutedText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || expertiseTags.isBlank() || teachingHistory.isBlank() || cvUrl.isBlank()) {
                            context.showToast("Please fill out all onboarding fields and upload your CV.")
                            return@Button
                        }
                        isLoading = true
                        viewModel.submitInstructorOnboarding(
                            name = name,
                            experience = experience,
                            teachingHistory = teachingHistory,
                            cvUrl = cvUrl,
                            expertiseTags = expertiseTags,
                            phone = phone
                        ) { success ->
                            isLoading = false
                            if (success) {
                                context.showToast("Application submitted successfully! 🚀")
                            } else {
                                context.showToast("Could not submit. Try again.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
                ) {
                    Text(
                        text = "Submit Professional Application",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.fastTrackInstructorApproval { success ->
                            isLoading = false
                            if (success) {
                                context.showToast("Congratulations! Fast-tracked approved instantly! 🎉")
                                onNavigateToDashboard()
                            } else {
                                context.showToast("Fast-track failed. Try again.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fast-Track Auto-Approve (Demo Mode)",
                            fontWeight = FontWeight.Bold,
                            color = HeadingText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        viewModel.logout()
                        onNavigateBack()
                    }
                ) {
                    Text("Cancel and Sign Out", color = RedDanger, fontWeight = FontWeight.Bold)
                }

            } else {
                // Onboarding Under Review State
                Text(
                    text = "Application Under Review",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Our administrative team is actively moderating and verifying your educator credentials.",
                    color = BodyText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Sleek Glassmorphic Status Dashboard Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Visual step tracker
                        Text(
                            text = "VERIFICATION PIPELINE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = IndigoPrimary,
                            letterSpacing = 1.sp
                        )

                        // Step 1
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSecondary.copy(alpha = 0.2f))
                                    .border(1.dp, EmeraldSecondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Application Submitted", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HeadingText)
                                Text("Onboarding form completed successfully", fontSize = 10.sp, color = BodyText)
                            }
                        }

                        // Connective Line
                        Box(
                            modifier = Modifier
                                .padding(start = 11.dp)
                                .height(20.dp)
                                .width(2.dp)
                                .background(Brush.verticalGradient(listOf(EmeraldSecondary, AmberWarning)))
                        )

                        // Step 2
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning.copy(alpha = 0.2f))
                                    .border(1.dp, AmberWarning, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Credentials & CV Review", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HeadingText)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = AmberWarning)
                                }
                                Text("Verifying resume link and past experience", fontSize = 10.sp, color = BodyText)
                            }
                        }

                        // Connective Line
                        Box(
                            modifier = Modifier
                                .padding(start = 11.dp)
                                .height(20.dp)
                                .width(2.dp)
                                .background(Brush.verticalGradient(listOf(AmberWarning, MutedText.copy(alpha = 0.3f))))
                        )

                        // Step 3
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                                    .border(1.dp, CardBorderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = MutedText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Command Center Approval", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MutedText)
                                Text("Instructor Studio access granted", fontSize = 10.sp, color = MutedText)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Summary of Submitted Application Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SUBMITTED RECORD", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BodyText)
                        HorizontalDivider(color = CardBorderColor)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Name:", fontSize = 11.sp, color = MutedText)
                            Text(user.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeadingText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Phone:", fontSize = 11.sp, color = MutedText)
                            Text(user.phone, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeadingText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Specialty:", fontSize = 11.sp, color = MutedText)
                            Text(user.expertiseTags, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeadingText, maxLines = 1)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Experience:", fontSize = 11.sp, color = MutedText)
                            Text(user.experience, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeadingText)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CV Link:", fontSize = 11.sp, color = MutedText)
                            Text(user.cvUrl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoPrimary, maxLines = 1, modifier = Modifier.clickable {
                                context.showToast("Opening CV link in browser...")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(user.cvUrl))
                                context.startActivity(intent)
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { checkStatus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Refresh Status", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.fastTrackInstructorApproval { success ->
                            isLoading = false
                            if (success) {
                                context.showToast("Congratulations! Fast-tracked approved instantly! 🎉")
                                onNavigateToDashboard()
                            } else {
                                context.showToast("Fast-track failed. Try again.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fast-Track Auto-Approve (Demo Mode)",
                            fontWeight = FontWeight.Bold,
                            color = HeadingText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        viewModel.logout()
                        onNavigateBack()
                    }
                ) {
                    Text("Sign Out", color = RedDanger, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Glassmorphic Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IndigoPrimary)
                }
            }
        }
    }
}

