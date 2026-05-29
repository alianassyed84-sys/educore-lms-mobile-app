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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

// ─── NOTIFICATIONS ────────────────────────────────────────

@Composable
fun AdminNotificationsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsersList.collectAsState()
    val sentHistory by viewModel.allSentNotifications.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("All Users") }
    var notifType by remember { mutableStateOf("In-App") }

    val targets = listOf("All Users","All Students","All Instructors","All Pro Users","All Free Users")
    val types = listOf("In-App","Push","Email","All Three")

    fun resolveTargetUsers(): List<UserEntity> = when (target) {
        "All Students" -> allUsers.filter { it.role == "Learner" }
        "All Instructors" -> allUsers.filter { it.role == "Instructor" }
        "All Pro Users" -> allUsers.filter { it.subscription == "Pro" }
        "All Free Users" -> allUsers.filter { it.subscription == "Free" }
        else -> allUsers
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) { SectionHeader("Notifications & Announcements") }
        TabRow(selectedTabIndex = selectedTab, containerColor = DarkCardBg, contentColor = IndigoPrimary) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Send", fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("History (${sentHistory.size})", fontSize = 12.sp) })
        }

        when (selectedTab) {
            0 -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EduDropdown("Target Audience", target, targets) { target = it }
                val resolved = resolveTargetUsers()
                Text("Will reach ${resolved.size} users", fontSize = 12.sp, color = EmeraldSecondary, fontWeight = FontWeight.Medium)
                EduDropdown("Notification Type", notifType, types) { notifType = it }
                EduTextField("Title (max 60 chars)", notifTitle) { if (it.length <= 60) notifTitle = it }
                Text("${notifTitle.length}/60", fontSize = 10.sp, color = MutedText)
                EduTextField("Message (max 200 chars)", notifMessage, maxLines = 3) { if (it.length <= 200) notifMessage = it }
                Text("${notifMessage.length}/200", fontSize = 10.sp, color = MutedText)

                if (notifTitle.isNotEmpty() && notifMessage.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth().border(1.dp, IndigoPrimary, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = IndigoGlow)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Preview", fontSize = 10.sp, color = IndigoPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(notifTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(notifMessage, fontSize = 12.sp, color = BodyText)
                        }
                    }
                }

                Button(onClick = {
                    if (notifTitle.isEmpty() || notifMessage.isEmpty()) { context.showToast("Fill title and message"); return@Button }
                    viewModel.adminSendNotification(notifTitle, notifMessage, target, notifType, resolveTargetUsers())
                    context.showToast("Notification sent to ${resolveTargetUsers().size} users!")
                    notifTitle = ""; notifMessage = ""
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Send Notification")
                }
            }
            1 -> LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (sentHistory.isEmpty()) {
                    item { Text("No notifications sent yet.", color = MutedText, fontSize = 13.sp) }
                }
                items(sentHistory) { n ->
                    Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                StatusBadge(n.notificationType)
                            }
                            Text(n.message, fontSize = 12.sp, color = BodyText, modifier = Modifier.padding(top = 4.dp))
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("→ ${n.target}", fontSize = 10.sp, color = MutedText)
                                Text("${n.deliveryCount} delivered • ${(n.openRate * 100).toInt()}% opened", fontSize = 10.sp, color = MutedText)
                            }
                            Text(formatTimestamp(n.sentAt), fontSize = 10.sp, color = MutedText)
                        }
                    }
                }
            }
        }
    }
}

// ─── ANALYTICS ───────────────────────────────────────────

