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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InstructorProfileScreen(viewModel: MainViewModel, user: UserEntity, onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf("Payouts") }
    
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = listOf("Payouts", "Settings").indexOf(activeTab), containerColor = DarkCardBg, contentColor = IndigoPrimary) {
            Tab(selected = activeTab == "Payouts", onClick = { activeTab = "Payouts" }, text = { Text("Earnings & Payouts", fontSize = 13.sp) })
            Tab(selected = activeTab == "Settings", onClick = { activeTab = "Settings" }, text = { Text("Profile & Settings", fontSize = 13.sp) })
        }
        
        when (activeTab) {
            "Payouts" -> InstructorPayoutsTab(viewModel, user)
            "Settings" -> InstructorSettingsTab(viewModel, user, onLogout)
        }
    }
}

@Composable
fun InstructorPayoutsTab(viewModel: MainViewModel, user: UserEntity) {
    val context = LocalContext.current
    val payouts by viewModel.allPayoutsList.collectAsState()
    val myPayouts = remember(payouts) { payouts.filter { it.instructorId == user.email } }
    
    val totalPaid = myPayouts.filter { it.status == "Paid" }.sumOf { it.amount }
    val pendingAmt = myPayouts.filter { it.status == "Pending" }.sumOf { it.amount }
    val available = 45000 - totalPaid - pendingAmt // Mock total earnings
    var requestAmt by remember { mutableStateOf("") }
    
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(Modifier.fillMaxWidth().border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = DarkCardBg)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Available for Withdrawal", fontSize = 12.sp, color = BodyText)
                    Text("₹${if (available > 0) available else 0}", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = EmeraldSecondary)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Pending", fontSize = 11.sp, color = BodyText); Text("₹$pendingAmt", fontWeight = FontWeight.Bold, color = AmberWarning) }
                        Column { Text("Total Paid Out", fontSize = 11.sp, color = BodyText); Text("₹$totalPaid", fontWeight = FontWeight.Bold, color = HeadingText) }
                    }
                }
            }
        }
        
        item {
            Text("Request Payout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            EduTextField("Amount to withdraw (Min ₹1000)", requestAmt, isNumber = true) { requestAmt = it }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { 
                val amt = requestAmt.toIntOrNull() ?: 0
                if (amt < 1000) { context.showToast("Minimum payout is ₹1000") }
                else if (amt > available) { context.showToast("Insufficient balance") }
                else { viewModel.payoutDao.insertPayout(PayoutEntity(instructorId = user.email, amount = amt, status = "Pending", requestedAt = System.currentTimeMillis())); requestAmt = ""; context.showToast("Payout requested!") }
            }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary)) { Text("Withdraw Funds") }
        }
        
        item {
            Text("Transaction History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            if (myPayouts.isEmpty()) {
                Text("No payout history yet.", color = MutedText, fontSize = 13.sp)
            }
        }
        
        items(myPayouts.sortedByDescending { it.requestedAt }) { p ->
            Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(12.dp)).background(DarkCardBg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if(p.status=="Paid") Icons.Default.CheckCircle else Icons.Default.Pending, null, tint = if(p.status=="Paid") EmeraldSecondary else AmberWarning, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Payout Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(p.requestedAt)), fontSize = 11.sp, color = BodyText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${p.amount}", fontWeight = FontWeight.Bold, color = HeadingText)
                    Text(p.status, fontSize = 10.sp, color = if(p.status=="Paid") EmeraldSecondary else AmberWarning)
                }
            }
        }
    }
}

@Composable
fun InstructorSettingsTab(viewModel: MainViewModel, user: UserEntity, onLogout: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(user.name) }
    var title by remember { mutableStateOf("Senior Developer") }
    var bio by remember { mutableStateOf("I teach people how to code.") }
    
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Public Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            EduTextField("Full Name", name) { name = it }
            Spacer(Modifier.height(8.dp))
            EduTextField("Professional Title", title) { title = it }
            Spacer(Modifier.height(8.dp))
            EduTextField("Bio", bio, maxLines = 4) { bio = it }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { context.showToast("Profile updated!") }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)) { Text("Save Profile") }
        }
        
        item {
            Text("Security", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { context.showToast("Password reset link sent") }, Modifier.fillMaxWidth(), border = BorderStroke(1.dp, CardBorderColor)) { Text("Change Password", color = HeadingText) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onLogout, Modifier.fillMaxWidth(), border = BorderStroke(1.dp, RedDanger)) { Text("Sign Out", color = RedDanger) }
        }
    }
}
