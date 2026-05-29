package com.example.data

/**
 * FirestoreExtensions.kt
 * ──────────────────────
 * Converts Firestore document maps (Map<String, Any?>) into strongly-typed
 * Room/domain entities so the rest of the app keeps working with the same
 * data models during the Room → Firestore migration.
 */

/**
 * Converts a Firestore user document map into a [UserEntity].
 * Missing fields gracefully fall back to safe defaults.
 *
 * @param uid The Firebase Auth UID (used as the email-equivalent key in Firestore).
 */
fun Map<String, Any?>.toUserEntity(uid: String): UserEntity = UserEntity(
    email           = (this["email"]          as? String)  ?: uid,
    name            = (this["name"]           as? String)  ?: "Unknown",
    passwordHash    = "",                                  // Never stored in Firestore
    role            = (this["role"]           as? String)  ?: "Learner",
    isVerified      = (this["isVerified"]     as? Boolean) ?: false,
    isApproved      = (this["isApproved"]     as? Boolean) ?: false,
    isActive        = (this["isActive"]       as? Boolean) ?: true,
    isBanned        = (this["isBanned"]       as? Boolean) ?: false,
    subscription    = (this["subscription"]   as? String)  ?: "Free",
    proExpiryAt     = (this["proExpiryAt"]    as? Long)    ?: 0L,
    streakDays      = (this["streakDays"]     as? String)  ?: "0/7",
    streakCount     = ((this["streakCount"]   as? Long)?.toInt()) ?: 0,
    xp              = ((this["xp"]            as? Long)?.toInt()) ?: 0,
    badges          = (this["badges"]         as? String)  ?: "",
    phone           = (this["phone"]          as? String)  ?: "",
    bio             = (this["bio"]            as? String)  ?: "",
    photoUrl        = (this["photoUrl"]       as? String)  ?: "",
    expertiseTags   = (this["expertiseTags"]  as? String)  ?: "",
    commissionRate  = ((this["commissionRate"] as? Long)?.toInt()) ?: 30,
    totalEarnings   = ((this["totalEarnings"] as? Long)?.toInt()) ?: 0,
    suspensionReason = (this["suspensionReason"] as? String) ?: "",
    isFeatured      = (this["isFeatured"]     as? Boolean) ?: false,
    createdAt       = (this["createdAt"]      as? Long)    ?: System.currentTimeMillis(),
    lastActiveAt    = (this["lastActiveAt"]   as? Long)    ?: System.currentTimeMillis()
)

/**
 * Converts a Firestore course document map into a [CourseEntity].
 */
fun Map<String, Any?>.toCourseEntity(): CourseEntity = CourseEntity(
    id              = ((this["id"]              as? Long)?.toInt()) ?: 0,
    title           = (this["title"]            as? String) ?: "",
    description     = (this["description"]      as? String) ?: "",
    longDescription = (this["longDescription"]  as? String) ?: "",
    instructorId    = (this["instructorId"]     as? String) ?: "",
    instructorName  = (this["instructorName"]   as? String) ?: "",
    coInstructorId  = (this["coInstructorId"]   as? String) ?: "",
    coInstructorName= (this["coInstructorName"] as? String) ?: "",
    revenueSharePrimary = ((this["revenueSharePrimary"] as? Long)?.toInt()) ?: 100,
    category        = (this["category"]         as? String) ?: "",
    subCategory     = (this["subCategory"]      as? String) ?: "",
    difficulty      = (this["difficulty"]       as? String) ?: "Beginner",
    language        = (this["language"]         as? String) ?: "English",
    tags            = (this["tags"]             as? String) ?: "",
    price           = ((this["price"]           as? Long)?.toInt()) ?: 0,
    discountPrice   = ((this["discountPrice"]   as? Long)?.toInt()) ?: 0,
    discountExpiry  = (this["discountExpiry"]   as? Long)   ?: 0L,
    enrollmentCap   = ((this["enrollmentCap"]   as? Long)?.toInt()) ?: 0,
    accessDuration  = (this["accessDuration"]   as? String) ?: "Lifetime",
    status          = (this["status"]           as? String) ?: "Pending",
    thumbnail       = (this["thumbnail"]        as? String) ?: "coding_placeholder",
    introVideoUrl   = (this["introVideoUrl"]    as? String) ?: "",
    certificateTemplate = ((this["certificateTemplate"] as? Long)?.toInt()) ?: 1,
    rating          = ((this["rating"]          as? Double)?.toFloat()) ?: 4.5f,
    reviewCount     = ((this["reviewCount"]     as? Long)?.toInt()) ?: 0,
    enrolledCount   = ((this["enrolledCount"]   as? Long)?.toInt()) ?: 0,
    isFeatured      = (this["isFeatured"]       as? Boolean) ?: false,
    featuredOrder   = ((this["featuredOrder"]   as? Long)?.toInt()) ?: 0
)

