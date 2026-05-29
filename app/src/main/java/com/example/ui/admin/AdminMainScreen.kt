package com.example.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class SidebarItem(val id: String, val label: String, val icon: ImageVector)

val adminSidebarItems = listOf(
    SidebarItem("overview",       "Overview",            Icons.Default.Home),
    SidebarItem("courses",        "Course Control",      Icons.Default.Book),
    SidebarItem("users",          "User Control",        Icons.Default.Group),
    SidebarItem("instructors",    "Instructor Control",  Icons.Default.School),
    SidebarItem("revenue",        "Revenue & Payouts",   Icons.Default.AccountBalanceWallet),
    SidebarItem("content",        "Content Manager",     Icons.Default.Image),
    SidebarItem("sessions",       "Live Sessions",       Icons.Default.VideoCall),
    SidebarItem("notifications",  "Notifications",       Icons.Default.Notifications),
    SidebarItem("analytics",      "Reports & Analytics", Icons.Default.BarChart),
    SidebarItem("settings",       "Platform Settings",   Icons.Default.Settings),
    SidebarItem("audit",          "Audit Log",           Icons.Default.Security),
)

// ─── Bottom nav tabs for mobile ──────────────────────────────
private data class AdminTab(val id: String, val label: String, val icon: ImageVector)
private val adminBottomTabs = listOf(
    AdminTab("overview",   "Overview",  Icons.Default.Dashboard),
    AdminTab("users",      "Users",     Icons.Default.Group),
    AdminTab("courses",    "Courses",   Icons.Default.MenuBook),
    AdminTab("revenue",    "Revenue",   Icons.Default.AccountBalanceWallet),
    AdminTab("more",       "More",      Icons.Default.MoreHoriz)
)

// ─── "More" drawer items ──────────────────────────────────────
private val moreItems = listOf(
    Triple("instructors",   "Instructors",    Icons.Default.School),
    Triple("content",       "Content",        Icons.Default.Image),
    Triple("sessions",      "Live Sessions",  Icons.Default.VideoCall),
    Triple("notifications", "Notifications",  Icons.Default.Notifications),
    Triple("analytics",     "Analytics",      Icons.Default.BarChart),
    Triple("settings",      "Settings",       Icons.Default.Settings),
    Triple("audit",         "Audit Log",      Icons.Default.Security),
)

