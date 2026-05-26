package com.example.ui.learner

import android.widget.Toast
import com.example.util.showToast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// ━━━━━━━ SECTIONS ROOT CONTROLLER ━━━━━━━
@Composable
fun LearnerMainScreen(
    viewModel: MainViewModel,
    onNavigateToCourseDetail: (Int) -> Unit,
    onNavigateToCoursePlayer: (Int) -> Unit,
    onNavigateToUpgrade: () -> Unit,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Home") }
    
    val currentUserState by viewModel.currentUser.collectAsState()
    val user = currentUserState ?: return

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(
                containerColor = DarkCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val tabs = listOf(
                    NavigationTabItem("Home", Icons.Default.Home),
                    NavigationTabItem("My Courses", Icons.Default.Book),
                    NavigationTabItem("Search", Icons.Default.Search),
                    NavigationTabItem("Path", Icons.Default.AltRoute),
                    NavigationTabItem("Profile", Icons.Default.Person)
                )

                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab.name,
                        onClick = { activeTab = tab.name },
                        icon = { Icon(tab.icon, contentDescription = tab.name) },
                        label = { Text(tab.name, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = IndigoPrimary,
                            unselectedIconColor = MutedText,
                            unselectedTextColor = MutedText,
                            indicatorColor = IndigoPrimary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Home" -> LearnerHomeScreen(viewModel, user, onNavigateToCourseDetail, onNavigateToUpgrade)
                "My Courses" -> LearnerMyCoursesScreen(viewModel, user, onNavigateToCourseDetail, onNavigateToCoursePlayer)
                "Search" -> LearnerSearchScreen(viewModel, onNavigateToCourseDetail)
                "Path" -> LearnerPathGamificationScreen(viewModel, user)
                "Profile" -> LearnerProfileScreen(viewModel, user, onNavigateToUpgrade, onLogout)
            }
        }
    }
}