fun Map<String, Any?>.toLiveSessionEntity(): LiveSessionEntity = LiveSessionEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    instructorId = (this["instructorId"] as? String) ?: "",
    instructorName = (this["instructorName"] as? String) ?: "",
    topic = (this["topic"] as? String) ?: "",
    description = (this["description"] as? String) ?: "",
    scheduledAt = (this["scheduledAt"] as? String) ?: "",
    duration = (this["duration"] as? String) ?: "",
    maxParticipants = ((this["maxParticipants"] as? Long)?.toInt()) ?: 100,
    status = (this["status"] as? String) ?: "Upcoming",
    enrolledCount = ((this["enrolledCount"] as? Long)?.toInt()) ?: 0,
    cancelReason = (this["cancelReason"] as? String) ?: "",
    createdByAdmin = (this["createdByAdmin"] as? Boolean) ?: false
)

fun Map<String, Any?>.toPayoutEntity(): PayoutEntity = PayoutEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    instructorId = (this["instructorId"] as? String) ?: "",
    amount = ((this["amount"] as? Long)?.toInt()) ?: 0,
    status = (this["status"] as? String) ?: "Pending",
    requestedAt = (this["requestedAt"] as? Long) ?: 0L,
    processedAt = (this["processedAt"] as? Long) ?: 0L,
    transactionId = (this["transactionId"] as? String) ?: ""
)

fun Map<String, Any?>.toEnrollmentEntity(): EnrollmentEntity = EnrollmentEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    userEmail = (this["userId"] as? String) ?: "",
    courseId = ((this["courseId"] as? String)?.hashCode()) ?: 0,
    progress = ((this["progress"] as? Long)?.toInt()) ?: 0,
    completedLessonIds = ((this["completedLessonIds"] as? List<*>)?.joinToString(",")) ?: "",
    notes = (this["notes"] as? String) ?: "",
    wishlist = (this["wishlist"] as? Boolean) ?: false,
    isCompleted = (this["isCompleted"] as? Boolean) ?: false,
    certificateGranted = (this["certificateGranted"] as? Boolean) ?: false,
    enrolledAt = (this["enrolledAt"] as? Long) ?: System.currentTimeMillis()
)

fun Map<String, Any?>.toNotificationEntity(): NotificationEntity = NotificationEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    userEmail = (this["userId"] as? String) ?: "",
    message = (this["message"] as? String) ?: "",
    type = (this["type"] as? String) ?: "Alert",
    isRead = (this["isRead"] as? Boolean) ?: false,
    createdAt = (this["createdAt"] as? Long) ?: System.currentTimeMillis()
)

fun Map<String, Any?>.toAdminLogEntity(): AdminLogEntity = AdminLogEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    adminEmail = (this["adminUid"] as? String) ?: "",
    action = (this["action"] as? String) ?: "",
    targetId = (this["targetId"] as? String) ?: "",
    targetType = (this["targetType"] as? String) ?: "",
    oldValue = (this["oldValue"] as? String) ?: "",
    newValue = (this["newValue"] as? String) ?: "",
    timestamp = (this["timestamp"] as? Long) ?: System.currentTimeMillis()
)

fun Map<String, Any?>.toBannerEntity(): BannerEntity = BannerEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    imageUrl = (this["imageUrl"] as? String) ?: "",
    title = (this["title"] as? String) ?: "",
    subtitle = (this["subtitle"] as? String) ?: "",
    buttonLabel = (this["buttonLabel"] as? String) ?: "Explore",
    isEnabled = (this["isEnabled"] as? Boolean) ?: true,
    displayOrder = ((this["displayOrder"] as? Long)?.toInt()) ?: 0
)

fun Map<String, Any?>.toSentNotificationEntity(): SentNotificationEntity = SentNotificationEntity(
    id = ((this["firestoreId"] as? String)?.hashCode()) ?: 0,
    title = (this["title"] as? String) ?: "",
    message = (this["message"] as? String) ?: "",
    target = (this["target"] as? String) ?: "",
    notificationType = (this["notificationType"] as? String) ?: "Push",
    deliveryCount = ((this["deliveryCount"] as? Long)?.toInt()) ?: 0,
    openRate = ((this["openRate"] as? Double)?.toFloat()) ?: 0f
)
