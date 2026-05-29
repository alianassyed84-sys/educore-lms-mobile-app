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
