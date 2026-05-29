package com.example.ui.admin

import com.example.util.showToast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

// ─── CONTENT MANAGER ─────────────────────────────────────

@Composable
fun AdminContentManagerScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Banners", "Featured Courses", "Announcement")

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) { SectionHeader("Content Manager", "Visual content across the app") }
        TabRow(selectedTabIndex = selectedTab, containerColor = DarkCardBg, contentColor = IndigoPrimary) {
            tabs.forEachIndexed { i, t -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, fontSize = 12.sp) }) }
        }
        when (selectedTab) {
            0 -> BannersTab(viewModel)
            1 -> FeaturedCoursesTab(viewModel)
            2 -> AnnouncementTab(viewModel)
        }
    }
}

@Composable
fun BannersTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val banners by viewModel.allBannersList.collectAsState()
    var newTitle by remember { mutableStateOf("") }
    var newSubtitle by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }
    var newBtnLabel by remember { mutableStateOf("Explore") }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Homepage Banners (${banners.size}/5)", fontWeight = FontWeight.Bold, color = HeadingText)
            Spacer(Modifier.height(12.dp))
        }
        items(banners) { banner ->
            Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(banner.subtitle, fontSize = 11.sp, color = BodyText) }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(checked = banner.isEnabled, onCheckedChange = { viewModel.adminToggleBanner(banner.id, it) }, colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary))
                            IconButton(onClick = { /* delete */ }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = RedDanger, modifier = Modifier.size(16.dp)) }
                        }
                    }
                    if (banner.imageUrl.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(8.dp)).background(IndigoGlow)) {
                            Text(banner.imageUrl, fontSize = 10.sp, color = BodyText, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Add New Banner", fontWeight = FontWeight.Bold, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            EduTextField("Banner Title", newTitle) { newTitle = it }
            Spacer(Modifier.height(8.dp))
            EduTextField("Subtitle", newSubtitle) { newSubtitle = it }
            Spacer(Modifier.height(8.dp))
            EduTextField("Image URL (Unsplash or upload link)", newImageUrl) { newImageUrl = it }
            Spacer(Modifier.height(8.dp))
            EduTextField("Button Label", newBtnLabel) { newBtnLabel = it }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { if (newTitle.isNotEmpty()) { viewModel.adminAddBanner(newImageUrl, newTitle, newSubtitle, newBtnLabel); newTitle=""; newSubtitle=""; newImageUrl=""; context.showToast("Banner added!") } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("+ Add Banner") }
        }
    }
}

@Composable
fun FeaturedCoursesTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()
    val featured = courses.filter { it.isFeatured }.sortedBy { it.featuredOrder }
    val nonFeatured = courses.filter { !it.isFeatured && it.status == "Published" }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Featured Courses (${featured.size})", fontWeight = FontWeight.Bold, color = HeadingText) }
        items(featured) { course ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkCardBg).border(1.dp, AmberWarning.copy(0.5f), RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(course.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1); Text(course.instructorName, fontSize = 10.sp, color = BodyText) }
                IconButton(onClick = { viewModel.adminSetCourseFeatured(course.id, false); context.showToast("Removed from featured") }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, null, tint = RedDanger, modifier = Modifier.size(14.dp)) }
            }
        }
        item { Spacer(Modifier.height(8.dp)); Text("Add to Featured", fontWeight = FontWeight.Bold, color = HeadingText) }
        items(nonFeatured.take(10)) { course ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(DarkCardBg).border(1.dp, CardBorderColor, RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(course.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1); Text(course.instructorName, fontSize = 10.sp, color = BodyText) }
                IconButton(onClick = { viewModel.adminSetCourseFeatured(course.id, true, featured.size); context.showToast("Added to featured!") }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = EmeraldSecondary, modifier = Modifier.size(14.dp)) }
            }
        }
    }
}