data class NavigationTabItem(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// ━━━━━━━ TAB 1: HOME FEED ━━━━━━━
@Composable
fun LearnerHomeScreen(
    viewModel: MainViewModel,
    user: UserEntity,
    onNavigateToCourseDetail: (Int) -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()
    val enrollments by viewModel.userEnrollments.collectAsState()

    val recommendedCourses = remember(courses) {
        courses.filter { it.status == "Approved" }
    }

    val continueLearningEnrollment = remember(enrollments, recommendedCourses) {
        enrollments.firstOrNull { it.progress < 100 }?.let { enrollment ->
            recommendedCourses.firstOrNull { it.id == enrollment.courseId }?.let { course ->
                Pair(enrollment, course)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Top Bar Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BodyText
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, CircleShape)
                        .clickable {
                            context.showToast("No unread alerts.")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = IndigoPrimary
                    )
                }
            }
        }

        // Weekly Streak Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular progress ring
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = CardBorderColor,
                                style = Stroke(width = 6.dp.toPx())
                            )
                            drawArc(
                                color = EmeraldSecondary,
                                startAngle = -90f,
                                sweepAngle = (4f / 7f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 6.dp.toPx())
                            )
                        }
                        Text(
                            text = "4/7",
                            fontWeight = FontWeight.Bold,
                            color = HeadingText,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${user.streakCount} Day Streak!",
                                style = MaterialTheme.typography.titleLarge,
                                color = HeadingText,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire Core",
                                tint = AmberWarning,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep coding daily to unlock your 7-Day Streak Badge!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BodyText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Continue Learning Card
        continueLearningEnrollment?.let { (enrollment, course) ->
            item {
                Text(
                    text = "Continue Learning",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    val progressValue = enrollment.progress / 100f
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoGlow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Laptop, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instructor: ${course.instructorName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    color = BodyText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${enrollment.progress}% complete", fontSize = 12.sp, color = BodyText)
                            Text(text = "Remaining: ${100 - enrollment.progress}%", fontSize = 11.sp, color = MutedText)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndigoPrimary,
                            trackColor = CardBorderColor
                        )
                    }
                }
            }
        }

        // Recommended For You (Horizontal LazyRow)
        item {
            Text(
                text = "Recommended For You",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recommendedCourses) { course ->
                    val isEnrolled = enrollments.any { it.courseId == course.id }
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { onNavigateToCourseDetail(course.id) }
                            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column {
                            // Thumbnail Representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        when (course.category) {
                                            "Coding" -> Brush.verticalGradient(listOf(IndigoPrimary, DarkCardBg))
                                            "Design" -> Brush.verticalGradient(listOf(EmeraldSecondary, DarkCardBg))
                                            else -> Brush.verticalGradient(listOf(AmberWarning, DarkCardBg))
                                        }
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(DarkCardBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = course.category,
                                        fontSize = 10.sp,
                                        color = HeadingText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = course.title,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${course.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HeadingText)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "(${course.enrolledCount} enrolled)", fontSize = 11.sp, color = BodyText)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        Text(
                                            text = if (course.price == 0) "Free" else "₹${course.price}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (course.price == 0) EmeraldSecondary else HeadingText
                                        )
                                    }

                                    // Locked indicators for free plan premium courses
                                    if (user.subscription == "Free" && course.price > 0) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked Premium", tint = AmberWarning, modifier = Modifier.size(16.dp))
                                    } else if (isEnrolled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(EmeraldSecondary.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("Enrolled", color = EmeraldSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Instructors
        item {
            Text(
                text = "Top Instructors",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InstructorRowItem("Dev Kar", "24.5k Students", "https://example.com/avatar1")
                }
                item {
                    InstructorRowItem("Sarah Hughes", "18.3k Students", "https://example.com/avatar2")
                }
                item {
                    InstructorRowItem("Dr. Ramesh Sen", "9.2k Students", "https://example.com/avatar3")
                }
            }
        }

        // Floating Pro banner at bottom for free status users
        if (user.subscription == "Free") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToUpgrade() }
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(IndigoPrimary, EmeraldSecondary)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upgrade to Pro Version",
                                fontWeight = FontWeight.Bold,
                                color = HeadingText,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock premium paths & certified tokens for only ₹499/mo",
                                color = BodyText,
                                fontSize = 11.sp
                            )
                        }
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Upgrade", tint = EmeraldSecondary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InstructorRowItem(name: String, students: String, avatarUrl: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        modifier = Modifier
            .width(130.dp)
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = HeadingText, maxLines = 1)
            Text(text = students, fontSize = 10.sp, color = BodyText)
        }
    }
}

