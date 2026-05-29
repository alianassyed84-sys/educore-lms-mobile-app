package com.example.ui.instructor

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

// ─── ROOT NAVIGATION ──────────────────────────────────────
@Composable
fun InstructorMainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val user = currentUser ?: return
    var activeTab by remember { mutableStateOf(0) }
    var showCourseBuilder by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<CourseEntity?>(null) }

    val notifications by viewModel.userNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }
    val payouts by viewModel.allPayoutsList.collectAsState()
    val myPayouts = remember(payouts) { payouts.filter { it.instructorId == user.email } }
    val totalEarnings = myPayouts.filter { it.status == "Paid" }.sumOf { it.amount }

    if (showCourseBuilder) {
        InstructorCourseBuilderScreen(viewModel = viewModel, editCourse = editingCourse, onDismiss = { showCourseBuilder = false; editingCourse = null })
        return
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = { InstructorTopBar(user = user, screenTitle = when(activeTab) { 0->"Dashboard"; 1->"My Courses"; 2->"Create Course"; 3->"Analytics"; else->"Profile" }, unreadCount = unreadCount, totalEarnings = totalEarnings, onBellClick = { activeTab = 4 }) },
        bottomBar = {
            NavigationBar(containerColor = DarkCardBg, tonalElevation = 0.dp) {
                val tabs = listOf(
                    Triple(Icons.Default.Home, "Home", 0),
                    Triple(Icons.Default.Book, "Courses", 1),
                    Triple(Icons.Default.AddCircle, "Create", 2),
                    Triple(Icons.Default.BarChart, "Analytics", 3),
                    Triple(Icons.Default.Person, "Profile", 4)
                )
                tabs.forEach { (icon, label, idx) ->
                    NavigationBarItem(
                        selected = activeTab == idx,
                        onClick = { if (idx == 2) { editingCourse = null; showCourseBuilder = true } else activeTab = idx },
                        icon = { Icon(icon, null, tint = if (activeTab == idx) IndigoPrimary else MutedText, modifier = Modifier.size(if (idx == 2) 28.dp else 22.dp)) },
                        label = { Text(label, fontSize = 10.sp, color = if (activeTab == idx) IndigoPrimary else MutedText) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = IndigoPrimary.copy(0.12f))
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (activeTab) {
                0 -> InstructorHomeScreen(viewModel, user, onCreateCourse = { showCourseBuilder = true }, onNavigateTab = { activeTab = it })
                1 -> InstructorCoursesScreen(viewModel, user, onEditCourse = { editingCourse = it; showCourseBuilder = true })
                3 -> InstructorAnalyticsScreen(viewModel, user)
                4 -> InstructorProfileScreen(viewModel, user, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun InstructorTopBar(user: UserEntity, screenTitle: String, unreadCount: Int, totalEarnings: Int, onBellClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().background(DarkCardBg).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(IndigoPrimary), contentAlignment = Alignment.Center) {
                Text("E", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(screenTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HeadingText)
        }
        // Right actions
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Earnings chip
            Row(
                Modifier.clip(RoundedCornerShape(50.dp)).background(EmeraldSecondary.copy(0.12f)).border(1.dp, EmeraldSecondary.copy(0.3f), RoundedCornerShape(50.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("₹${if (totalEarnings > 1000) "${totalEarnings/1000}K" else totalEarnings}", color = EmeraldSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            // Bell
            Box {
                IconButton(onClick = onBellClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = HeadingText, modifier = Modifier.size(22.dp))
                }
                if (unreadCount > 0) {
                    Box(
                        Modifier.size(16.dp).clip(CircleShape).background(RedDanger).align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) { Text(if (unreadCount > 9) "9+" else "$unreadCount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