@Composable
fun AnnouncementTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    var announcementText by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Global Announcement Banner", fontWeight = FontWeight.Bold, color = HeadingText)
        Spacer(Modifier.height(4.dp))
        Text("Shown at the top of the app for all users when enabled.", fontSize = 12.sp, color = BodyText)
        Spacer(Modifier.height(16.dp))
        EduTextField("Announcement text (e.g. 🎉 Sale — 50% off!)", announcementText, maxLines = 3) { announcementText = it }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Announcement", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Switch(checked = isEnabled, onCheckedChange = { isEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary))
        }
        Spacer(Modifier.height(12.dp))
        if (announcementText.isNotEmpty() && isEnabled) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(IndigoPrimary).padding(12.dp)) {
                Text("Preview: $announcementText", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
        }
        Button(onClick = { viewModel.adminSaveSetting("announcement_text", announcementText); viewModel.adminSaveSetting("announcement_enabled", isEnabled.toString()); context.showToast("Announcement saved!") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Save Announcement") }
    }
}

// ─── LIVE SESSION CONTROL ─────────────────────────────────

@Composable
fun AdminLiveSessionControlScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val sessions by viewModel.allSessionsList.collectAsState()
    val allUsers by viewModel.allUsersList.collectAsState()
    val instructors = remember(allUsers) { allUsers.filter { it.role == "Instructor" && it.isApproved } }
    var filter by remember { mutableStateOf("All") }
    var showCreateForm by remember { mutableStateOf(false) }
    var selectedInstructorId by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-06-01 14:00") }
    var duration by remember { mutableStateOf("1 Hour") }
    var capacity by remember { mutableStateOf("100") }
    var cancelSessionId by remember { mutableStateOf<Int?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    if (cancelSessionId != null) {
        AlertDialog(onDismissRequest = { cancelSessionId = null }, containerColor = DarkCardBg,
            title = { Text("Cancel Session", color = HeadingText) },
            text = { EduTextField("Reason for cancellation", cancelReason, maxLines = 2) { cancelReason = it } },
            confirmButton = { Button(onClick = { viewModel.adminCancelSession(cancelSessionId!!, cancelReason); cancelSessionId = null; cancelReason = ""; context.showToast("Session cancelled") }, colors = ButtonDefaults.buttonColors(containerColor = RedDanger)) { Text("Cancel Session") } },
            dismissButton = { TextButton(onClick = { cancelSessionId = null }) { Text("Keep") } }
        )
    }

    val filtered = remember(sessions, filter) {
        when (filter) { "Upcoming" -> sessions.filter { it.status == "Upcoming" }; "Live" -> sessions.filter { it.status == "Live" }; "Completed" -> sessions.filter { it.status == "Past" }; "Cancelled" -> sessions.filter { it.status == "Cancelled" }; else -> sessions }
    }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader("Live Sessions", "${sessions.size} total")
                Button(onClick = { showCreateForm = !showCreateForm }, colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary), modifier = Modifier.height(36.dp)) { Text(if (showCreateForm) "Close" else "+ Create", fontSize = 12.sp) }
            }
        }

        if (showCreateForm) {
            item {
                Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Create Session for Instructor", fontWeight = FontWeight.Bold, color = HeadingText)
                        EduDropdown("Assign to Instructor", instructors.firstOrNull { it.email == selectedInstructorId }?.name ?: "Select instructor", instructors.map { it.name }) { name -> selectedInstructorId = instructors.firstOrNull { it.name == name }?.email ?: "" }
                        EduTextField("Session Topic", topic) { topic = it }
                        EduTextField("Date & Time (YYYY-MM-DD HH:MM)", date) { date = it }
                        EduDropdown("Duration", duration, listOf("30 Min","1 Hour","1.5 Hours","2 Hours")) { duration = it }
                        EduTextField("Max Participants", capacity, isNumber = true) { capacity = it }
                        Button(onClick = {
                            val inst = instructors.firstOrNull { it.email == selectedInstructorId }
                            if (inst != null && topic.isNotEmpty()) { viewModel.adminCreateSessionForInstructor(inst.email, inst.name, topic, date, duration, capacity.toIntOrNull() ?: 100); topic = ""; showCreateForm = false; context.showToast("Session created!") }
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)) { Text("Schedule Session") }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All","Upcoming","Live","Completed","Cancelled")) { f -> FilterChip(f, filter == f) { filter = f } }
            }
        }

        items(filtered) { session ->
            Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(session.topic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${session.instructorName} • ${session.scheduledAt}", fontSize = 11.sp, color = BodyText)
                            Text("${session.enrolledCount}/${session.maxParticipants} enrolled • ${session.duration}", fontSize = 11.sp, color = MutedText)
                            if (session.createdByAdmin) Text("Created by Admin", fontSize = 10.sp, color = IndigoPrimary)
                        }
                        StatusBadge(session.status)
                    }
                    if (session.status == "Upcoming") {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmallButton("Cancel", RedDanger) { cancelSessionId = session.id }
                        }
                    }
                    if (session.cancelReason.isNotEmpty()) { Spacer(Modifier.height(6.dp)); Text("Reason: ${session.cancelReason}", fontSize = 10.sp, color = RedDanger) }
                }
            }
        }
    }
}
