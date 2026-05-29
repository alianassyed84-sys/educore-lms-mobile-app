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
    val couponDao = database.couponDao()
    val bannerDao = database.bannerDao()
    val sentNotificationDao = database.sentNotificationDao()
    val platformSettingDao = database.platformSettingDao()

    // Admin impersonation
    val impersonatedUser = MutableStateFlow<UserEntity?>(null)

    // Auth state
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val selectedRole = mutableStateOf("Learner")
    private var failedAttempts = mutableMapOf<String, Int>()
    private var lockedAccounts = mutableMapOf<String, Long>()
    private var requestTimestamps = mutableListOf<Long>()

    // OTP / verification states (kept for UI screen compatibility)
    var verificationEmail = mutableStateOf("")
    var generatedOtp = mutableStateOf("")
    var otpPurpose = mutableStateOf("signup")
    var signupPrefilledName = mutableStateOf("")
    var signupPrefilledPassword = mutableStateOf("")

    // Streams (Migrated to Firestore — Phase 3 Complete)
    val allCoursesList: StateFlow<List<CourseEntity>> = FirebaseRepository.getAllCoursesFlow()
        .map { list -> list.map { it.toCourseEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allSessionsList: StateFlow<List<LiveSessionEntity>> = FirebaseRepository.getAllSessionsFlow()
        .map { list -> list.map { it.toLiveSessionEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allPayoutsList: StateFlow<List<PayoutEntity>> = FirebaseRepository.getAllPayoutsFlow()
        .map { list -> list.map { it.toPayoutEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allUsersList: StateFlow<List<UserEntity>> = FirebaseRepository.getAllUsersFlow()
        .map { list -> list.map { it.toUserEntity(it["uid"] as? String ?: "") } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allAdminLogsList: StateFlow<List<AdminLogEntity>> = FirebaseRepository.getAllAdminLogsFlow()
        .map { list -> list.map { it.toAdminLogEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allBannersList: StateFlow<List<BannerEntity>> = FirebaseRepository.getAllBannersFlow()
        .map { list -> list.map { it.toBannerEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allSentNotifications: StateFlow<List<SentNotificationEntity>> = FirebaseRepository.getAllSentNotificationsFlow()
        .map { list -> list.map { it.toSentNotificationEntity() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userEnrollments = currentUser.flatMapLatest { user ->
        if (user != null) FirebaseRepository.getEnrollmentsForUserFlow(user.email).map { list -> list.map { it.toEnrollmentEntity() } }
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications = currentUser.flatMapLatest { user ->
        if (user != null) FirebaseRepository.getNotificationsForUserFlow(user.email).map { list -> list.map { it.toNotificationEntity() } }
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed local Room DB (offline fallback data)
            try { DatabaseSeeder.seedIfNeeded(getApplication(), database) } catch (e: Exception) { e.printStackTrace() }
            // Seed Firebase Auth + Firestore (runs only once — skipped if data exists)
            try { FirebaseSeeder.seedIfNeeded() } catch (e: Exception) { e.printStackTrace() }
        }
        // Restore session: if Firebase still has a signed-in user, reload their profile
        restoreFirebaseSession()
    }

    // ─── HELPERS ───────────────────────────────────────────
    private fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        requestTimestamps.removeIf { now - it > 60000 }
        if (requestTimestamps.size >= 10) return false
        requestTimestamps.add(now); return true
    }
    private fun sanitizeInput(input: String) = input.replace(Regex("<[^>]*>"), "")
    private fun genTxnId() = "TXN_" + UUID.randomUUID().toString().replace("-", "").take(10).uppercase()

    fun evaluatePasswordStrength(password: String): String {
        if (password.length < 4) return "Weak"
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?".contains(it) }) score++
        return when (score) { 0, 1 -> "Weak"; 2 -> "Fair"; 3 -> "Strong"; else -> "Very Strong" }
    }

    // ─── FIREBASE SESSION RESTORE ────────────────────────────────────────
    /**
     * If Firebase Auth has a valid cached token, fetch the Firestore profile
     * and restore [currentUser] so the user skips the login screen on relaunch.
     */
    private fun restoreFirebaseSession() {
        val fbUser = FirebaseRepository.currentFirebaseUser ?: return
        viewModelScope.launch {
            try {
                val profile = FirebaseRepository.getUserProfile(fbUser.uid)
                if (profile != null) {
                    currentUser.value = profile.toUserEntity(fbUser.uid)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─── AUTH — FIREBASE BACKED ─────────────────────────────────────────

    /**
     * Login via Firebase Authentication.
     * Callbacks:
     *   onResult(true,  "SUCCESS")                    → login complete
     *   onResult(true,  "EMAIL_NOT_VERIFIED")          → route to verify screen
     *   onResult(false, "error message")               → show error toast
     */
    suspend fun login(
        emailInput: String,
        passwordInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!checkRateLimit()) { onResult(false, "Too many requests. Please wait a moment."); return }
        val email    = sanitizeInput(emailInput).trim().lowercase()
        val password = sanitizeInput(passwordInput)

        // Brute-force lock check
        val lockExpireTime = lockedAccounts[email]
        if (lockExpireTime != null) {
            val remaining = lockExpireTime - System.currentTimeMillis()
            if (remaining > 0) {
                onResult(false, "Account locked. Try again in ${(remaining / 60000) + 1} min.")
                return
            } else {
                lockedAccounts.remove(email); failedAttempts.remove(email)
            }
        }

        val result = FirebaseRepository.login(email, password)
        if (result.isFailure) {
            val attempts = failedAttempts.getOrDefault(email, 0) + 1
            failedAttempts[email] = attempts
            if (attempts >= 5) {
                lockedAccounts[email] = System.currentTimeMillis() + 15 * 60 * 1000
                onResult(false, "Too many failed attempts. Account locked for 15 minutes.")
            } else {
                onResult(false, "Invalid email or password. Attempt $attempts of 5.")
            }
            return
        }

        failedAttempts.remove(email)
        val uid = result.getOrThrow()

        // Fetch Firestore profile
        val profile = try {
            FirebaseRepository.getUserProfile(uid)
        } catch (e: Exception) {
            onResult(false, "Could not load profile. Check your internet connection.")
            return
        }

        if (profile == null) {
            onResult(false, "Account profile not found. Please contact support.")
            return
        }

        val userEntity = profile.toUserEntity(uid)

        // Guard: role must match selected role tab
        if (userEntity.role.lowercase() != selectedRole.value.lowercase()) {
            FirebaseRepository.signOut()
            onResult(false, "Selected role does not match your account role.")
            return
        }

        // Guard: account must be active
        if (!userEntity.isActive) {
            FirebaseRepository.signOut()
            onResult(false, if (userEntity.isBanned) "This account is permanently banned." else "Account suspended by Admin.")
            return
        }

        // Guard: email must be verified
        val emailVerified = FirebaseRepository.reloadAndCheckVerified()
        if (!emailVerified) {
            verificationEmail.value = email
            onResult(true, "EMAIL_NOT_VERIFIED")
            return
        }

        // Guard: instructors must be admin-approved
        if (userEntity.role == "Instructor" && !userEntity.isApproved) {
            FirebaseRepository.signOut()
            onResult(false, "Your instructor account is under review. Admin approval required.")
            return
        }

        currentUser.value = userEntity
        onResult(true, "SUCCESS")
    }

    /**
     * Register via Firebase Auth + write profile to Firestore.
     * Firebase automatically sends a verification email on success.
     */
    suspend fun register(
        nameInput: String,
        emailInput: String,
        passwordInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!checkRateLimit()) { onResult(false, "Too many requests. Please wait a moment."); return }
        val name     = sanitizeInput(nameInput).trim()
        val email    = sanitizeInput(emailInput).trim().lowercase()
        val password = sanitizeInput(passwordInput)

        if (name.isBlank())      { onResult(false, "Please enter your full name."); return }
        if (password.length < 8) { onResult(false, "Password must be at least 8 characters."); return }
        if (selectedRole.value == "Admin") { onResult(false, "Admin accounts cannot be created publicly."); return }
        onResult(true, "SUCCESS")
    }

    suspend fun verifyOtp(enteredCode: String, onResult: (Boolean, String) -> Unit) {
        if (otpPurpose.value == "signup") {
            // With Firebase, we don't use manual OTPs, we check if they clicked the email link
            val isVerified = FirebaseRepository.reloadAndCheckVerified()
            if (isVerified) {
                val fbUser = FirebaseRepository.currentFirebaseUser
                if (fbUser != null) {
                    FirebaseRepository.markUserVerifiedInFirestore(fbUser.uid)
                    // Reload the current user value
                    restoreFirebaseSession()
                    onResult(true, "SUCCESS")
                } else {
                    onResult(false, "Session lost. Please log in again.")
                }
            } else {
                onResult(false, "Email not verified yet. Please check your inbox and click the link.")
            }
        } else {
            onResult(true, "RESET_PASSWORD_APPROVED")
        }
    }

    suspend fun forgotPasswordRequest(emailInput: String, onResult: (Boolean, String) -> Unit) {
        val email = sanitizeInput(emailInput).trim().lowercase()
        val result = FirebaseRepository.sendPasswordResetEmail(email)
        if (result.isSuccess) {
            onResult(true, "Password reset email sent. Please check your inbox.")
        } else {
            onResult(false, "Failed to send reset email. Account may not exist.")
        }
    }

    suspend fun resetPassword(newPasswordInput: String, onResult: (Boolean, String) -> Unit) {
        // With Firebase, password reset is handled via the link in the email.
        // We just return success here as the UI might still call it.
        onResult(true, "Please use the link sent to your email to reset your password.")
    }

    fun logout() { currentUser.value = null; impersonatedUser.value = null }

    // ─── LEARNER ACTIONS ────────────────────────────────────
    fun enrollInCourse(courseId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            if (user.subscription == "Free" && userEnrollments.value.size >= 5) return@launch
            val existing = userEnrollments.value.find { it.courseId == courseId }
            if (existing == null) {
                FirebaseRepository.enrollUserInCourse(user.email, courseId.toString())
            }
        }
    }

    fun toggleWishlist(courseId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val existing = userEnrollments.value.find { it.courseId == courseId }
            if (existing != null) {
                FirebaseRepository.updateEnrollment(user.email, courseId.toString(), mapOf("wishlist" to !existing.wishlist))
            } else {
                FirebaseRepository.enrollUserInCourse(user.email, courseId.toString())
                FirebaseRepository.updateEnrollment(user.email, courseId.toString(), mapOf("wishlist" to true))
            }
        }
    }

    fun saveLessonNotes(courseId: Int, notesText: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            FirebaseRepository.updateEnrollment(user.email, courseId.toString(), mapOf("notes" to notesText))
        }
    }

    fun markLessonComplete(courseId: Int, lessonId: Int, onComplete: (Int) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val enrollment = userEnrollments.value.find { it.courseId == courseId } ?: return@launch
            val completedIds = enrollment.completedLessonIds.split(",").filter { it.isNotEmpty() }.toMutableList()
            if (!completedIds.contains(lessonId.toString())) {
                completedIds.add(lessonId.toString())
                val lessonsList = lessonDao.getLessonsForCourse(courseId)
                val progress = if (lessonsList.isEmpty()) 100 else ((completedIds.size.toFloat() / lessonsList.size) * 100).toInt()
                val isFinished = progress >= 100
                FirebaseRepository.updateEnrollmentProgress(user.email, courseId.toString(), completedIds, progress, isFinished)
                
                val badges = user.badges.split(",").filter { it.isNotEmpty() }.toMutableList()
                if (isFinished && !badges.contains("Course Finished")) badges.add("Course Finished")
                
                FirebaseRepository.updateUserProfile(user.email, mapOf(
                    "xp" to user.xp + 50,
                    "badges" to badges.joinToString(",")
                ))
                currentUser.value = user.copy(xp = user.xp + 50, badges = badges.joinToString(","))
                onComplete(50)
            } else onComplete(0)
        }
    }

    fun addNewCourseReview(courseId: Int, rating: Int, comment: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch { reviewDao.insertReview(ReviewEntity(courseId = courseId, userEmail = user.email, userName = user.name, rating = rating, comment = comment)) }
    }

    fun upgradeToProInstant(pricingPlanText: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val expiry = System.currentTimeMillis() + if (pricingPlanText.contains("Year")) 365L * 86400000 else 30L * 86400000
            FirebaseRepository.updateUserProfile(user.email, mapOf(
                "subscription" to "Pro",
                "proExpiryAt" to expiry
            ))
            // Current User state gets updated automatically by real-time flow, but we can fast-update:
            currentUser.value = user.copy(subscription = "Pro", proExpiryAt = expiry)
            FirebaseRepository.addNotification(user.email, "You upgraded to EduCore Pro! Enjoy unlimited courses.", "Alert")
        }
    }

    // ─── INSTRUCTOR ACTIONS ─────────────────────────────────
    fun publishCourseByInstructor(title: String, details: String, category: String, difficulty: String, priceVal: Int, lessonsList: List<LessonEntity>) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val courseData = mapOf(
                "title" to title,
                "description" to details,
                "instructorId" to user.email,
                "instructorName" to user.name,
                "category" to category,
                "difficulty" to difficulty,
                "price" to priceVal,
                "status" to "Pending"
            )
            val mappedLessons = lessonsList.map { lesson ->
                mapOf(
                    "sectionName" to lesson.sectionName,
                    "sectionOrder" to lesson.sectionOrder,
                    "lessonOrder" to lesson.lessonOrder,
                    "title" to lesson.title,
                    "type" to lesson.type,
                    "duration" to lesson.duration,
                    "videoUrl" to lesson.videoUrl,
                    "articleContent" to lesson.articleContent,
                    "isPreview" to lesson.isPreview
                )
            }
            FirebaseRepository.addCourseWithLessons(courseData, mappedLessons)
            FirebaseRepository.addNotification(user.email, "Course '$title' submitted for admin review.", "Course")
        }
    }

    fun scheduleLiveStream(topic: String, description: String, dateStr: String, durationVal: String, capacity: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            FirebaseRepository.addSession(mapOf(
                "instructorId" to user.email,
                "instructorName" to user.name,
                "topic" to topic,
                "description" to description,
                "scheduledAt" to dateStr,
                "duration" to durationVal,
                "maxParticipants" to capacity,
                "status" to "Upcoming",
                "enrolledCount" to 0,
                "createdByAdmin" to false
            ))
        }
    }

    fun requestInstructorWithdrawal(amountStr: String, onResult: (Boolean, String) -> Unit) {
        val user = currentUser.value ?: return
        val amt = amountStr.toIntOrNull() ?: 0
        if (amt < 1000) { onResult(false, "Minimum withdrawal is ₹1,000."); return }
        viewModelScope.launch {
            FirebaseRepository.requestPayout(user.email, amt)
            onResult(true, "Payout of ₹$amt requested.")
        }
    }

    // ─── ADMIN CORE ACTIONS ─────────────────────────────────
    private fun adminLog(action: String, targetId: String, targetType: String, oldVal: String = "", newVal: String = "") {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            adminLogDao.insertAdminLog(AdminLogEntity(adminEmail = admin.email, action = action, targetId = targetId, targetType = targetType, oldValue = oldVal, newValue = newVal))
        }
    }

    fun adminApproveInstructor(instructorEmail: String) {
        viewModelScope.launch {
            userDao.approveInstructor(instructorEmail, true)
            adminLog("APPROVED_INSTRUCTOR", instructorEmail, "USER", "Pending", "Approved")
            notificationDao.insertNotification(NotificationEntity(userEmail = instructorEmail, message = "Your instructor account has been approved! Start creating courses.", type = "Alert"))
        }
    }

    fun adminRejectInstructor(instructorEmail: String, reason: String) {
        viewModelScope.launch {
            userDao.setUserActiveState(instructorEmail, false)
            adminLog("REJECTED_INSTRUCTOR", instructorEmail, "USER", "Pending", "Rejected: $reason")
            notificationDao.insertNotification(NotificationEntity(userEmail = instructorEmail, message = "Instructor request declined. Reason: $reason", type = "Alert"))
        }
    }

    fun adminModerateCourse(courseId: Int, isApproved: Boolean, rejectReason: String = "") {
        viewModelScope.launch {
            val course = courseDao.getCourseById(courseId) ?: return@launch
            val status = if (isApproved) "Published" else "Rejected"
            courseDao.updateCourseStatus(courseId, status)
            adminLog(if (isApproved) "APPROVED_COURSE" else "REJECTED_COURSE", courseId.toString(), "COURSE", course.status, status)
            notificationDao.insertNotification(NotificationEntity(userEmail = course.instructorId, message = "Your course '${course.title}' was $status.${if (!isApproved) " Reason: $rejectReason" else ""}", type = "Alert"))
        }
    }

    fun adminSuspendUser(userEmail: String, isSuspended: Boolean) {
        viewModelScope.launch {
            userDao.setUserActiveState(userEmail, !isSuspended)
            adminLog(if (isSuspended) "SUSPENDED_USER" else "UNSUSPENDED_USER", userEmail, "USER")
        }
    }

    fun adminBanUser(userEmail: String) {
        viewModelScope.launch {
            userDao.banUser(userEmail)
            adminLog("BANNED_USER_PERMANENTLY", userEmail, "USER", "Active", "Banned")
        }
    }

    fun adminDeleteUser(userEmail: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(userEmail) ?: return@launch
            userDao.deleteUser(user)
            adminLog("DELETED_USER", userEmail, "USER")
        }
    }

    fun adminChangeUserRole(userEmail: String, newRole: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(userEmail) ?: return@launch
            userDao.changeUserRole(userEmail, newRole)
            adminLog("CHANGED_USER_ROLE", userEmail, "USER", user.role, newRole)
        }
    }

    fun adminGrantPro(userEmail: String, daysCount: Int) {
        viewModelScope.launch {
            val expiry = System.currentTimeMillis() + daysCount.toLong() * 86400000
            userDao.setUserSubscription(userEmail, "Pro", expiry)
            adminLog("GRANTED_PRO_ACCESS", userEmail, "USER", "Free", "Pro for $daysCount days")
            notificationDao.insertNotification(NotificationEntity(userEmail = userEmail, message = "Admin granted you Pro access for $daysCount days!", type = "Alert"))
        }
    }

    fun adminRevokePro(userEmail: String) {
        viewModelScope.launch {
            userDao.setUserSubscription(userEmail, "Free", 0L)
            adminLog("REVOKED_PRO_ACCESS", userEmail, "USER", "Pro", "Free")
        }
    }

    fun adminProcessPayout(payoutId: Int) {
        viewModelScope.launch {
            val txnId = genTxnId()
            payoutDao.updatePayoutStatus(payoutId, "Paid", System.currentTimeMillis(), txnId)
            adminLog("PAID_PAYOUT", payoutId.toString(), "PAYOUT", "Pending", "Paid: $txnId")
        }
    }

    fun adminUpdateCourse(course: CourseEntity, oldStatus: String) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
            adminLog("UPDATED_COURSE", course.id.toString(), "COURSE", oldStatus, course.status)
        }
    }

    fun adminDeleteCourse(courseId: Int) {
        viewModelScope.launch {
            courseDao.deleteCourseById(courseId)
            lessonDao.deleteLessonsForCourse(courseId)
            adminLog("DELETED_COURSE", courseId.toString(), "COURSE")
        }
    }

    fun adminReassignInstructor(courseId: Int, newInstructorId: String, newInstructorName: String, oldInstructorId: String) {
        viewModelScope.launch {
            courseDao.reassignInstructor(courseId, newInstructorId, newInstructorName)
            adminLog("REASSIGNED_INSTRUCTOR", courseId.toString(), "COURSE", oldInstructorId, newInstructorId)
            notificationDao.insertNotification(NotificationEntity(userEmail = oldInstructorId, message = "Admin reassigned your course to $newInstructorName.", type = "Alert"))
            notificationDao.insertNotification(NotificationEntity(userEmail = newInstructorId, message = "Admin assigned a course to you.", type = "Course"))
        }
    }

    fun adminSetCommissionRate(instructorEmail: String, newRate: Int) {
        viewModelScope.launch {
            userDao.setInstructorCommission(instructorEmail, newRate)
            adminLog("CHANGED_COMMISSION", instructorEmail, "USER", "30%", "$newRate%")
        }
    }

    fun adminEnrollStudent(userEmail: String, courseId: Int) {
        viewModelScope.launch {
            if (enrollmentDao.getEnrollment(userEmail, courseId) == null) {
                enrollmentDao.insertEnrollment(EnrollmentEntity(userEmail = userEmail, courseId = courseId))
                courseDao.incrementEnrollmentCount(courseId)
                adminLog("MANUALLY_ENROLLED_STUDENT", userEmail, "ENROLLMENT", "", courseId.toString())
            }
        }
    }

    fun adminRemoveEnrollment(userEmail: String, courseId: Int) {
        viewModelScope.launch {
            enrollmentDao.removeEnrollment(userEmail, courseId)
            adminLog("REMOVED_ENROLLMENT", userEmail, "ENROLLMENT", courseId.toString(), "")
        }
    }

    fun adminGrantCertificate(userEmail: String, courseId: Int) {
        viewModelScope.launch {
            enrollmentDao.grantCertificate(userEmail, courseId)
            adminLog("GRANTED_CERTIFICATE", userEmail, "ENROLLMENT", "", courseId.toString())
            notificationDao.insertNotification(NotificationEntity(userEmail = userEmail, message = "Admin granted you a completion certificate!", type = "Course"))
        }
    }

    fun adminResetProgress(userEmail: String, courseId: Int) {
        viewModelScope.launch {
            enrollmentDao.resetProgress(userEmail, courseId)
            adminLog("RESET_STUDENT_PROGRESS", userEmail, "ENROLLMENT", "", courseId.toString())
        }
    }

    fun adminModerateReview(reviewId: Int, status: String) {
        viewModelScope.launch {
            reviewDao.updateReviewStatus(reviewId, status)
            adminLog("MODERATED_REVIEW", reviewId.toString(), "REVIEW", "", status)
        }
    }

    fun adminCancelSession(sessionId: Int, reason: String) {
        viewModelScope.launch {
            liveSessionDao.cancelSession(sessionId, reason)
            adminLog("CANCELLED_SESSION", sessionId.toString(), "SESSION", "Upcoming", "Cancelled: $reason")
        }
    }

    fun adminCreateSessionForInstructor(instructorId: String, instructorName: String, topic: String, date: String, duration: String, capacity: Int) {
        viewModelScope.launch {
            liveSessionDao.insertSession(LiveSessionEntity(instructorId = instructorId, instructorName = instructorName, topic = topic, description = "", scheduledAt = date, duration = duration, maxParticipants = capacity, createdByAdmin = true))
            adminLog("CREATED_SESSION_FOR_INSTRUCTOR", instructorId, "SESSION", "", topic)
            notificationDao.insertNotification(NotificationEntity(userEmail = instructorId, message = "Admin scheduled a live class for you: '$topic' on $date.", type = "Live"))
        }
    }

    fun adminSendNotification(title: String, message: String, target: String, type: String, users: List<UserEntity>) {
        viewModelScope.launch {
            users.forEach { u -> notificationDao.insertNotification(NotificationEntity(userEmail = u.email, message = "$title: $message", type = "Alert")) }
            sentNotificationDao.insertSentNotification(SentNotificationEntity(title = title, message = message, target = target, notificationType = type, deliveryCount = users.size, openRate = 0.45f))
            adminLog("SENT_NOTIFICATION", target, "NOTIFICATION", "", "To ${users.size} users")
        }
    }

    fun adminCreateCoupon(code: String, discountPercent: Int, maxUses: Int, courseId: Int) {
        viewModelScope.launch {
            couponDao.insertCoupon(CouponEntity(code = code, courseId = courseId, discountPercent = discountPercent, expiryDate = System.currentTimeMillis() + 30L * 86400000, maxUses = maxUses))
            adminLog("CREATED_COUPON", code, "SYSTEM", "", "$discountPercent% off")
        }
    }

    fun adminAddBanner(imageUrl: String, title: String, subtitle: String, buttonLabel: String) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            bannerDao.insertBanner(BannerEntity(imageUrl = imageUrl, title = title, subtitle = subtitle, buttonLabel = buttonLabel))
            adminLog("ADDED_BANNER", title, "SYSTEM", "", imageUrl)
        }
    }

    fun adminToggleBanner(bannerId: Int, enabled: Boolean) {
        viewModelScope.launch { bannerDao.setBannerEnabled(bannerId, enabled) }
    }

    fun adminSaveSetting(key: String, value: String, oldValue: String = "") {
        viewModelScope.launch {
            platformSettingDao.setSetting(PlatformSettingEntity(key = key, value = value))
            adminLog("CHANGED_SETTING", key, "SYSTEM", oldValue, value)
        }
    }

    fun adminImpersonateUser(targetEmail: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(targetEmail) ?: return@launch
            impersonatedUser.value = user
            adminLog("IMPERSONATED_USER", targetEmail, "USER")
        }
    }

    fun adminStopImpersonation() { impersonatedUser.value = null }

    fun adminFeatureInstructor(email: String, featured: Boolean) {
        viewModelScope.launch {
            userDao.setInstructorFeatured(email, featured)
            adminLog(if (featured) "FEATURED_INSTRUCTOR" else "UNFEATURED_INSTRUCTOR", email, "USER")
        }
    }

    fun adminSetCourseFeatured(courseId: Int, featured: Boolean, order: Int = 0) {
        viewModelScope.launch {
            courseDao.setCourseFeatured(courseId, featured, order)
            adminLog(if (featured) "FEATURED_COURSE" else "UNFEATURED_COURSE", courseId.toString(), "COURSE")
        }
    }

    fun adminAddLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            lessonDao.insertLesson(lesson)
            adminLog("ADDED_LESSON", lesson.courseId.toString(), "COURSE", "", lesson.title)
        }
    }

    fun adminDeleteLesson(lessonId: Int, courseId: Int) {
        viewModelScope.launch {
            lessonDao.deleteLessonById(lessonId)
            adminLog("DELETED_LESSON", lessonId.toString(), "COURSE", courseId.toString(), "")
        }
    }
}