@Composable
fun AdminAnalyticsScreen(viewModel: MainViewModel) {
    val allUsers by viewModel.allUsersList.collectAsState()
    val courses by viewModel.allCoursesList.collectAsState()
    val payouts by viewModel.allPayoutsList.collectAsState()
    val sessions by viewModel.allSessionsList.collectAsState()

    val totalRevenue = payouts.filter { it.status == "Paid" }.sumOf { it.amount }
    val proUsers = allUsers.count { it.subscription == "Pro" }
    val freeUsers = allUsers.count { it.subscription == "Free" }
    val conversionRate = if (allUsers.isNotEmpty()) (proUsers * 100f / allUsers.size) else 0f
    val topCourses = courses.sortedByDescending { it.enrolledCount }.take(5)
    val topByRating = courses.sortedByDescending { it.rating }.take(5)

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionHeader("Reports & Analytics", "Platform-wide performance data") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPICard("Revenue", "₹${totalRevenue.formatNum()}", EmeraldSecondary, Icons.Default.CurrencyRupee, Modifier.weight(1f))
                KPICard("Conversion", "${conversionRate.toInt()}%", IndigoPrimary, Icons.Default.TrendingUp, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KPICard("Pro Users", "$proUsers", AmberWarning, Icons.Default.Star, Modifier.weight(1f))
                KPICard("Free Users", "$freeUsers", BodyText, Icons.Default.Person, Modifier.weight(1f))
            }
        }

        item {
            Text("Revenue Trend (6-Month Simulated)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth().height(160.dp).border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val pts = listOf(0.9f, 0.7f, 0.5f, 0.65f, 0.35f, 0.1f)
                        val path = androidx.compose.ui.graphics.Path()
                        pts.forEachIndexed { i, v ->
                            val x = size.width * (i / (pts.size - 1).toFloat())
                            val y = size.height * v
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, EmeraldSecondary, style = Stroke(width = 6f))
                        pts.forEachIndexed { i, v ->
                            val x = size.width * (i / (pts.size - 1).toFloat())
                            val y = size.height * v
                            drawCircle(EmeraldSecondary, 8f, androidx.compose.ui.geometry.Offset(x, y))
                        }
                    }
                }
            }
        }

        item {
            Text("Top Courses by Enrollment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            topCourses.forEachIndexed { i, c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${i+1}", fontWeight = FontWeight.Bold, color = IndigoPrimary, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text("${c.instructorName} • ⭐${c.rating}", fontSize = 10.sp, color = BodyText)
                    }
                    Text("${c.enrolledCount}", fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                }
                Divider(color = CardBorderColor, modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        item {
            Text("Top Courses by Rating", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            topByRating.forEachIndexed { i, c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${i+1}", fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                    Column(Modifier.weight(1f)) { Text(c.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1); Text(c.instructorName, fontSize = 10.sp, color = BodyText) }
                    Text("⭐${c.rating}", fontWeight = FontWeight.Bold, color = AmberWarning)
                }
                Divider(color = CardBorderColor, modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        item {
            Text("Platform Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            val rows = listOf(
                "Total Users" to "${allUsers.size}",
                "Total Courses" to "${courses.size}",
                "Published Courses" to "${courses.count { it.status == "Published" }}",
                "Pending Review" to "${courses.count { it.status == "Pending" }}",
                "Total Sessions" to "${sessions.size}",
                "Upcoming Sessions" to "${sessions.count { it.status == "Upcoming" }}"
            )
            rows.forEach { (k, v) -> InfoRow(k, v) }
        }
    }
}

// ─── PLATFORM SETTINGS ───────────────────────────────────

@Composable
fun AdminPlatformSettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var appName by remember { mutableStateOf("EduCore") }
    var commissionRate by remember { mutableStateOf("30") }
    var minPayout by remember { mutableStateOf("1000") }
    var refundWindow by remember { mutableStateOf("7") }
    var freeLimit by remember { mutableStateOf("5") }
    var maintenanceMode by remember { mutableStateOf(false) }
    var liveEnabled by remember { mutableStateOf(true) }
    var certsEnabled by remember { mutableStateOf(true) }
    var referralEnabled by remember { mutableStateOf(true) }
    var reviewsEnabled by remember { mutableStateOf(true) }
    var leaderboardEnabled by remember { mutableStateOf(true) }
    var offlineEnabled by remember { mutableStateOf(true) }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionHeader("Platform Settings", "Configure global app behavior") }

        item {
            SettingsSection("General") {
                EduTextField("App Name", appName) { appName = it }
                Spacer(Modifier.height(8.dp))
                EduTextField("Support Email", "support@educore.saas") {}
            }
        }

        item {
            SettingsSection("Monetization") {
                EduTextField("Platform Commission %", commissionRate, isNumber = true) { commissionRate = it }
                Spacer(Modifier.height(8.dp))
                EduTextField("Min Payout Threshold (₹)", minPayout, isNumber = true) { minPayout = it }
                Spacer(Modifier.height(8.dp))
                EduTextField("Refund Window (days)", refundWindow, isNumber = true) { refundWindow = it }
                Spacer(Modifier.height(8.dp))
                EduTextField("Free Tier Monthly Enrollment Limit", freeLimit, isNumber = true) { freeLimit = it }
            }
        }

        item {
            SettingsSection("Feature Flags") {
                ToggleRow("Live Classes", liveEnabled) { liveEnabled = it }
                ToggleRow("Certificates", certsEnabled) { certsEnabled = it }
                ToggleRow("Referral Program", referralEnabled) { referralEnabled = it }
                ToggleRow("Course Reviews", reviewsEnabled) { reviewsEnabled = it }
                ToggleRow("Student Leaderboard", leaderboardEnabled) { leaderboardEnabled = it }
                ToggleRow("Offline Downloads (Pro)", offlineEnabled) { offlineEnabled = it }
                ToggleRow("Maintenance Mode", maintenanceMode, dangerColor = true) { maintenanceMode = it }
            }
        }

        item {
            Button(onClick = {
                viewModel.adminSaveSetting("app_name", appName)
                viewModel.adminSaveSetting("commission_rate", commissionRate)
                viewModel.adminSaveSetting("min_payout", minPayout)
                viewModel.adminSaveSetting("maintenance_mode", maintenanceMode.toString())
                viewModel.adminSaveSetting("live_enabled", liveEnabled.toString())
                context.showToast("Settings saved successfully!")
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) {
                Text("Save All Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IndigoPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, dangerColor: Boolean = false, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = if (dangerColor && checked) RedDanger else HeadingText)
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = if (dangerColor) RedDanger else IndigoPrimary))
    }
    Divider(color = CardBorderColor)
}

// ─── AUDIT LOG ────────────────────────────────────────────

@Composable
fun AdminAuditLogScreen(viewModel: MainViewModel) {
    val logs by viewModel.allAdminLogsList.collectAsState()
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }

    val filtered = remember(logs, filter, search) {
        logs.filter { log ->
            (filter == "All" || log.targetType == filter) &&
            (search.isEmpty() || log.action.contains(search, true) || log.targetId.contains(search, true) || log.adminEmail.contains(search, true))
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        SectionHeader("Audit Log", "${logs.size} actions recorded")
        EduTextField("Search actions…", search) { search = it }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("All","USER","COURSE","PAYOUT","SYSTEM","NOTIFICATION","SESSION")) { f -> FilterChip(f, filter == f) { filter = f } }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filtered.isEmpty()) {
                item { Text("No audit records found.", color = MutedText, fontSize = 13.sp) }
            }
            items(filtered) { log ->
                Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(
                            when(log.targetType) { "USER" -> IndigoPrimary; "COURSE" -> AmberWarning; "PAYOUT" -> EmeraldSecondary; else -> MutedText }
                        ), contentAlignment = Alignment.Center) {
                            Icon(when(log.targetType) { "USER" -> Icons.Default.Person; "COURSE" -> Icons.Default.Book; "PAYOUT" -> Icons.Default.AttachMoney; else -> Icons.Default.Settings }, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(log.action.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HeadingText)
                            if (log.targetId.isNotEmpty()) Text("Target: ${log.targetId}", fontSize = 11.sp, color = BodyText)
                            if (log.oldValue.isNotEmpty() && log.newValue.isNotEmpty()) Text("${log.oldValue} → ${log.newValue}", fontSize = 11.sp, color = IndigoPrimary)
                            Text("by ${log.adminEmail} • ${log.ipAddress}", fontSize = 10.sp, color = MutedText)
                            Text(formatTimestamp(log.timestamp), fontSize = 10.sp, color = MutedText)
                        }
                        Box(Modifier.clip(RoundedCornerShape(50.dp)).background(
                            when(log.targetType) { "USER" -> IndigoPrimary.copy(0.15f); "COURSE" -> AmberWarning.copy(0.15f); else -> MutedText.copy(0.15f) }
                        ).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(log.targetType, fontSize = 9.sp, color = HeadingText) }
                    }
                }
            }
        }
    }
}
