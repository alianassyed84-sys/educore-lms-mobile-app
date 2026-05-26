package com.example.ui.instructor

import android.widget.Toast
import com.example.util.showToast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// ━━━━━━━ SECTIONS ROOT CONTROLLER ━━━━━━━
@Composable
fun InstructorMainScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Dashboard") }
    val currentUserState by viewModel.currentUser.collectAsState()
    val user = currentUserState ?: return

    var showCourseWizard by remember { mutableStateOf(false) }

    if (showCourseWizard) {
        CourseBuilderWizard(
            viewModel = viewModel,
            onDismiss = { showCourseWizard = false }
        )
    } else {
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                Column(modifier = Modifier.background(DarkCardBg)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Instructor Studio", fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 16.sp)
                        }

                        IconButton(onClick = onLogout) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit", tint = RedDanger)
                        }
                    }

                    // Top Tab row selectors
                    ScrollableTabRow(
                        selectedTabIndex = when (activeTab) {
                            "Dashboard" -> 0
                            "Courses" -> 1
                            "Analytics" -> 2
                            "Live" -> 3
                            else -> 4
                        },
                        containerColor = DarkCardBg,
                        contentColor = IndigoPrimary,
                        edgePadding = 12.dp
                    ) {
                        val tabs = listOf("Dashboard", "Courses", "Analytics", "Live", "Payouts")
                        tabs.forEach { tabName ->
                            Tab(
                                selected = activeTab == tabName,
                                onClick = { activeTab = tabName },
                                text = { Text(tabName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeTab == tabName) IndigoPrimary else BodyText) }
                            )
                        }
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
                    "Dashboard" -> InstructorHomeScreen(viewModel, user, onCreateCourseClick = { showCourseWizard = true }, onNavigateTab = { activeTab = it })
                    "Courses" -> InstructorCoursesListScreen(viewModel, user, onCreateCourseClick = { showCourseWizard = true })
                    "Analytics" -> InstructorAnalyticsScreen(viewModel)
                    "Live" -> LiveSessionManagerScreen(viewModel)
                    "Payouts" -> InstructorPayoutsScreen(viewModel, user)
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 1: INSTRUCTOR HOME ━━━━━━━
@Composable
fun InstructorHomeScreen(
    viewModel: MainViewModel,
    user: UserEntity,
    onCreateCourseClick: () -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val courses by viewModel.allCoursesList.collectAsState()
    val instructorCourses = remember(courses) { courses.filter { it.instructorId == user.email } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome back, ${user.name}",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track your course metrics and update lessons in real-time.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 13.sp,
                color = BodyText
            )
        }

        // Stats grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeStatBox(label = "Monthly Revenues", value = "₹24,500", icon = Icons.Default.TrendingUp, valueColor = EmeraldSecondary, modifier = Modifier.weight(1f))
                    HomeStatBox(label = "Active Students", value = "1,240", icon = Icons.Default.Groups, valueColor = HeadingText, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeStatBox(label = "Aggregate Rating", value = "4.8 ★", icon = Icons.Default.Star, valueColor = AmberWarning, modifier = Modifier.weight(1f))
                    HomeStatBox(label = "Courses Published", value = "${instructorCourses.size}", icon = Icons.Default.Laptop, valueColor = HeadingText, modifier = Modifier.weight(1f))
                }
            }
        }

        // Quick Actions Row
        item {
            Text(text = "Quick Studio Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCreateCourseClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Create Course", fontSize = 11.sp, color = HeadingText)
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateTab("Live") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Podcasts, contentDescription = null, tint = EmeraldSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Schedule Live", fontSize = 11.sp, color = HeadingText)
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateTab("Analytics") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.QueryStats, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Analytics", fontSize = 11.sp, color = HeadingText)
                    }
                }
            }
        }

        // Recent Activity FEED
        item {
            Text(text = "Recent Activity Feed", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActivityFeedRow("Pooja Patil enrolled in Python Masters", "5 mins ago", EmeraldSecondary)
                    ActivityFeedRow("Received 5★ Review from Rohit Sharma", "1 hour ago", AmberWarning)
                    ActivityFeedRow("Payout request of ₹15,000 sent to admin", "Yesterday", IndigoPrimary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeStatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, color = BodyText, fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = valueColor)
        }
    }
}

@Composable
fun ActivityFeedRow(msg: String, time: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(tint))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = msg, fontSize = 12.sp, color = HeadingText, maxLines = 1)
        }
        Text(text = time, fontSize = 10.sp, color = MutedText)
    }
}

