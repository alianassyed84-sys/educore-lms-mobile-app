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

@Composable
fun AdminMainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val currentUserState by viewModel.currentUser.collectAsState()
    currentUserState ?: return

    var activeSection by remember { mutableStateOf("overview") }
    var sidebarExpanded by remember { mutableStateOf(true) }
    val impersonated by viewModel.impersonatedUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Impersonation Banner
        if (impersonated != null) {
            Row(
                modifier = Modifier.fillMaxWidth().background(AmberWarning).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👁 Viewing as: ${impersonated!!.name}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextButton(onClick = { viewModel.adminStopImpersonation() }) {
                    Text("Exit View", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Sidebar ──────────────────────────────────────
            val sidebarWidth by animateDpAsState(if (sidebarExpanded) 200.dp else 64.dp, label = "sidebar")
            Column(
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight()
                    .background(DarkCardBg)
                    .border(BorderStroke(1.dp, CardBorderColor), RectangleShape)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (sidebarExpanded) Arrangement.SpaceBetween else Arrangement.Center
                ) {
                    if (sidebarExpanded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            Spacer(Modifier.width(8.dp))
                            Text("EduCore", fontWeight = FontWeight.Bold, color = HeadingText, fontSize = 14.sp)
                        }
                    }
                    IconButton(onClick = { sidebarExpanded = !sidebarExpanded }, modifier = Modifier.size(32.dp)) {
                        Icon(if (sidebarExpanded) Icons.Default.ChevronLeft else Icons.Default.ChevronRight, null, tint = BodyText)
                    }
                }

                Divider(color = CardBorderColor)
                Spacer(Modifier.height(8.dp))

                // Nav Items
                adminSidebarItems.forEach { item ->
                    val isActive = activeSection == item.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) IndigoPrimary else Color.Transparent)
                            .clickable { activeSection = item.id }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(item.icon, null, tint = if (isActive) Color.White else BodyText, modifier = Modifier.size(20.dp))
                        if (sidebarExpanded) {
                            Spacer(Modifier.width(12.dp))
                            Text(item.label, color = if (isActive) Color.White else BodyText, fontSize = 13.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Divider(color = CardBorderColor)

                // Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLogout() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = RedDanger, modifier = Modifier.size(20.dp))
                    if (sidebarExpanded) { Spacer(Modifier.width(12.dp)); Text("Logout", color = RedDanger, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Main Content ─────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
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
        }
    }
}

// ─── SHARED COMPONENTS ────────────────────────────────────

@Composable
fun KPICard(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, color = BodyText, fontSize = 11.sp)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String = "") {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = HeadingText)
        if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = BodyText)
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "published", "approved", "paid", "active", "upcoming" -> EmeraldSecondary
        "pending", "processing" -> AmberWarning
        "rejected", "suspended", "banned", "cancelled" -> RedDanger
        else -> MutedText
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
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
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)); SectionHeader("Command Center", "Platform-wide overview") }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPICard("Total Users", "${users.size}", IndigoPrimary, Icons.Default.Group, Modifier.weight(1f))
                    KPICard("Total Revenue", "₹${totalRevenue.formatNum()}", EmeraldSecondary, Icons.Default.CurrencyRupee, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KPICard("Active Courses", "${courses.count { it.status == "Published" }}", AmberWarning, Icons.Default.Book, Modifier.weight(1f))
                    KPICard("Pro Users", "${users.count { it.subscription == "Pro" }}", IndigoPrimary, Icons.Default.Star, Modifier.weight(1f))
                }
            }
        }

        item {
            Text("Action Required", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            if (pendingInstructors > 0) AlertRow("$pendingInstructors instructor(s) awaiting approval", AmberWarning)
            if (pendingCourses > 0) AlertRow("$pendingCourses course(s) pending moderation", AmberWarning)
            if (pendingPayouts > 0) AlertRow("$pendingPayouts payout request(s) pending", RedDanger)
            if (pendingInstructors == 0 && pendingCourses == 0 && pendingPayouts == 0)
                AlertRow("All clear — no pending actions", EmeraldSecondary)
        }

        item {
            Text("Recent Admin Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
            Spacer(Modifier.height(8.dp))
            logs.take(5).forEach { log ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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

@Composable
fun AlertRow(message: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)).padding(12.dp),
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
