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

@Composable
fun InstructorAnalyticsScreen(viewModel: MainViewModel, user: UserEntity) {
    var rangeSelected by remember { mutableStateOf("Last 30 Days") }
    val payouts by viewModel.allPayoutsList.collectAsState()
    val courses by viewModel.allCoursesList.collectAsState()
    val myCourses = remember(courses) { courses.filter { it.instructorId == user.email } }
    val myPayouts = remember(payouts) { payouts.filter { it.instructorId == user.email } }
    
    val totalRevenue = myPayouts.filter { it.status == "Paid" }.sumOf { it.amount }
    val totalEnrollments = myCourses.sumOf { it.enrolledCount }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            ScrollableTabRow(selectedTabIndex = listOf("Last 7 Days","Last 30 Days","3 Months","All Time").indexOf(rangeSelected).coerceAtLeast(0), containerColor = DarkBg, contentColor = IndigoPrimary, edgePadding = 0.dp) {
                listOf("Last 7 Days","Last 30 Days","3 Months","All Time").forEach { r -> Tab(selected = rangeSelected == r, onClick = { rangeSelected = r }, text = { Text(r, fontSize = 12.sp) }) }
            }
        }

        // Revenue Chart
        item {
            Text("Earnings Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth().height(180.dp).border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val pts = listOf(0.9f, 0.7f, 0.8f, 0.4f, 0.6f, 0.2f, 0.1f)
                        val path = Path()
                        pts.forEachIndexed { i, v ->
                            val x = size.width * (i / (pts.size - 1f))
                            val y = size.height * v
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, EmeraldSecondary, style = Stroke(width = 6f))
                        pts.forEachIndexed { i, v -> drawCircle(EmeraldSecondary, 8f, androidx.compose.ui.geometry.Offset(size.width * (i / (pts.size - 1f)), size.height * v)) }
                    }
                }
            }
        }

        // Top Metrics
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InstructorStatCard("Total Revenue", "₹$totalRevenue", "All time", EmeraldSecondary, Icons.Default.CurrencyRupee)
                InstructorStatCard("Enrollments", "$totalEnrollments", "+12% vs last month", IndigoPrimary, Icons.Default.Groups)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InstructorStatCard("Avg Completion", "42%", "Course completion rate", AmberWarning, Icons.Default.CheckCircle)
                InstructorStatCard("Avg Watch Time", "1h 15m", "Per student", HeadingText, Icons.Default.AccessTime)
            }
        }

        // Course Performance
        item {
            Text("Course Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            myCourses.sortedByDescending { it.enrolledCount }.forEach { course ->
                Row(Modifier.fillMaxWidth().padding(vertical=6.dp).clip(RoundedCornerShape(12.dp)).background(DarkCardBg).border(1.dp, CardBorderColor, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(IndigoPrimary), contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, null, tint = Color.White) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(course.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${course.enrolledCount} students • ⭐${course.rating}", fontSize = 11.sp, color = BodyText)
                    }
                    Text("₹${course.enrolledCount * course.price * 0.7}", fontWeight = FontWeight.Bold, color = EmeraldSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