// ━━━━━━━ SCREEN 2: COURSES LIST SCREEN ━━━━━━━
@Composable
fun InstructorCoursesListScreen(
    viewModel: MainViewModel,
    user: UserEntity,
    onCreateCourseClick: () -> Unit
) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()
    val instructorCourses = remember(courses) { courses.filter { it.instructorId == user.email } }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateCourseClick,
                containerColor = IndigoPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Course")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "My Course Catalog", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (instructorCourses.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MutedText, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No courses created yet. Tap the '+' FAB below to start building your first course step-by-step!", color = BodyText, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(instructorCourses) { course ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "${course.category} •   ₹${course.price}", fontSize = 11.sp, color = BodyText)
                                    }

                                    // Status Badge
                                    val statusColor = when (course.status) {
                                        "Approved" -> EmeraldSecondary
                                        "Pending" -> AmberWarning
                                        else -> RedDanger
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(statusColor.copy(alpha = 0.2f))
                                            .border(1.dp, statusColor, RoundedCornerShape(50.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = course.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row {
                                        Text(text = "Enrolled: ", fontSize = 12.sp, color = BodyText)
                                        Text(text = "${course.enrolledCount} students", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    Row {
                                        Text(text = "Rating: ", fontSize = 12.sp, color = BodyText)
                                        Text(text = "${course.rating} ★", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { context.showToast("Editing Course Content currently disabled.") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, CardBorderColor)
                                    ) {
                                        Text("Edit Details", fontSize = 11.sp, color = HeadingText)
                                    }

                                    OutlinedButton(
                                        onClick = { context.showToast("Underdevelopment Analytics Drilldown.") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, CardBorderColor)
                                    ) {
                                        Text("Analytics", fontSize = 11.sp, color = HeadingText)
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

// ━━━━━━━ SCREEN 3: COURSE BUILDER WIZARD ━━━━━━━
@Composable
fun CourseBuilderWizard(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(1) } // 1 of 4

    // Form states
    var courseTitle by remember { mutableStateOf("") }
    var courseDesc by remember { mutableStateOf("") }
    var courseCategory by remember { mutableStateOf("Coding") }
    var difficulty by remember { mutableStateOf("Beginner") }
    var isPaid by remember { mutableStateOf(false) }
    var priceStr by remember { mutableStateOf("") }

    // Curriculum Builder
    var sectionTitle by remember { mutableStateOf("") }
    val lessonsDraft = remember { mutableStateListOf<LessonEntity>() }
    var lessonTitleDraft by remember { mutableStateOf("") }
    var lessonTypeDraft by remember { mutableStateOf("Video") }
    var lessonDurationDraft by remember { mutableStateOf("10:00") }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (step > 1) step-- else onDismiss() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HeadingText)
                }

                Text(text = "Step $step of 4", fontWeight = FontWeight.Bold, color = IndigoPrimary)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                when (step) {
                    1 -> {
                        Text(text = "Basic Information", style = MaterialTheme.typography.displayMedium, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = courseTitle,
                            onValueChange = { courseTitle = it },
                            label = { Text("Course Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = courseDesc,
                            onValueChange = { courseDesc = it },
                            label = { Text("Course Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Selector Dropdown simplified
                        Text(text = "Select Category", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BodyText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Coding", "Design", "DevOps").forEach { category ->
                                val selected = courseCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (selected) IndigoPrimary else DarkCardBg)
                                        .border(1.dp, CardBorderColor, RoundedCornerShape(50.dp))
                                        .clickable { courseCategory = category }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = category, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Difficulty Level
                        Text(text = "Difficulty Level", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BodyText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                                val selected = difficulty == level
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (selected) IndigoPrimary else DarkCardBg)
                                        .border(1.dp, CardBorderColor, RoundedCornerShape(50.dp))
                                        .clickable { difficulty = level }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = level, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    2 -> {
                        Text(text = "Curriculum Builder", style = MaterialTheme.typography.displayMedium, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = sectionTitle,
                            onValueChange = { sectionTitle = it },
                            label = { Text("Default Section / Chapter Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add Lesson Small Sub-form
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Add Custom Lesson", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = lessonTitleDraft,
                                    onValueChange = { lessonTitleDraft = it },
                                    label = { Text("Lesson Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("Video", "Article", "Quiz").forEach { type ->
                                        val selected = lessonTypeDraft == type
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(if (selected) IndigoPrimary else DarkBg)
                                                .clickable { lessonTypeDraft = type }
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = type, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (lessonTitleDraft.isBlank()) return@Button
                                        lessonsDraft.add(
                                            LessonEntity(
                                                courseId = 0, // Assigned later
                                                sectionName = sectionTitle.ifEmpty { "Chapter 1: Overview" },
                                                title = lessonTitleDraft,
                                                type = lessonTypeDraft,
                                                duration = lessonDurationDraft
                                            )
                                        )
                                        lessonTitleDraft = ""
                                        context.showToast("Lesson added successfully!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                ) {
                                    Text("Add Lesson")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // List added lessons
                        Text(text = "Lessons Added (${lessonsDraft.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        lessonsDraft.forEachIndexed { idx, lesson ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCardBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${idx + 1}.   ${lesson.title} (${lesson.type})", fontSize = 12.sp, color = HeadingText)
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = RedDanger,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { lessonsDraft.removeAt(idx) }
                                )
                            }
                        }
                    }
                    3 -> {
                        Text(text = "Set Price Tag", style = MaterialTheme.typography.displayMedium, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardBg)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Does this course cost money?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Switch(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSecondary)
                            )
                        }

                        if (isPaid) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { priceStr = it },
                                label = { Text("Price in Indian Rupees (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    4 -> {
                        Text(text = "Review & Submit", style = MaterialTheme.typography.displayMedium, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Summary of Information", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Title: $courseTitle", fontSize = 13.sp, color = HeadingText)
                        Text(text = "Category: $courseCategory", fontSize = 13.sp, color = BodyText)
                        Text(text = "Price: ${if (isPaid) "₹$priceStr" else "Free"}", fontSize = 13.sp, color = HeadingText)
                        Text(text = "Total Lessons: ${lessonsDraft.size}", fontSize = 13.sp, color = BodyText)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submission checklist
                        Text(text = "Publishing Checklist", fontWeight = FontWeight.Bold, color = HeadingText)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChecklistIndicator("Course title provided", courseTitle.isNotBlank())
                        ChecklistIndicator("Course description provided", courseDesc.isNotBlank())
                        ChecklistIndicator("Curriculum has at least 1 lesson", lessonsDraft.isNotEmpty())
                    }
                }
            }

            // Bottom Flow Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeadingText),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            if (step == 1 && courseTitle.isBlank()) {
                                context.showToast("Please enter course title.")
                                return@Button
                            }
                            step++
                        } else {
                            // Publish Action
                            val finalPrice = if (isPaid) priceStr.toIntOrNull() ?: 0 else 0
                            viewModel.publishCourseByInstructor(
                                courseTitle,
                                courseDesc,
                                courseCategory,
                                difficulty,
                                finalPrice,
                                lessonsDraft
                            )
                            context.showToast("Course successfully submitted for review!", Toast.LENGTH_LONG)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text(text = if (step == 4) "Submit for Admin Review" else "Next Step")
                }
            }
        }
    }
}

@Composable
fun ChecklistIndicator(label: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
            contentDescription = null,
            tint = if (checked) EmeraldSecondary else RedDanger,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, fontSize = 12.sp, color = if (checked) HeadingText else BodyText)
    }
}

// ━━━━━━━ SCREEN 4: ANALYTICS SCREEN ━━━━━━━
@Composable
fun InstructorAnalyticsScreen(
    viewModel: MainViewModel
) {
    var rangeSelected by remember { mutableStateOf("Last 30 Days") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Performance Ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Range Select Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Last 7 Days", "Last 30 Days", "All Time").forEach { term ->
                    val isSelected = term == rangeSelected
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) IndigoPrimary else DarkCardBg)
                            .border(1.dp, CardBorderColor, RoundedCornerShape(50.dp))
                            .clickable { rangeSelected = term }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = term, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // Revenue Chart Simulation Row
        item {
            Text(text = "Revenues Over Time ($rangeSelected)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                // Line chart simulator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.8f)
                            lineTo(size.width * 0.3f, size.height * 0.6f)
                            lineTo(size.width * 0.6f, size.height * 0.9f)
                            lineTo(size.width * 0.8f, size.height * 0.3f)
                            lineTo(size.width, size.height * 0.1f)
                        }
                        drawPath(
                            path = path,
                            color = EmeraldSecondary,
                            style = Stroke(width = 8f)
                        )
                    }
                    Text(text = "Total Collected: ₹24,500", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
                }
            }
        }

        // Custom demography stats breakdown
        item {
            Text(text = "Demographics & User Base", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("India (UPI Channels)", fontSize = 12.sp, color = BodyText)
                        Text("78% representation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("USA & Global (Cards)", fontSize = 12.sp, color = BodyText)
                        Text("22% representation", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Divider(color = CardBorderColor)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Skill Level: Beginners", fontSize = 12.sp, color = BodyText)
                        Text("65%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ━━━━━━━ SCREEN 5: LIVE SESSION MANAGER ━━━━━━━
@Composable
fun LiveSessionManagerScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val liveSessions by viewModel.allSessionsList.collectAsState()

    var showScheduleForm by remember { mutableStateOf(false) }

    // Live form fields
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("2026-05-30") }
    var durationSelected by remember { mutableStateOf("1 Hour") }

    if (showScheduleForm) {
        AlertDialog(
            onDismissRequest = { showScheduleForm = false },
            title = { Text("Schedule Live Lecture", color = HeadingText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Topic Name") })
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description Details") })
                    OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Scheduled Date (YYYY-MM-DD)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topic.isBlank()) return@Button
                        viewModel.scheduleLiveStream(topic, description, dateStr, durationSelected, 100)
                        topic = ""
                        description = ""
                        showScheduleForm = false
                        context.showToast("Session Scheduled successfully!")
                    }
                ) {
                    Text("Schedule Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleForm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showScheduleForm = true },
                containerColor = IndigoPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Class, contentDescription = "Schedule Live")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Scheduled Stream Rooms", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(
                    onClick = {
                        context.showToast("Simulated Camera & Voice stream connected!", Toast.LENGTH_LONG)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)
                ) {
                    Text("Go Live Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (liveSessions.isEmpty()) {
                Text("No upcoming scheduled sessions.", color = MutedText, fontSize = 12.sp)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(liveSessions) { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = session.topic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(EmeraldSecondary.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = session.status, color = EmeraldSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(text = session.description, color = BodyText, fontSize = 12.sp, maxLines = 2)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Scheduled: ${session.scheduledAt}", fontSize = 11.sp, color = MutedText)
                                    Text(text = "Capacity: ${session.enrolledCount} attending", fontSize = 11.sp, color = MutedText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━ SCREEN 6: PAYOUTS SCREEN ━━━━━━━
@Composable
fun InstructorPayoutsScreen(
    viewModel: MainViewModel,
    user: UserEntity
) {
    val context = LocalContext.current
    val corporatePayouts by viewModel.allPayoutsList.collectAsState()
    val instructorPayouts = remember(corporatePayouts) {
        corporatePayouts.filter { it.instructorId == user.email }
    }

    var amtInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Earnings & Transfers", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        // Ledger Card Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Brush.horizontalGradient(listOf(EmeraldSecondary, IndigoPrimary)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Withdrawable Royalty Balance", color = BodyText, fontSize = 11.sp)
                Text("₹15,000", style = MaterialTheme.typography.displayMedium, color = EmeraldSecondary, fontSize = 32.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "EduCore handles billing & global conversions securely. Platform Commission rate is set at 30% standard. You receive 70% direct royalty payout splits.",
                    fontSize = 11.sp,
                    color = BodyText,
                    lineHeight = 16.sp
                )
            }
        }

        // Withdrawal Request panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Request Quick Payout Split", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amtInput,
                    onValueChange = { amtInput = it },
                    placeholder = { Text("Amount in Rupees (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary, unfocusedBorderColor = CardBorderColor)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (amtInput.isBlank()) {
                            context.showToast("Please enter withdrawal amount requested.")
                            return@Button
                        }
                        viewModel.requestInstructorWithdrawal(amtInput) { success, msg ->
                            context.showToast(msg, Toast.LENGTH_LONG)
                            if (success) amtInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger Bank Transfer Request")
                }
            }
        }

        // Payout history logging
        Text(text = "Payout History Records", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        if (instructorPayouts.isEmpty()) {
            Text("No transactions logged yet.", color = MutedText, fontSize = 12.sp)
        } else {
            instructorPayouts.forEach { history ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardBg)
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Rupees Requested: ₹${history.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Date: May 2026", fontSize = 11.sp, color = MutedText)
                    }

                    // Payout Badge status
                    val bColor = when (history.status) {
                        "Paid" -> EmeraldSecondary
                        "Pending" -> AmberWarning
                        else -> RedDanger
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(bColor.copy(alpha = 0.2f))
                            .border(1.dp, bColor, RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = history.status, color = bColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
