package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase = AppDatabase.getDatabase(application)
    
    val userDao = database.userDao()
    val courseDao = database.courseDao()
    val lessonDao = database.lessonDao()
    val enrollmentDao = database.enrollmentDao()
    val reviewDao = database.reviewDao()
    val liveSessionDao = database.liveSessionDao()
    val payoutDao = database.payoutDao()
    val notificationDao = database.notificationDao()
    val adminLogDao = database.adminLogDao()

    // --- Authentication & Session State ---
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val selectedRole = mutableStateOf("Learner") // "Learner", "Instructor", "Admin"
    
    // Safety & Locking variables
    private var failedAttempts = mutableMapOf<String, Int>()
    private var lockedAccounts = mutableMapOf<String, Long>() // Email -> lock expire time
    private var requestTimestamps = mutableListOf<Long>() // For rate-limiting per minute

    // OTP states
    var verificationEmail = mutableStateOf("")
    var signupPrefilledName = mutableStateOf("")
    var signupPrefilledPassword = mutableStateOf("")
    var generatedOtp = mutableStateOf("")
    var otpPurpose = mutableStateOf("signup") // "signup" or "forgot_password"

    // --- General Streams ---
    val allCoursesList: StateFlow<List<CourseEntity>> = courseDao.getAllCoursesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessionsList: StateFlow<List<LiveSessionEntity>> = liveSessionDao.getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayoutsList: StateFlow<List<PayoutEntity>> = payoutDao.getAllPayoutsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsersList: StateFlow<List<UserEntity>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminLogsList: StateFlow<List<AdminLogEntity>> = adminLogDao.getAllAdminLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state flows filtered for active students/instructors
    val userEnrollments = currentUser.flatMapLatest { user ->
        if (user != null) {
            enrollmentDao.getEnrollmentsForUserFlow(user.email)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications = currentUser.flatMapLatest { user ->
        if (user != null) {
            notificationDao.getNotificationsForUserFlow(user.email)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                // Seed database with amazing mock content
                DatabaseSeeder.seedIfNeeded(application, database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Rate Limiter ---
    private fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        requestTimestamps.removeIf { now - it > 60000 } // keep last 1 minute
        if (requestTimestamps.size >= 10) {
            return false // Limit exceeded!
        }
        requestTimestamps.add(now)
        return true
    }

    // --- Helper Sanitizer ---
    private fun sanitizeInput(input: String): String {
        return input.replace(Regex("<[^>]*>"), "") // Simple XSS sanitization
    }

    // --- Password Strength ---
    fun evaluatePasswordStrength(password: String): String {
        if (password.length < 4) return "Weak"
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { "!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) score++
        
        return when (score) {
            0, 1 -> "Weak"
            2 -> "Fair"
            3 -> "Strong"
            else -> "Very Strong"
        }
    }

    // --- Core Auth Logic ---
    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun login(emailInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        if (!checkRateLimit()) {
            onResult(false, "Rate limit exceeded. Max 10 requests per minute.")
            return
        }

        val email = sanitizeInput(emailInput).trim().lowercase()
        val password = sanitizeInput(passwordInput)

        // Check if locked
        val lockExpireTime = lockedAccounts[email]
        if (lockExpireTime != null) {
            val remaining = lockExpireTime - System.currentTimeMillis()
            if (remaining > 0) {
                val remMin = (remaining / 60000) + 1
                onResult(false, "Account locked. Try again in $remMin minute(s).")
                return
            } else {
                lockedAccounts.remove(email)
                failedAttempts.remove(email)
            }
        }

        val user = userDao.getUserByEmail(email)
        if (user == null) {
            onResult(false, "Invalid credentials.")
            return
        }

        if (!user.isActive) {
            onResult(false, "Your account has been suspended by an Admin.")
            return
        }

        if (user.passwordHash == hashPassword(password)) {
            // Success! Reset failed attempts
            failedAttempts.remove(email)
            
            if (user.role.lowercase() != selectedRole.value.lowercase()) {
                onResult(false, "Selected role does not match account role.")
                return
            }

            if (!user.isVerified) {
                // Must verify email first
                verificationEmail.value = user.email
                otpPurpose.value = "signup"
                generatedOtp.value = generate6DigitOtp()
                onResult(true, "OTP_VERIFICATION_REQUIRED")
                return
            }

            if (user.role == "Instructor" && !user.isApproved) {
                onResult(false, "Your instructor account is under review. Admin approval is required.")
                return
            }

            currentUser.value = user
            onResult(true, "SUCCESS")
        } else {
            // Failed attempt
            val attempts = failedAttempts.getOrDefault(email, 0) + 1
            failedAttempts[email] = attempts
            if (attempts >= 5) {
                lockedAccounts[email] = System.currentTimeMillis() + 15 * 60 * 1000 // 15 mins lock
                onResult(false, "Too many failed attempts. Account locked for 15 minutes.")
            } else {
                onResult(false, "Invalid credentials. Attempt $attempts of 5.")
            }
        }
    }

    suspend fun register(nameInput: String, emailInput: String, passwordInput: String, onResult: (Boolean, String) -> Unit) {
        if (!checkRateLimit()) {
            onResult(false, "Rate limit exceeded. Max 10 requests per minute.")
            return
        }

        val name = sanitizeInput(nameInput).trim()
        val email = sanitizeInput(emailInput).trim().lowercase()
        val password = sanitizeInput(passwordInput)

        if (password.length < 8) {
            onResult(false, "Password must be at least 8 characters.")
            return
        }

        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            onResult(false, "An account with this email already exists.")
            return
        }

        // Only existing admins can create admins
        if (selectedRole.value == "Admin") {
            onResult(false, "Admin accounts cannot be created publicly.")
            return
        }

        val isApprovedByDefault = selectedRole.value == "Learner" // Instructors need admin approval

        val newUser = UserEntity(
            email = email,
            name = name,
            passwordHash = hashPassword(password),
            role = selectedRole.value,
            isVerified = false,
            isApproved = isApprovedByDefault,
            subscription = "Free"
        )
        userDao.insertUser(newUser)

        // Generate Verification OTP and navigate
        verificationEmail.value = email
        generatedOtp.value = generate6DigitOtp()
        otpPurpose.value = "signup"

        onResult(true, "SUCCESS")
    }

    suspend fun verifyOtp(enteredCode: String, onResult: (Boolean, String) -> Unit) {
        if (enteredCode == generatedOtp.value) {
            val email = verificationEmail.value
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                if (otpPurpose.value == "signup") {
                    val updatedUser = user.copy(isVerified = true)
                    userDao.insertUser(updatedUser)
                    
                    if (updatedUser.role == "Instructor" && !updatedUser.isApproved) {
                        onResult(true, "INSTRUCTOR_PENDING")
                    } else {
                        currentUser.value = updatedUser
                        onResult(true, "SUCCESS")
                    }
                } else if (otpPurpose.value == "forgot_password") {
                    onResult(true, "RESET_PASSWORD_APPROVED")
                }
            } else {
                onResult(false, "User not found.")
            }
        } else {
            onResult(false, "Invalid OTP. Please check and try again.")
        }
    }

    suspend fun forgotPasswordRequest(emailInput: String, onResult: (Boolean, String) -> Unit) {
        val email = sanitizeInput(emailInput).trim().lowercase()
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            onResult(false, "Email address not found.")
            return
        }
        verificationEmail.value = email
        generatedOtp.value = generate6DigitOtp()
        otpPurpose.value = "forgot_password"
        onResult(true, "SUCCESS")
    }

    suspend fun resetPassword(newPasswordInput: String, onResult: (Boolean, String) -> Unit) {
        val email = verificationEmail.value
        val password = sanitizeInput(newPasswordInput)
        if (password.length < 8) {
            onResult(false, "Password must be at least 8 characters.")
            return
        }
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            val updatedUser = user.copy(passwordHash = hashPassword(password), isVerified = true)
            userDao.insertUser(updatedUser)
            currentUser.value = updatedUser
            onResult(true, "SUCCESS")
        } else {
            onResult(false, "Error identifying user profile.")
        }
    }

    fun logout() {
        currentUser.value = null
    }

    private fun generate6DigitOtp(): String {
        return (100000..999999).random().toString()
    }

    // --- Learner Nexus Actions ---
    fun enrollInCourse(courseId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            // Free users limit: Max 5 courses enrolled per month
            if (user.subscription == "Free") {
                val enrollmentsCount = userEnrollments.value.size
                if (enrollmentsCount >= 5) {
                    // Triggers subscription promo
                    return@launch
                }
            }

            val existing = enrollmentDao.getEnrollment(user.email, courseId)
            if (existing == null) {
                val newEnrollment = EnrollmentEntity(
                    userEmail = user.email,
                    courseId = courseId,
                    progress = 0,
                    completedLessonIds = "",
                    enrolledAt = System.currentTimeMillis()
                )
                enrollmentDao.insertEnrollment(newEnrollment)
                courseDao.incrementEnrollmentCount(courseId)
            }
        }
    }

    fun toggleWishlist(courseId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val existing = enrollmentDao.getEnrollment(user.email, courseId)
            if (existing != null) {
                enrollmentDao.insertEnrollment(existing.copy(wishlist = !existing.wishlist))
            } else {
                enrollmentDao.insertEnrollment(
                    EnrollmentEntity(
                        userEmail = user.email,
                        courseId = courseId,
                        wishlist = true
                    )
                )
            }
        }
    }

    fun saveLessonNotes(courseId: Int, notesText: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val enrollment = enrollmentDao.getEnrollment(user.email, courseId) ?: return@launch
            enrollmentDao.updateNotes(user.email, courseId, notesText)
        }
    }

    fun markLessonComplete(courseId: Int, lessonId: Int, onComplete: (xpEarned: Int) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val enrollment = enrollmentDao.getEnrollment(user.email, courseId) ?: return@launch
            val completedIds = enrollment.completedLessonIds.split(",").filter { it.isNotEmpty() }.toMutableList()
            val lessonIdStr = lessonId.toString()
            
            if (!completedIds.contains(lessonIdStr)) {
                completedIds.add(lessonIdStr)
                val newCompletedIdsStr = completedIds.joinToString(",")
                
                // Calculate progress
                val lessonsList = lessonDao.getLessonsForCourse(courseId)
                val progress = if (lessonsList.isEmpty()) 100 else ((completedIds.size.toFloat() / lessonsList.size) * 100).toInt()
                
                val isNowFinished = progress >= 100
                enrollmentDao.updateEnrollment(
                    enrollment.copy(
                        completedLessonIds = newCompletedIdsStr,
                        progress = progress,
                        isCompleted = isNowFinished
                    )
                )

                // Add XP (+50 XP) & streak bonuses
                val updatedXP = user.xp + 50
                val milestoneBadges = user.badges.split(",").toMutableList()
                if (isNowFinished && !milestoneBadges.contains("Course Finished")) {
                    milestoneBadges.add("Course Finished")
                }
                
                val updatedUser = user.copy(
                    xp = updatedXP,
                    badges = milestoneBadges.joinToString(",")
                )
                userDao.insertUser(updatedUser)
                currentUser.value = updatedUser

                onComplete(50)
            } else {
                onComplete(0)
            }
        }
    }

    fun addNewCourseReview(courseId: Int, rating: Int, comment: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            reviewDao.insertReview(
                ReviewEntity(
                    courseId = courseId,
                    userEmail = user.email,
                    userName = user.name,
                    rating = rating,
                    comment = comment
                )
            )
        }
    }

    fun upgradeToProInstant(pricingPlanText: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(subscription = "Pro")
            userDao.insertUser(updatedUser)
            currentUser.value = updatedUser
            
            // Send In App notification
            notificationDao.insertNotification(
                NotificationEntity(
                    userEmail = user.email,
                    message = "Congratulations! You have successfully upgraded to EduCore Pro Pro plan. Enjoy unlimited courses and certificates!",
                    type = "Alert"
                )
            )
        }
    }

    // --- Instructor Actions ---
    fun publishCourseByInstructor(
        title: String,
        details: String,
        category: String,
        difficulty: String,
        priceVal: Int,
        lessonsList: List<LessonEntity>
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newCourse = CourseEntity(
                title = title,
                description = details,
                instructorId = user.email,
                instructorName = user.name,
                category = category,
                price = priceVal,
                status = "Pending", // Needs Admin review!
                thumbnail = "course_custom"
            )
            val cId = courseDao.insertCourse(newCourse).toInt()
            
            // Map lessons and insert
            val lessonsWithCourseId = lessonsList.map { it.copy(courseId = cId) }
            lessonDao.insertLessons(lessonsWithCourseId)

            // Notify instructor
            notificationDao.insertNotification(
                NotificationEntity(
                    userEmail = user.email,
                    message = "Your course '$title' has been successfully submitted for Admin Review. It will be online within 48 hours.",
                    type = "Course"
                )
            )
        }
    }

    fun scheduleLiveStream(topic: String, description: String, dateStr: String, durationVal: String, capacity: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            liveSessionDao.insertSession(
                LiveSessionEntity(
                    instructorId = user.email,
                    instructorName = user.name,
                    topic = topic,
                    description = description,
                    scheduledAt = dateStr,
                    duration = durationVal,
                    maxParticipants = capacity,
                    status = "Upcoming"
                )
            )
        }
    }

    fun requestInstructorWithdrawal(amountStr: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        val amt = amountStr.toIntOrNull() ?: 0
        if (amt < 1000) {
            onResult(false, "Minimum withdrawal requested must be ₹1,000.")
            return
        }

        viewModelScope.launch {
            payoutDao.insertPayout(
                PayoutEntity(
                    instructorId = user.email,
                    amount = amt,
                    status = "Pending",
                    requestedAt = System.currentTimeMillis()
                )
            )
            onResult(true, "Payout of ₹$amt requested successfully and is pending admin approval.")
        }
    }

    // --- Admin Control Actions ---
    fun adminApproveInstructor(instructorEmail: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            userDao.approveInstructor(instructorEmail, true)
            adminLogDao.insertAdminLog(
                AdminLogEntity(
                    adminEmail = admin.email,
                    action = "APPROVED_INSTRUCTOR",
                    targetId = instructorEmail,
                    targetType = "USER"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    userEmail = instructorEmail,
                    message = "Awesome! Your EduCore Instructor profile has been approved. You can now build courses & schedule live streams.",
                    type = "Alert"
                )
            )
        }
    }

    fun adminRejectInstructor(instructorEmail: String, reason: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            // Delete user or keep as unapproved
            userDao.setUserActiveState(instructorEmail, false)
            adminLogDao.insertAdminLog(
                AdminLogEntity(
                    adminEmail = admin.email,
                    action = "REJECTED_INSTRUCTOR_REASON: $reason",
                    targetId = instructorEmail,
                    targetType = "USER"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    userEmail = instructorEmail,
                    message = "Your Instructor profile request was declined. Reason: $reason",
                    type = "Alert"
                )
            )
        }
    }

    fun adminModerateCourse(courseId: Int, isApproved: Boolean, rejectReason: String = "") {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            val course = courseDao.getCourseById(courseId) ?: return@launch
            val status = if (isApproved) "Approved" else "Rejected"
            courseDao.updateCourseStatus(courseId, status)
            
            adminLogDao.insertAdminLog(
                AdminLogEntity(
                    adminEmail = admin.email,
                    action = if (isApproved) "APPROVED_COURSE" else "REJECTED_COURSE: $rejectReason",
                    targetId = courseId.toString(),
                    targetType = "COURSE"
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    userEmail = course.instructorId,
                    message = "Your course '${course.title}' has been $status by the administrator. ${if (!isApproved) "Reason: $rejectReason" else ""}",
                    type = "Alert"
                )
            )
        }
    }

    fun adminSuspendUser(userEmail: String, isSuspended: Boolean) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            userDao.setUserActiveState(userEmail, !isSuspended)
            adminLogDao.insertAdminLog(
                AdminLogEntity(
                    adminEmail = admin.email,
                    action = if (isSuspended) "SUSPENDED_USER" else "UNSUSPENDED_USER",
                    targetId = userEmail,
                    targetType = "USER"
                )
            )
        }
    }

    fun adminProcessPayout(payoutId: Int) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            val txnId = "TXN_" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase()
            payoutDao.updatePayoutStatus(
                id = payoutId,
                status = "Paid",
                processedAt = System.currentTimeMillis(),
                transactionId = txnId
            )
            adminLogDao.insertAdminLog(
                AdminLogEntity(
                    adminEmail = admin.email,
                    action = "PAID_INSTRUCTOR_WITHDRAWAL",
                    targetId = payoutId.toString(),
                    targetType = "PAYOUT"
                )
            )
        }
    }
}