@Composable
fun AdminMainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val currentUserState by viewModel.currentUser.collectAsState()
    currentUserState ?: return

    var activeSection by remember { mutableStateOf("overview") }
    var showMoreDrawer by remember { mutableStateOf(false) }
    val impersonated by viewModel.impersonatedUser.collectAsState()

    Box(Modifier.fillMaxSize().background(DarkBg)) {
        Column(Modifier.fillMaxSize()) {
            // ── Impersonation Banner ─────────────────────────────
            if (impersonated != null) {
                Row(
                    Modifier.fillMaxWidth().background(AmberWarning).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👁 Viewing as: ${impersonated!!.name}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TextButton(onClick = { viewModel.adminStopImpersonation() }) {
                        Text("Exit View", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Premium Top Bar ──────────────────────────────────
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0D0D1F), DarkCardBg)))
            ) {
                Box(
                    Modifier.fillMaxWidth().height(1.dp).align(Alignment.BottomStart)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, RedDanger, AmberWarning, IndigoPrimary, Color.Transparent)))
                )
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Admin shield avatar
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(RedDanger.copy(0.8f), Color(0xFF7C3AED)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Admin Console", fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 15.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(EmeraldSecondary))
                                Spacer(Modifier.width(4.dp))
                                Text("EduCore Platform", fontSize = 11.sp, color = BodyText)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val sectionLabel = adminSidebarItems.find { it.id == activeSection }?.label ?: activeSection
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceElevated).border(1.dp, CardBorderColor, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sectionLabel, fontSize = 11.sp, color = BodyText, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Logout, null, tint = RedDanger.copy(0.8f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // ── Main Content ─────────────────────────────────────
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (activeSection) {
                    "overview"      -> AdminOverviewScreen(viewModel)
                    "courses"       -> AdminCourseControlScreen(viewModel)
                    "users"         -> AdminUserControlScreen(viewModel)
                    "instructors"   -> AdminInstructorControlScreen(viewModel)
                    "revenue"       -> AdminRevenueScreen(viewModel)
                    "content"       -> AdminContentManagerScreen(viewModel)
                    "sessions"      -> AdminLiveSessionControlScreen(viewModel)
                    "notifications" -> AdminNotificationsScreen(viewModel)
                    "analytics"     -> AdminAnalyticsScreen(viewModel)
                    "settings"      -> AdminPlatformSettingsScreen(viewModel)
                    "audit"         -> AdminAuditLogScreen(viewModel)
                }
            }

            // ── Premium Bottom Nav ───────────────────────────────
            Box(Modifier.fillMaxWidth().background(DarkCardBg).navigationBarsPadding()) {
                Box(
                    Modifier.fillMaxWidth().height(1.dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, RedDanger, AmberWarning, IndigoPrimary, Color.Transparent)))
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    adminBottomTabs.forEach { tab ->
                        val isMore = tab.id == "more"
                        val selected = if (isMore) showMoreDrawer else (activeSection == tab.id && !showMoreDrawer)
                        val accentColor = when (tab.id) {
                            "overview" -> IndigoPrimary
                            "users"    -> EmeraldSecondary
                            "courses"  -> AmberWarning
                            "revenue"  -> Color(0xFF10B981)
                            else       -> BodyText
                        }
                        val scale by animateFloatAsState(if (selected) 1.1f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "scale")
                        Column(
                            Modifier.weight(1f).graphicsLayer(scaleX = scale, scaleY = scale)
                                .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                                    if (isMore) {
                                        showMoreDrawer = !showMoreDrawer
                                    } else {
                                        activeSection = tab.id
                                        showMoreDrawer = false
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(if (selected) 42.dp else 36.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) accentColor.copy(0.2f) else Color.Transparent)
                                    .then(if (selected) Modifier.border(1.dp, accentColor.copy(0.4f), RoundedCornerShape(12.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(tab.icon, null, tint = if (selected) accentColor else MutedText, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(tab.label, fontSize = 9.sp, color = if (selected) accentColor else MutedText, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // ── "More" Bottom Sheet Drawer ───────────────────────────
        AnimatedVisibility(
            visible = showMoreDrawer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(DarkCardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(20.dp)
            ) {
                // Handle bar
                Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MutedText).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Text("More Options", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
                Spacer(Modifier.height(12.dp))
                moreItems.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (id, label, icon) ->
                            val isActive = activeSection == id
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                    .background(if (isActive) IndigoPrimary.copy(0.15f) else SurfaceElevated)
                                    .border(1.dp, if (isActive) IndigoPrimary.copy(0.4f) else CardBorderColor, RoundedCornerShape(14.dp))
                                    .clickable { activeSection = id; showMoreDrawer = false }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, null, tint = if (isActive) IndigoPrimary else BodyText, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(label, fontSize = 10.sp, color = if (isActive) IndigoPrimary else BodyText, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                        // Fill empty slots
                        repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(4.dp))
                // Logout from drawer
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(RedDanger.copy(0.1f)).border(1.dp, RedDanger.copy(0.3f), RoundedCornerShape(14.dp))
                        .clickable { onLogout() }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Logout, null, tint = RedDanger, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = RedDanger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── SHARED COMPONENTS ────────────────────────────────────

@Composable
fun KPICard(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(16.dp)).background(DarkCardBg)
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(16.dp))
    ) {
        // Subtle top color accent
        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(color))
        Column(Modifier.padding(16.dp).padding(top = 3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(label, color = BodyText, fontSize = 11.sp)
                    Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String = "") {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
        if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = BodyText)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, bg) = when (status.lowercase()) {
        "published", "approved", "paid", "active", "upcoming" -> Pair(EmeraldSecondary, EmeraldSecondary.copy(0.15f))
        "pending", "processing" -> Pair(AmberWarning, AmberWarning.copy(0.15f))
        "rejected", "suspended", "banned", "cancelled" -> Pair(RedDanger, RedDanger.copy(0.15f))
        else -> Pair(MutedText, MutedText.copy(0.15f))
    }
    Box(
        Modifier.clip(RoundedCornerShape(50.dp)).background(bg).border(1.dp, color.copy(0.3f), RoundedCornerShape(50.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun ConfirmDialog(title: String, message: String, confirmLabel: String, confirmColor: Color = RedDanger, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBg,
        title = { Text(title, color = HeadingText, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = BodyText, fontSize = 13.sp) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = confirmColor)) { Text(confirmLabel, color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = BodyText) } }
    )
}

// ─── OVERVIEW ─────────────────────────────────────────────

@Composable
fun AdminOverviewScreen(viewModel: MainViewModel) {
    val users by viewModel.allUsersList.collectAsState()
    val courses by viewModel.allCoursesList.collectAsState()
    val payouts by viewModel.allPayoutsList.collectAsState()
    val logs by viewModel.allAdminLogsList.collectAsState()

    val pendingInstructors = users.count { it.role == "Instructor" && !it.isApproved }
    val pendingCourses = courses.count { it.status == "Pending" }
    val pendingPayouts = payouts.count { it.status == "Pending" }
    val totalRevenue = payouts.filter { it.status == "Paid" }.sumOf { it.amount }

    androidx.compose.foundation.lazy.LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            // Section heading
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Command Center", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = HeadingText)
                    Text("Platform-wide overview", fontSize = 12.sp, color = BodyText)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(10.dp)).background(EmeraldSecondary.copy(0.15f)).border(1.dp, EmeraldSecondary.copy(0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Live", fontSize = 11.sp, color = EmeraldSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPICard("Total Users", "${users.size}", IndigoPrimary, Icons.Default.Group, Modifier.weight(1f))
                    KPICard("Revenue", "₹${totalRevenue.formatNum()}", EmeraldSecondary, Icons.Default.CurrencyRupee, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPICard("Courses", "${courses.count { it.status == "Published" }}", AmberWarning, Icons.Default.Book, Modifier.weight(1f))
                    KPICard("Pro Users", "${users.count { it.subscription == "Pro" }}", Color(0xFF8B5CF6), Icons.Default.Star, Modifier.weight(1f))
                }
            }
        }

        item {
            Text("Action Required", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (pendingInstructors > 0) AlertRow("$pendingInstructors instructor(s) awaiting approval", AmberWarning)
                if (pendingCourses > 0) AlertRow("$pendingCourses course(s) pending moderation", AmberWarning)
                if (pendingPayouts > 0) AlertRow("$pendingPayouts payout request(s) pending", RedDanger)
                if (pendingInstructors == 0 && pendingCourses == 0 && pendingPayouts == 0)
                    AlertRow("All clear — no pending actions ✓", EmeraldSecondary)
            }
        }

        item {
            Text("Recent Admin Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(10.dp))
            if (logs.isEmpty()) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceElevated).padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No recent activity", color = MutedText, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    logs.take(6).forEach { log ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceElevated).border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(IndigoPrimary))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(log.action.replace("_", " "), fontSize = 12.sp, color = HeadingText)
                                Text(formatTimestamp(log.timestamp), fontSize = 10.sp, color = MutedText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertRow(message: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)).border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

fun Int.formatNum(): String {
    return if (this >= 100000) "${this / 100000}.${(this % 100000) / 10000}L"
    else if (this >= 1000) "${this / 1000},${String.format("%03d", this % 1000)}"
    else this.toString()
}

fun formatTimestamp(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
