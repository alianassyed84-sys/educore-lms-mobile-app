package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseRepository
 * ------------------
 * Single source of truth for all Firebase Auth & Firestore operations.
 * The ViewModel calls this instead of Room DAOs for auth and cloud data.
 *
 * Collections in Firestore:
 *   users/          → user profile documents (keyed by UID)
 *   courses/        → course documents
 *   enrollments/    → enrollment records
 *   live_sessions/  → live session documents
 *   payouts/        → payout records
 *   notifications/  → per-user notifications
 *   admin_logs/     → admin audit trail
 */
object FirebaseRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    // ─── Firestore collection references ────────────────────────────────
    private val usersCol get() = db.collection("users")
    private val coursesCol get() = db.collection("courses")
    private val enrollmentsCol get() = db.collection("enrollments")
    private val sessionsCol get() = db.collection("live_sessions")
    private val payoutsCol get() = db.collection("payouts")
    private val notificationsCol get() = db.collection("notifications")
    private val adminLogsCol get() = db.collection("admin_logs")
    private val bannersCol get() = db.collection("banners")
    private val sentNotificationsCol get() = db.collection("sent_notifications")
    private val platformSettingsCol get() = db.collection("platform_settings")
    private val couponsCol get() = db.collection("coupons")

    // ════════════════════════════════════════════════════════════════════
    //  AUTH SECTION
    // ════════════════════════════════════════════════════════════════════

    /** Returns the currently signed-in Firebase user, or null. */
    val currentFirebaseUser: FirebaseUser? get() = auth.currentUser

    /**
     * Register a new user with Firebase Auth and write their profile to Firestore.
     * @param name      Display name
     * @param email     Email address
     * @param password  Password (min 8 chars)
     * @param role      "Learner" | "Instructor"
     * @return Result wrapping the UID on success, or an exception on failure.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<String> = runCatching {
        // 1. Create the Firebase Auth user
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: error("Firebase returned null UID after registration.")

        // 2. Send email verification
        authResult.user?.sendEmailVerification()?.await()

        // 3. Write the profile document to Firestore users/{uid}
        val profile = mapOf(
            "uid"            to uid,
            "email"          to email,
            "name"           to name,
            "role"           to role,
            "isVerified"     to false,          // set true after email verification
            "isApproved"     to (role == "Learner"),
            "isActive"       to true,
            "isBanned"       to false,
            "subscription"   to "Free",
            "proExpiryAt"    to 0L,
            "streakDays"     to "0/7",
            "streakCount"    to 0,
            "xp"             to 0,
            "badges"         to "",
            "phone"          to "",
            "bio"            to "",
            "photoUrl"       to "",
            "expertiseTags"  to "",
            "commissionRate" to 30,
            "totalEarnings"  to 0,
            "isFeatured"     to false,
            "createdAt"      to System.currentTimeMillis()
        )
        usersCol.document(uid).set(profile).await()
        uid
    }

    /**
     * Sign in with email and password using Firebase Auth.
     * @return Result wrapping the UID on success.
     */
    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("Login succeeded but Firebase returned null UID.")
    }

    /**
     * Send a password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Sign the current user out.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Reload the current Firebase user to get the latest emailVerified status.
     * Returns true if the email is now verified.
     */
    suspend fun reloadAndCheckVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified == true
    }

    /**
     * Mark a user as verified in Firestore (called after email link confirmed).
     */
    suspend fun markUserVerifiedInFirestore(uid: String) {
        usersCol.document(uid).update("isVerified", true).await()
    }

    /**
     * Resend the Firebase verification email to the current user.
     */
    suspend fun resendVerificationEmail() {
        try { auth.currentUser?.sendEmailVerification()?.await() } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * Sign in with Google using Firebase Authentication.
     * If the user profile does not exist in Firestore, creates one automatically.
     */
    suspend fun loginWithGoogle(idToken: String, selectedRole: String): Result<String> = runCatching {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val user = authResult.user ?: error("Google Sign-In returned null user.")
        val uid = user.uid
        val email = user.email ?: ""
        val name = user.displayName ?: "Google User"
        val photoUrl = user.photoUrl?.toString() ?: ""

        // Check if the user document exists in Firestore
        val profileDoc = usersCol.document(uid).get().await()
        if (!profileDoc.exists()) {
            if (selectedRole == "Admin") {
                error("Admin accounts cannot be created publicly.")
            }
            // New user registration via Google Sign-In
            val profile = mapOf(
                "uid"            to uid,
                "email"          to email,
                "name"           to name,
                "role"           to selectedRole,
                "isVerified"     to true,          // Google email is already verified!
                "isApproved"     to (selectedRole == "Learner"),
                "isActive"       to true,
                "isBanned"       to false,
                "subscription"   to "Free",
                "proExpiryAt"    to 0L,
                "streakDays"     to "0/7",
                "streakCount"    to 0,
                "xp"             to 0,
                "badges"         to "",
                "phone"          to "",
                "bio"            to "",
                "photoUrl"       to photoUrl,
                "expertiseTags"  to "",
                "commissionRate" to 30,
                "totalEarnings"  to 0,
                "isFeatured"     to false,
                "createdAt"      to System.currentTimeMillis()
            )
            usersCol.document(uid).set(profile).await()
        }
        uid
    }

    // ════════════════════════════════════════════════════════════════════
    //  USER PROFILE SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Fetch a user profile document from Firestore by UID.
     * Returns null if no document exists.
     */
    suspend fun getUserProfile(uid: String): Map<String, Any?>? {
        val doc = usersCol.document(uid).get().await()
        return if (doc.exists()) doc.data else null
    }

    /**
     * Observe a user profile as a Flow (real-time listener).
     */
    fun getUserProfileFlow(uid: String): Flow<Map<String, Any?>?> = callbackFlow {
        val listener = usersCol.document(uid).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.data)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Update specific fields in the user's Firestore profile.
     */
    suspend fun updateUserProfile(uid: String, fields: Map<String, Any?>) {
        usersCol.document(uid).set(fields, SetOptions.merge()).await()
    }

    /**
     * Observe all users as a real-time Flow.
     */
    fun getAllUsersFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = usersCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id)?.plus("uid" to it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ════════════════════════════════════════════════════════════════════
    //  COURSES SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Observe all courses as a real-time Flow.
     */
    fun getAllCoursesFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = coursesCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observe courses for a specific instructor.
     */
    fun getCoursesByInstructorFlow(instructorId: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = coursesCol
            .whereEqualTo("instructorId", instructorId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Add a new course document to Firestore.
     * @return the auto-generated Firestore document ID.
     */
    suspend fun addCourse(courseData: Map<String, Any?>): String {
        val ref = coursesCol.add(courseData).await()
        return ref.id
    }

    /**
     * Add a new course document along with its lessons as a subcollection.
     */
    suspend fun addCourseWithLessons(courseData: Map<String, Any?>, lessons: List<Map<String, Any?>>): String {
        val ref = coursesCol.add(courseData).await()
        for (lesson in lessons) {
            ref.collection("lessons").add(lesson).await()
        }
        return ref.id
    }

    /**
     * Update a course document in Firestore.
     */
    suspend fun updateCourse(firestoreId: String, fields: Map<String, Any?>) {
        coursesCol.document(firestoreId).set(fields, SetOptions.merge()).await()
    }

    /**
     * Update only the status field of a course.
     */
    suspend fun updateCourseStatus(firestoreId: String, status: String) {
        coursesCol.document(firestoreId).update("status", status).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  ENROLLMENTS SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Observe enrollments for the current user as a real-time Flow.
     */
    fun getEnrollmentsForUserFlow(uid: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = enrollmentsCol
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Enroll the user in a course (creates/updates an enrollment doc).
     */
    suspend fun enrollUserInCourse(userId: String, courseId: String) {
        val docId = "${userId}_${courseId}"
        val data = mapOf(
            "userId"             to userId,
            "courseId"           to courseId,
            "progress"           to 0,
            "completedLessonIds" to emptyList<String>(),
            "isCompleted"        to false,
            "certificateGranted" to false,
            "wishlist"           to false,
            "notes"              to "",
            "enrolledAt"         to System.currentTimeMillis()
        )
        enrollmentsCol.document(docId).set(data, SetOptions.merge()).await()
    }

    /**
     * Update progress for a specific enrollment.
     */
    suspend fun updateEnrollmentProgress(
        userId: String,
        courseId: String,
        completedIds: List<String>,
        progress: Int,
        isCompleted: Boolean
    ) {
        val docId = "${userId}_${courseId}"
        enrollmentsCol.document(docId).update(
            mapOf(
                "completedLessonIds" to completedIds,
                "progress"           to progress,
                "isCompleted"        to isCompleted
            )
        ).await()
    }

    /**
     * Update arbitrary fields on a specific enrollment.
     */
    suspend fun updateEnrollment(userId: String, courseId: String, fields: Map<String, Any?>) {
        val docId = "${userId}_${courseId}"
        enrollmentsCol.document(docId).set(fields, SetOptions.merge()).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  NOTIFICATIONS SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Observe notifications for a specific user as a real-time Flow.
     */
    fun getNotificationsForUserFlow(uid: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = notificationsCol
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // Gracefully fallback on query errors instead of crashing the Flow context
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                
                // Sort in-memory locally by "createdAt" to completely bypass composite index requirements!
                val sortedList = list.sortedByDescending { doc ->
                    when (val cat = doc["createdAt"]) {
                        is com.google.firebase.Timestamp -> cat.toDate().time
                        is java.util.Date -> cat.time
                        is Long -> cat
                        is String -> try { cat.toLong() } catch(e: Exception) { 0L }
                        else -> 0L
                    }
                }
                trySend(sortedList)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Add a notification document for a user.
     */
    suspend fun addNotification(userId: String, message: String, type: String) {
        notificationsCol.add(
            mapOf(
                "userId"    to userId,
                "message"   to message,
                "type"      to type,
                "isRead"    to false,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
    }

    /**
     * Mark all notifications as read for a user.
     */
    suspend fun markAllNotificationsRead(uid: String) {
        val docs = notificationsCol.whereEqualTo("userId", uid)
            .whereEqualTo("isRead", false).get().await()
        val batch = db.batch()
        docs.forEach { batch.update(it.reference, "isRead", true) }
        batch.commit().await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  ADMIN LOGS SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Write an admin action log to Firestore.
     */
    suspend fun addAdminLog(
        adminUid: String,
        action: String,
        targetId: String,
        targetType: String,
        oldValue: String = "",
        newValue: String = ""
    ) {
        adminLogsCol.add(
            mapOf(
                "adminUid"   to adminUid,
                "action"     to action,
                "targetId"   to targetId,
                "targetType" to targetType,
                "oldValue"   to oldValue,
                "newValue"   to newValue,
                "timestamp"  to System.currentTimeMillis()
            )
        ).await()
    }

    /**
     * Observe all admin logs as a real-time Flow.
     */
    fun getAllAdminLogsFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = adminLogsCol.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ════════════════════════════════════════════════════════════════════
    //  BANNERS & SENT NOTIFICATIONS SECTION
    // ════════════════════════════════════════════════════════════════════

    fun getAllBannersFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = bannersCol.orderBy("displayOrder")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllSentNotificationsFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = sentNotificationsCol.orderBy("sentAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ════════════════════════════════════════════════════════════════════
    //  LIVE SESSIONS SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Observe all live sessions as a real-time Flow.
     */
    fun getAllSessionsFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = sessionsCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Add a new live session document.
     */
    suspend fun addSession(sessionData: Map<String, Any?>): String {
        val ref = sessionsCol.add(sessionData).await()
        return ref.id
    }

    // ════════════════════════════════════════════════════════════════════
    //  PAYOUTS SECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Observe payouts for an instructor as a real-time Flow.
     */
    fun getPayoutsForInstructorFlow(instructorUid: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = payoutsCol
            .whereEqualTo("instructorId", instructorUid)
            .orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Request a payout.
     */
    suspend fun requestPayout(instructorId: String, amount: Int): String {
        val ref = payoutsCol.add(
            mapOf(
                "instructorId" to instructorId,
                "amount"       to amount,
                "status"       to "Pending",
                "requestedAt"  to System.currentTimeMillis(),
                "processedAt"  to 0L,
                "transactionId" to ""
            )
        ).await()
        return ref.id
    }

    /**
     * Helper to find a User's UID by email
     */
    suspend fun getUidByEmail(email: String): String? {
        val docs = usersCol.whereEqualTo("email", email).limit(1).get().await()
        return docs.documents.firstOrNull()?.id
    }

    /**
     * Helper to find a Course's firestore ID by title (since UI uses Int hashcodes temporarily)
     */
    suspend fun getCourseIdByTitle(title: String): String? {
        val docs = coursesCol.whereEqualTo("title", title).limit(1).get().await()
        return docs.documents.firstOrNull()?.id
    }

    /**
     * Helper to find a Session's firestore ID by topic
     */
    suspend fun getSessionIdByTopic(topic: String): String? {
        val docs = sessionsCol.whereEqualTo("topic", topic).limit(1).get().await()
        return docs.documents.firstOrNull()?.id
    }

    /**
     * Observe all payouts as a real-time Flow.
     */
    fun getAllPayoutsFlow(): Flow<List<Map<String, Any?>>> = callbackFlow {
        val listener = payoutsCol.orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.plus("firestoreId" to it.id) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ════════════════════════════════════════════════════════════════════
    //  REVIEWS SECTION
    // ════════════════════════════════════════════════════════════════════

    /** Add a review to the reviews collection. */
    suspend fun db_addReview(courseId: String, userEmail: String, userName: String, rating: Int, comment: String) {
        db.collection("reviews").add(mapOf(
            "courseId"  to courseId,
            "userEmail" to userEmail,
            "userName"  to userName,
            "rating"    to rating.toLong(),
            "comment"   to comment,
            "status"    to "Approved",
            "createdAt" to System.currentTimeMillis()
        )).await()
    }

    /** Update a review document by its Firestore ID. */
    suspend fun updateReview(firestoreId: String, fields: Map<String, Any?>) {
        db.collection("reviews").document(firestoreId).set(fields, SetOptions.merge()).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  PAYOUTS — ADDITIONAL HELPERS
    // ════════════════════════════════════════════════════════════════════

    /** Get payout Firestore ID by instructor and Pending status. */
    suspend fun getPayoutIdByInstructor(instructorId: String): String? {
        val docs = payoutsCol
            .whereEqualTo("instructorId", instructorId)
            .whereEqualTo("status", "Pending")
            .limit(1).get().await()
        return docs.documents.firstOrNull()?.id
    }

    /** Update arbitrary fields on a payout document. */
    suspend fun updatePayout(firestoreId: String, fields: Map<String, Any?>) {
        payoutsCol.document(firestoreId).set(fields, SetOptions.merge()).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  COURSES — ADDITIONAL HELPERS
    // ════════════════════════════════════════════════════════════════════

    /** Delete a course document from Firestore. */
    suspend fun deleteCourse(firestoreId: String) {
        coursesCol.document(firestoreId).delete().await()
    }

    /** Add a lesson to a course's lessons sub-collection. */
    suspend fun addLessonToCourse(courseFirestoreId: String, lessonData: Map<String, Any?>) {
        coursesCol.document(courseFirestoreId).collection("lessons").add(lessonData).await()
    }

    /** Delete a lesson from a course's lessons sub-collection. */
    suspend fun deleteLessonFromCourse(courseFirestoreId: String, lessonFirestoreId: String) {
        coursesCol.document(courseFirestoreId).collection("lessons").document(lessonFirestoreId).delete().await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  ENROLLMENTS — ADDITIONAL HELPERS
    // ════════════════════════════════════════════════════════════════════

    /** Remove an enrollment document. */
    suspend fun removeEnrollment(userId: String, courseId: String) {
        val docId = "${userId}_${courseId}"
        enrollmentsCol.document(docId).delete().await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  SESSIONS — ADDITIONAL HELPERS
    // ════════════════════════════════════════════════════════════════════

    /** Update a live session document. */
    suspend fun updateSession(firestoreId: String, fields: Map<String, Any?>) {
        sessionsCol.document(firestoreId).set(fields, SetOptions.merge()).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  SENT NOTIFICATIONS
    // ════════════════════════════════════════════════════════════════════

    /** Add a sent notification record. */
    suspend fun addSentNotification(data: Map<String, Any?>) {
        sentNotificationsCol.add(data).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  COUPONS
    // ════════════════════════════════════════════════════════════════════

    /** Add a coupon document. */
    suspend fun addCoupon(data: Map<String, Any?>) {
        val code = data["code"] as? String ?: return
        couponsCol.document(code).set(data).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  BANNERS
    // ════════════════════════════════════════════════════════════════════

    /** Add a banner document. */
    suspend fun addBanner(data: Map<String, Any?>) {
        bannersCol.add(data).await()
    }

    /** Find banner Firestore ID by title. */
    suspend fun getBannerIdByTitle(title: String): String? {
        val docs = bannersCol.whereEqualTo("title", title).limit(1).get().await()
        return docs.documents.firstOrNull()?.id
    }

    /** Update a banner document. */
    suspend fun updateBanner(firestoreId: String, fields: Map<String, Any?>) {
        bannersCol.document(firestoreId).set(fields, SetOptions.merge()).await()
    }

    // ════════════════════════════════════════════════════════════════════
    //  PLATFORM SETTINGS
    // ════════════════════════════════════════════════════════════════════

    /** Update (or create) a platform setting document by key. */
    suspend fun updateSetting(key: String, value: String) {
        platformSettingsCol.document(key).set(
            mapOf("key" to key, "value" to value, "updatedAt" to System.currentTimeMillis()),
            SetOptions.merge()
        ).await()
    }
}
