package com.example.ui.instructor

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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InstructorHomeScreen(viewModel: MainViewModel, user: UserEntity, onCreateCourse: () -> Unit, onNavigateTab: (Int) -> Unit) {
    val context = LocalContext.current
    val courses by viewModel.allCoursesList.collectAsState()
    val sessions by viewModel.allSessionsList.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val myCourses = remember(courses) { courses.filter { it.instructorId == user.email } }
    val myUpcoming = remember(sessions) { sessions.filter { it.instructorId == user.email && it.status == "Upcoming" } }
    val timeGreet = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) { in 5..11 -> "Good Morning"; in 12..16 -> "Good Afternoon"; else -> "Good Evening" }
    val dateStr = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()).format(Date())

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
        // Greeting
        item {
            Column {
                Text("$timeGreet, ${user.name.split(" ").first()} 👋", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = HeadingText)
                Text(dateStr, fontSize = 12.sp, color = MutedText)
            }
        }

        // Status banner
        if (!user.isApproved && user.isActive) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AmberWarning.copy(0.12f)).border(1.dp, AmberWarning.copy(0.4f), RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column { Text("Account Under Review", fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 13.sp); Text("Usually takes 24 hours. You'll be notified on approval.", fontSize = 11.sp, color = AmberWarning.copy(0.8f)) }
                }
            }
        }
        if (!user.isActive) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(RedDanger.copy(0.12f)).border(1.dp, RedDanger.copy(0.4f), RoundedCornerShape(12.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, null, tint = RedDanger, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column { Text("Account Suspended", fontWeight = FontWeight.Bold, color = RedDanger, fontSize = 13.sp); if (user.suspensionReason.isNotEmpty()) Text("Reason: ${user.suspensionReason}", fontSize = 11.sp, color = RedDanger.copy(0.8f)) }
                }
            }
        }

        // KPI Stats
        item {
            val published = myCourses.count { it.status == "Published" }
            val drafts = myCourses.count { it.status == "Draft" }
            val totalStudents = myCourses.sumOf { it.enrolledCount }
            val avgRating = if (myCourses.isNotEmpty()) myCourses.map { it.rating }.average().toFloat() else 0f

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { InstructorStatCard("This Month", "₹24,500", "+12%", EmeraldSecondary, Icons.Default.CurrencyRupee) }
                item { InstructorStatCard("Total Students", "$totalStudents", "+34 this week", IndigoPrimary, Icons.Default.Group) }
                item { InstructorStatCard("Avg Rating", "⭐ ${if(avgRating>0f) String.format("%.1f",avgRating) else "N/A"}", "from ${myCourses.sumOf{it.reviewCount}} reviews", AmberWarning, Icons.Default.Star) }
                item { InstructorStatCard("Published", "$published", "$drafts draft, 1 in review", HeadingText, Icons.Default.Book) }
            }
        }

        // Activity Feed
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("What's Happening", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            val activities = buildList {
                notifications.take(5).forEach { n -> add(Triple(n.message, formatRelTime(n.createdAt), when(n.type){"Course"->Icons.Default.School;"Payout"->Icons.Default.AccountBalanceWallet;"Live"->Icons.Default.VideoCall;else->Icons.Default.Notifications})) }
                if (myUpcoming.isNotEmpty()) add(Triple("Upcoming: ${myUpcoming.first().topic} on ${myUpcoming.first().scheduledAt}", "Scheduled", Icons.Default.VideoCall))
            }
            if (activities.isEmpty()) {
                Text("No recent activity yet. Create your first course!", fontSize = 13.sp, color = MutedText)
            } else {
                Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activities.forEach { (msg, time, icon) ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(32.dp).clip(CircleShape).background(IndigoGlow), contentAlignment = Alignment.Center) { Icon(icon, null, tint = IndigoPrimary, modifier = Modifier.size(16.dp)) }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) { Text(msg, fontSize = 12.sp, color = HeadingText, maxLines = 2); Text(time, fontSize = 10.sp, color = MutedText) }
                            }
                        }
                    }
                }
            }
        }

        // Performance Snapshot
        item {
            Text("Revenue Trend (7 days)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth().height(140.dp).border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val pts = listOf(0.8f, 0.5f, 0.65f, 0.4f, 0.7f, 0.55f, 0.3f)
                        val path = Path()
                        pts.forEachIndexed { i, v ->
                            val x = size.width * (i / (pts.size - 1f))
                            val y = size.height * v
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, IndigoPrimary, style = Stroke(width = 5f))
                        pts.forEachIndexed { i, v ->
                            drawCircle(IndigoPrimary, 6f, androidx.compose.ui.geometry.Offset(size.width * (i / (pts.size-1f)), size.height * v))
                        }
                    }
                }
            }
        }

        // Upcoming sessions
        if (myUpcoming.isNotEmpty()) {
            item {
                Text("Upcoming Live Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Card(Modifier.fillMaxWidth().border(1.dp, IndigoPrimary.copy(0.4f), RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(myUpcoming.first().topic, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${myUpcoming.first().scheduledAt} • ${myUpcoming.first().duration}", fontSize = 12.sp, color = BodyText)
                        Text("${myUpcoming.first().enrolledCount} enrolled", fontSize = 11.sp, color = MutedText)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { onNavigateTab(4) }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary), modifier = Modifier.fillMaxWidth().height(36.dp)) { Text("Prepare Session", fontSize = 13.sp) }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionBtn("New Course", Icons.Default.AddCircle, IndigoPrimary, Modifier.weight(1f)) { onCreateCourse() }
                QuickActionBtn("Schedule Live", Icons.Default.VideoCall, DarkCardBg, Modifier.weight(1f)) { onNavigateTab(4) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionBtn("View Reviews", Icons.Default.Star, DarkCardBg, Modifier.weight(1f)) { onNavigateTab(1) }
                QuickActionBtn("Request Payout", Icons.Default.AccountBalanceWallet, EmeraldSecondary, Modifier.weight(1f)) { onNavigateTab(4) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun InstructorStatCard(label: String, value: String, sub: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.width(160.dp).border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 10.sp, color = BodyText)
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(14.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(sub, fontSize = 10.sp, color = MutedText)
        }
    }
}

@Composable
fun QuickActionBtn(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        border = if (bgColor == DarkCardBg) BorderStroke(1.dp, CardBorderColor) else null
    ) {
        Icon(icon, null, tint = if (bgColor == DarkCardBg) HeadingText else Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = if (bgColor == DarkCardBg) HeadingText else Color.White)
    }
}

fun formatRelTime(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when { diff < 60000L -> "Just now"; diff < 3600000L -> "${diff/60000}m ago"; diff < 86400000L -> "${diff/3600000}h ago"; else -> "${diff/86400000}d ago" }
}