// ━━━━━━━ SCREEN 2: COURSE DETAIL PAGE ━━━━━━━
@Composable
fun CourseDetailScreen(
    courseId: Int,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val courses by viewModel.allCoursesList.collectAsState()
    val enrollments by viewModel.userEnrollments.collectAsState()
    val currentUserState by viewModel.currentUser.collectAsState()
    val user = currentUserState ?: return

    val course = remember(courses) { courses.firstOrNull { it.id == courseId } } ?: return
    val isEnrolled = remember(enrollments) { enrollments.any { it.courseId == courseId } }
    val isPremium = course.price > 0
    val isWishlisted = remember(enrollments) { enrollments.firstOrNull { it.courseId == courseId }?.wishlist ?: false }

    var selectedTab by remember { mutableStateOf("Overview") } // "Overview", "Curriculum", "Reviews"

    // Lessons loading flow
    var lessonsList by remember { mutableStateOf<List<LessonEntity>>(emptyList()) }
    LaunchedEffect(courseId) {
        viewModel.lessonDao.getLessonsForCourseFlow(courseId).collect {
            lessonsList = it
        }
    }

    // Reviews flow
    var reviewsList by remember { mutableStateOf<List<ReviewEntity>>(emptyList()) }
    LaunchedEffect(courseId) {
        viewModel.reviewDao.getReviewsForCourseFlow(courseId).collect {
            reviewsList = it
        }
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardBg)
                    .border(BorderStroke(1.dp, CardBorderColor))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Price", fontSize = 11.sp, color = BodyText)
                    Text(
                        text = if (course.price == 0) "Free" else "₹${course.price}",
                        fontWeight = FontWeight.Bold,
                        color = if (course.price == 0) EmeraldSecondary else HeadingText,
                        fontSize = 20.sp
                    )
                }

                if (isEnrolled) {
                    Button(
                        onClick = { onNavigateToPlayer(courseId) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("Resume Learning", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (isPremium && user.subscription == "Free") {
                                onNavigateToUpgrade()
                            } else {
                                viewModel.enrollInCourse(courseId)
                                context.showToast("Welcome to the course!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = if (isPremium && user.subscription == "Free") "Unlock with Pro" else "Enroll Now",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image Placeholder banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.verticalGradient(listOf(IndigoPrimary, DarkBg))),
                contentAlignment = Alignment.TopStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onNavigateBack() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkCardBg.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            viewModel.toggleWishlist(courseId)
                            context.showToast(if (isWishlisted) "Removed from wishlist" else "Added to wishlist")
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkCardBg.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save wishlist",
                            tint = if (isWishlisted) Color.Red else Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Category Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(IndigoGlow)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = course.category, fontSize = 11.sp, color = IndigoPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = course.title,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${course.rating}", fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "(${course.reviewCount} Reviews)", color = BodyText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "•   ${course.enrolledCount} Enrolled Students", color = BodyText, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Instructor Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(course.instructorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = course.instructorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Co-Founder @EduCore", fontSize = 11.sp, color = BodyText)
                    }
                    OutlinedButton(
                        onClick = { context.showToast("Following ${course.instructorName}!") },
                        border = BorderStroke(1.dp, CardBorderColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Follow", fontSize = 11.sp, color = HeadingText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section Tabs M3 style
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "Overview" -> 0
                        "Curriculum" -> 1
                        else -> 2
                    },
                    containerColor = DarkBg,
                    contentColor = IndigoPrimary
                ) {
                    val tabs = listOf("Overview", "Curriculum", "Reviews")
                    tabs.forEachIndexed { i, tabName ->
                        Tab(
                            selected = selectedTab == tabName,
                            onClick = { selectedTab = tabName },
                            text = { Text(tabName, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content Renderers
                when (selectedTab) {
                    "Overview" -> {
                        Column {
                            Text(text = "What you'll learn", fontWeight = FontWeight.Bold, color = HeadingText)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = BodyText
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Course Details", fontWeight = FontWeight.Bold, color = HeadingText)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "•   Full access with Pro subscription\n•   Verified certified course modules\n•   1-on-1 scheduled support hours included", fontSize = 12.sp, lineHeight = 22.sp, color = BodyText)
                        }
                    }
                    "Curriculum" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (lessonsList.isEmpty()) {
                                Text("No lessons configured yet.", color = MutedText, fontSize = 12.sp)
                            } else {
                                lessonsList.forEach { lesson ->
                                    val canAccess = !isPremium || user.subscription == "Pro" || lesson.isPreview
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DarkCardBg)
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (lesson.type == "Video") Icons.Default.PlayCircle else Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = IndigoPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = lesson.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                                Text(text = "${lesson.type} •   ${lesson.duration} mins", fontSize = 11.sp, color = BodyText)
                                            }
                                        }

                                        if (!canAccess) {
                                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = AmberWarning, modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Access", tint = EmeraldSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Reviews" -> {
                        Column {
                            // Star rating overview
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkCardBg)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "${course.rating}", style = MaterialTheme.typography.displayMedium, fontSize = 32.sp)
                                    Row {
                                        repeat(5) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                Text(
                                    text = "Excellent student feedback overall. Rating remains exceptionally strong.",
                                    color = BodyText,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f).padding(start = 24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom Draft Review Form
                            var ratingDraft by remember { mutableStateOf(5) }
                            var commentDraft by remember { mutableStateOf("") }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Add Your Review", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (r in 1..5) {
                                            IconButton(onClick = { ratingDraft = r }, modifier = Modifier.size(24.dp)) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (r <= ratingDraft) AmberWarning else MutedText
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = commentDraft,
                                        onValueChange = { commentDraft = it },
                                        placeholder = { Text("What did you think of this course?") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = IndigoPrimary,
                                            unfocusedBorderColor = CardBorderColor
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (commentDraft.isBlank()) return@Button
                                            viewModel.addNewCourseReview(courseId, ratingDraft, commentDraft)
                                            commentDraft = ""
                                            context.showToast("Review added successfully!")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                    ) {
                                        Text("Submit Review")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Reviews List
                            reviewsList.forEach { review ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MutedText),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = review.userName.take(1), color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row {
                                            repeat(review.rating) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = review.comment, fontSize = 11.sp, color = BodyText, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 3: COURSE PLAYER ━━━━━━━
@Composable
fun CoursePlayerScreen(
    courseId: Int,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val courses by viewModel.allCoursesList.collectAsState()
    val enrollments by viewModel.userEnrollments.collectAsState()
    val course = remember(courses) { courses.firstOrNull { it.id == courseId } } ?: return
    val enrollment = remember(enrollments) { enrollments.firstOrNull { it.courseId == courseId } }

    var lessonsList by remember { mutableStateOf<List<LessonEntity>>(emptyList()) }
    LaunchedEffect(courseId) {
        viewModel.lessonDao.getLessonsForCourseFlow(courseId).collect {
            lessonsList = it
        }
    }

    var selectedLesson by remember { mutableStateOf<LessonEntity?>(null) }
    LaunchedEffect(lessonsList) {
        if (selectedLesson == null && lessonsList.isNotEmpty()) {
            selectedLesson = lessonsList.first()
        }
    }

    // Interactive Notes Persistence inside session
    var studentNotesText by remember { mutableStateOf("") }
    LaunchedEffect(enrollment) {
        enrollment?.let {
            studentNotesText = it.notes
        }
    }

    // Video Player state simulation controls
    var isPlaying by remember { mutableStateOf(true) }
    var lessonProgress by remember { mutableStateOf(0.4f) }
    var xpConfettiGranted by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 16:9 Simulated Player Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f)
                    .background(Color.Black)
            ) {
                // Background visual simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(IndigoPrimary.copy(alpha = 0.3f), DarkBg)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Simulated states",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Streaming Lesson Video...",
                            color = BodyText,
                            fontSize = 11.sp
                        )
                    }
                }

                // Header back arrow
                IconButton(
                    onClick = { onNavigateBack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(DarkCardBg.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Header Back", tint = Color.White)
                }

                // Controls row at bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Control Play",
                            tint = Color.White
                        )
                    }

                    Slider(
                        value = lessonProgress,
                        onValueChange = { lessonProgress = it },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = IndigoPrimary,
                            thumbColor = IndigoPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(text = "10:15 / 22:30", color = Color.White, fontSize = 10.sp)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Title
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                selectedLesson?.let {
                    Text(
                        text = "Now Playing: ${it.title}",
                        color = EmeraldSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confetti / Reward Announcement Banner
                AnimatedVisibility(visible = xpConfettiGranted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, EmeraldSecondary, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, contentDescription = "Victory", tint = EmeraldSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Lesson Completed!", fontWeight = FontWeight.Bold, color = HeadingText)
                                Text("Reward: +50 XP and Streak count updated successfully!", fontSize = 11.sp, color = BodyText)
                            }
                        }
                    }
                }

                // "Mark as Complete" core action
                Button(
                    onClick = {
                        selectedLesson?.let { l ->
                            viewModel.markLessonComplete(courseId, l.id) { xp ->
                                if (xp > 0) {
                                    xpConfettiGranted = true
                                    coroutineScope.launch {
                                        delay(4000)
                                        xpConfettiGranted = false
                                    }
                                } else {
                                    context.showToast("Previously marked completed.")
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
                    Text("Mark as Complete", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Chapter List & Notes tabs
                var expandTrackTab by remember { mutableStateOf("Chapters") }
                TabRow(
                    selectedTabIndex = if (expandTrackTab == "Chapters") 0 else 1,
                    containerColor = DarkBg,
                    contentColor = IndigoPrimary
                ) {
                    Tab(
                        selected = expandTrackTab == "Chapters",
                        onClick = { expandTrackTab = "Chapters" },
                        text = { Text("Chapters", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = expandTrackTab == "Notes",
                        onClick = { expandTrackTab = "Notes" },
                        text = { Text("Personal Notes", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (expandTrackTab == "Chapters") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lessonsList.forEach { lesson ->
                            val isCompleted = enrollment?.completedLessonIds?.split(",")?.contains(lesson.id.toString()) == true
                            val isCurrent = selectedLesson?.id == lesson.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) IndigoGlow else DarkCardBg)
                                    .border(
                                        1.dp,
                                        if (isCurrent) IndigoPrimary else CardBorderColor,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedLesson = lesson }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = if (isCompleted) EmeraldSecondary else MutedText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = lesson.title,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isCurrent) HeadingText else BodyText
                                    )
                                }

                                Text(text = lesson.duration, fontSize = 11.sp, color = MutedText)
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = studentNotesText,
                            onValueChange = {
                                studentNotesText = it
                                viewModel.saveLessonNotes(courseId, it)
                            },
                            placeholder = { Text("Jot down notes, links or codes here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = CardBorderColor
                            )
                        )

                        Button(
                            onClick = {
                                viewModel.saveLessonNotes(courseId, studentNotesText)
                                context.showToast("Notes saved successfully!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
                        ) {
                            Text("Save Draft", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ TAB 2: SEARCH & DISCOVER ━━━━━━━
@Composable
fun LearnerSearchScreen(
    viewModel: MainViewModel,
    onNavigateToCourseDetail: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf("All") }
    val chips = listOf("All", "Coding", "Design", "Business", "DevOps")

    val courses by viewModel.allCoursesList.collectAsState()

    val filteredCourses = remember(courses, searchQuery, selectedChip) {
        courses.filter { course ->
            val matchQuery = course.title.contains(searchQuery, ignoreCase = true) ||
                    course.category.contains(searchQuery, ignoreCase = true)
            val matchChip = selectedChip == "All" || course.category == selectedChip
            matchQuery && matchChip && course.status == "Approved"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Discover Courses",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar with Filter Icon
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by language, instructor, skill...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = BodyText) },
            trailingIcon = {
                IconButton(onClick = { /* Filter option action */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = IndigoPrimary)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoPrimary,
                unfocusedBorderColor = CardBorderColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips list row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chip ->
                val isSelected = selectedChip == chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) IndigoPrimary else DarkCardBg)
                        .border(1.dp, if (isSelected) IndigoPrimary else CardBorderColor, RoundedCornerShape(50.dp))
                        .clickable { selectedChip = chip }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chip,
                        color = if (isSelected) Color.White else BodyText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results vertical list
        if (filteredCourses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, tint = MutedText, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No courses found. Try a different keyword.",
                    color = BodyText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCourses) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCourseDetail(course.id) }
                            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(course.category.take(2), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                                Text(text = course.instructorName, color = BodyText, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(12.dp))
                                    Text(text = " ${course.rating}", fontSize = 11.sp, color = HeadingText)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (course.price == 0) "Free" else "₹${course.price}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (course.price == 0) EmeraldSecondary else HeadingText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ TAB 3: MY COURSES ━━━━━━━
@Composable
fun LearnerMyCoursesScreen(
    viewModel: MainViewModel,
    user: UserEntity,
    onNavigateToCourseDetail: (Int) -> Unit,
    onNavigateToCoursePlayer: (Int) -> Unit
) {
    var selectedSectionTab by remember { mutableStateOf("In Progress") } // "In Progress", "Completed", "Wishlist"
    val courses by viewModel.allCoursesList.collectAsState()
    val enrollments by viewModel.userEnrollments.collectAsState()

    // Filtered lists based on enrollments & section criteria
    val progressCourses = remember(enrollments, courses) {
        enrollments.filter { it.progress < 100 && !it.wishlist }.mapNotNull { enrollment ->
            courses.firstOrNull { it.id == enrollment.courseId }?.let { Pair(enrollment, it) }
        }
    }

    val completedCourses = remember(enrollments, courses) {
        enrollments.filter { it.progress >= 100 && !it.wishlist }.mapNotNull { enrollment ->
            courses.firstOrNull { it.id == enrollment.courseId }?.let { Pair(enrollment, it) }
        }
    }

    val wishlistCourses = remember(enrollments, courses) {
        enrollments.filter { it.wishlist }.mapNotNull { enrollment ->
            courses.firstOrNull { it.id == enrollment.courseId }?.let { Pair(enrollment, it) }
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "My Learning",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simple row toggler
        TabRow(
            selectedTabIndex = when (selectedSectionTab) {
                "In Progress" -> 0
                "Completed" -> 1
                else -> 2
            },
            containerColor = DarkBg,
            contentColor = IndigoPrimary
        ) {
            Tab(selected = selectedSectionTab == "In Progress", onClick = { selectedSectionTab = "In Progress" }) {
                Text("In Progress", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = selectedSectionTab == "Completed", onClick = { selectedSectionTab = "Completed" }) {
                Text("Completed", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = selectedSectionTab == "Wishlist", onClick = { selectedSectionTab = "Wishlist" }) {
                Text("Wishlist", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSectionTab) {
            "In Progress" -> {
                if (progressCourses.isEmpty()) {
                    ListEmptyRenderer("No courses in progress. Explore the Search tab to enroll immediately!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(progressCourses) { (enrollment, course) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(IndigoPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(course.category.take(2), color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "Streak progress: ${enrollment.progress}% complete", fontSize = 11.sp, color = BodyText)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { enrollment.progress / 100f },
                                        color = IndigoPrimary,
                                        trackColor = CardBorderColor,
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onNavigateToCoursePlayer(course.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text("Continue Learning", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Completed" -> {
                if (completedCourses.isEmpty()) {
                    ListEmptyRenderer("Zero certificates earned yet. Finish a course to unlock high-fidelity credentials!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(completedCourses) { (enrollment, course) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = course.title, fontWeight = FontWeight.Bold)
                                            Text(text = "Completed On: May 2026", fontSize = 11.sp, color = BodyText)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            context.showToast("Downloading PDF certificate token (ID: CERT_${course.id}9472)...", Toast.LENGTH_LONG)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Download Certificate (PDF)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Wishlist" -> {
                if (wishlistCourses.isEmpty()) {
                    ListEmptyRenderer("Your wishlist is empty.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(wishlistCourses) { (enrollment, course) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = if (course.price == 0) "Free" else "₹${course.price}", color = EmeraldSecondary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { onNavigateToCourseDetail(course.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Enroll", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListEmptyRenderer(msg: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.School, contentDescription = null, tint = MutedText, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = msg, color = BodyText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
    }
}

// ━━━━━━━ TAB 4: PATH & GAMIFICATION ━━━━━━━
@Composable
fun LearnerPathGamificationScreen(
    viewModel: MainViewModel,
    user: UserEntity
) {
    var pathTab by remember { mutableStateOf("Roadmap") } // "Roadmap", "Badges", "Leaderboard"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Learning Path",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats summary header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCardBg)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("XP Points", fontSize = 10.sp, color = BodyText)
                Text("${user.xp} XP", fontWeight = FontWeight.Bold, color = EmeraldSecondary, fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Badges", fontSize = 10.sp, color = BodyText)
                Text("${user.badges.split(",").filter { it.isNotEmpty() }.size} Earned", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 16.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Streak Counter", fontSize = 10.sp, color = BodyText)
                Text("${user.streakCount} Days", fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = when (pathTab) {
                "Roadmap" -> 0
                "Badges" -> 1
                else -> 2
            },
            containerColor = DarkBg,
            contentColor = IndigoPrimary
        ) {
            Tab(selected = pathTab == "Roadmap", onClick = { pathTab = "Roadmap" }) {
                Text("Roadmap", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = pathTab == "Badges", onClick = { pathTab = "Badges" }) {
                Text("Badges", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = pathTab == "Leaderboard", onClick = { pathTab = "Leaderboard" }) {
                Text("Leaderboard", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (pathTab) {
            "Roadmap" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        TimelineNode("Beginner stage", "Start here with fundamental programming, logic structures & simple arrays.", true)
                    }
                    item {
                        TimelineNode("Intermediate block", "Learn structural layout grids, CSS systems, component models & data bindings.", true)
                    }
                    item {
                        TimelineNode("Advanced control", "Setup Docker deployment layers, CI/CD pipeline triggers & cloud architectures.", false)
                    }
                    item {
                        TimelineNode("Expert masters", "Formulate neural vector networks, optimize database locks & compile custom engines.", false)
                    }
                }
            }
            "Badges" -> {
                val earnedList = user.badges.split(",").filter { it.isNotEmpty() }
                val allBadges = listOf(
                    BadgeMetadata("First Course", "Successfully finished course onboarding", Icons.Default.Flag),
                    BadgeMetadata("7-Day Streak", "Code daily for 7 consecutive days", Icons.Default.FlashOn),
                    BadgeMetadata("Python Master", "Finished Advanced Python development module", Icons.Default.Token),
                    BadgeMetadata("Figma Expert", "Approved reviews on design layout structures", Icons.Default.Palette),
                    BadgeMetadata("Cert Champion", "Downloaded 3+ verified certificate tokens", Icons.Default.Star)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allBadges) { badge ->
                        val isEarned = earnedList.any { it.contains(badge.name) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isEarned) IndigoPrimary else CardBorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                        .background(if (isEarned) IndigoGlow else DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = null,
                                        tint = if (isEarned) IndigoPrimary else MutedText,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = badge.name, fontWeight = FontWeight.Bold, color = if (isEarned) HeadingText else MutedText, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = badge.desc,
                                    fontSize = 10.sp,
                                    color = MutedText,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            "Leaderboard" -> {
                val leaderboardList = listOf(
                    LeaderboardUser(1, "Dev Kar", 2400, "Instructor"),
                    LeaderboardUser(2, "Sarah Hughes", 1950, "Instructor"),
                    LeaderboardUser(3, "Rohit Sharma", 650, "Learner"),
                    LeaderboardUser(4, "Pooja Patil", 450, "Learner"),
                    LeaderboardUser(5, "Alok Jha", 310, "Learner"),
                    LeaderboardUser(6, "Karan Grover", 250, "Learner")
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(leaderboardList) { runner ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardBg)
                                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (runner.rank <= 3) IndigoPrimary else DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${runner.rank}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = runner.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = runner.role, fontSize = 10.sp, color = BodyText)
                                }
                            }

                            Text(
                                text = "${runner.xp} XP",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BadgeMetadata(val name: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
data class LeaderboardUser(val rank: Int, val name: String, val xp: Int, val role: String)

@Composable
fun TimelineNode(stage: String, details: String, isUnlocked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) EmeraldSecondary else MutedText)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(if (isUnlocked) EmeraldSecondary else MutedText)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = stage, fontWeight = FontWeight.Bold, color = if (isUnlocked) HeadingText else MutedText)
            Text(text = details, fontSize = 12.sp, color = BodyText, lineHeight = 18.sp)
        }
    }
}

// ━━━━━━━ TAB 5: PROFILE ━━━━━━━
@Composable
fun LearnerProfileScreen(
    viewModel: MainViewModel,
    user: UserEntity,
    onNavigateToUpgrade: () -> Unit,
    onLogout: () -> Unit
) {
    val enrollments by viewModel.userEnrollments.collectAsState()
    val completedCount = remember(enrollments) { enrollments.filter { it.progress >= 100 }.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(IndigoPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = user.name.take(1), style = MaterialTheme.typography.displayMedium, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = user.name, style = MaterialTheme.typography.headlineMedium, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = user.email, color = BodyText, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(10.dp))

        // Subscription Tier Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(if (user.subscription == "Pro") EmeraldSecondary.copy(alpha = 0.2f) else DarkCardBg)
                .border(
                    1.dp,
                    if (user.subscription == "Pro") EmeraldSecondary else CardBorderColor,
                    RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${user.subscription} Student Tier",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (user.subscription == "Pro") EmeraldSecondary else BodyText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row Widget
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(label = "Enrolled", count = "${enrollments.filter { !it.wishlist }.size}", modifier = Modifier.weight(1f))
            ProfileStatCard(label = "Finished", count = "$completedCount", modifier = Modifier.weight(1f))
            ProfileStatCard(label = "XP Points", count = "${user.xp}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Upgrade Card for Free Tier
        if (user.subscription == "Free") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToUpgrade() }
                    .border(1.dp, IndigoPrimary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Upgrade to EduCore Pro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Unlock unlimited enrollments, certified credits, & mentor support", fontSize = 10.sp, color = BodyText)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BodyText)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings items list
        SettingsItem(label = "Notification Manager", icon = Icons.Default.NotificationsActive)
        SettingsItem(label = "Offline Course Downloads", icon = Icons.Default.CloudDownload)
        SettingsItem(label = "Language Preferences", icon = Icons.Default.Translate)
        SettingsItem(label = "Privacy & Core Terms", icon = Icons.Default.Lock)
        SettingsItem(label = "Sign Out Account", icon = Icons.Default.ExitToApp, isDanger = true) {
            onLogout()
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileStatCard(label: String, count: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 10.sp, color = BodyText)
        }
    }
}

@Composable
fun SettingsItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDanger: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) RedDanger else IndigoPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontSize = 13.sp, color = if (isDanger) RedDanger else HeadingText)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
    }
}

// ━━━━━━━ SCREEN 8: PREMIUM UPGRADE ━━━━━━━
@Composable
fun PremiumUpgradeScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isYearlyPlan by remember { mutableStateOf(true) }
    var stepCheckoutShow by remember { mutableStateOf(false) }
    var successConfetti by remember { mutableStateOf(false) }

    val activePrice = if (isYearlyPlan) 3999 else 499

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
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HeadingText)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(64.dp))
                
                Text(
                    text = "Unlock EduCore Pro",
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Get certified on world-class coding frameworks & design principles.",
                    color = BodyText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // Comparison Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ComparisonRow(label = "Monthly Course Limits", free = "5 Courses/mo", pro = "Unlimited access")
                        ComparisonRow(label = "Verified PDF Certificates", free = "No", pro = "Yes (Unique ID)")
                        ComparisonRow(label = "Ad-free experience", free = "Contains Ads", pro = "100% Ad-Free")
                        ComparisonRow(label = "1-on-1 scheduled mentor minutes", free = "None", pro = "30 mins/month")
                        ComparisonRow(label = "Offline lessons download", free = "No", pro = "Yes")
                    }
                }

                // Billing Term selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(50.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (!isYearlyPlan) IndigoPrimary else Color.Transparent)
                            .clickable { isYearlyPlan = false }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Monthly (₹499)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isYearlyPlan) IndigoPrimary else Color.Transparent)
                            .clickable { isYearlyPlan = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Yearly (₹3,999 - Save 33%)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Pricing total display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Price: ₹$activePrice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = HeadingText
                    )
                    Text(text = if (isYearlyPlan) "Billed annually" else "Billed monthly", fontSize = 12.sp, color = BodyText)
                }

                Button(
                    onClick = { stepCheckoutShow = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("Upgrade to Pro Now", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Simulated Gateway Checkout sheet modal
            if (stepCheckoutShow) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { stepCheckoutShow = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {}
                            .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("EduCore Billing Integration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
                            Text("Secure Payment Gateway Powered by Razorpay", fontSize = 11.sp, color = BodyText)
                            
                            Divider(color = CardBorderColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Upgrade Term", color = BodyText, fontSize = 13.sp)
                                Text(if (isYearlyPlan) "Yearly Subscription" else "Monthly Subscription", color = HeadingText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Collected", color = BodyText, fontSize = 13.sp)
                                Text("₹$activePrice", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.upgradeToProInstant(if (isYearlyPlan) "Yearly" else "Monthly")
                                    stepCheckoutShow = false
                                    successConfetti = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
                            ) {
                                Text("Confirm Payment of ₹$activePrice", fontWeight = FontWeight.Bold)
                            }

                            Text("By confirming, you agree to our standard recursive billing terms. Cancellable anytime.", fontSize = 10.sp, color = MutedText, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Confetti success animations popup
            if (successConfetti) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { successConfetti = false },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(24.dp)
                            .border(1.dp, EmeraldSecondary, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Upgrade Successful!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = HeadingText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Congratulations! You are now a verified Pro learner. Access unlimited study sessions.", fontSize = 12.sp, color = BodyText, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    successConfetti = false
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Back to study modules")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, free: String, pro: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HeadingText)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Free plan: $free", color = BodyText, fontSize = 11.sp)
            Text("Pro plan: $pro", color = EmeraldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
