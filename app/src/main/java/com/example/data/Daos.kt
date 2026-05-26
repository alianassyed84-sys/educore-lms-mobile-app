package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmailFlow(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET isApproved = :approved WHERE email = :email")
    suspend fun approveInstructor(email: String, approved: Boolean)

    @Query("UPDATE users SET isActive = :active WHERE email = :email")
    suspend fun setUserActiveState(email: String, active: Boolean)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE status = :status")
    fun getCoursesByStatusFlow(status: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("UPDATE courses SET status = :status WHERE id = :id")
    suspend fun updateCourseStatus(id: Int, status: String)

    @Query("UPDATE courses SET enrolledCount = enrolledCount + 1 WHERE id = :id")
    suspend fun incrementEnrollmentCount(id: Int)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    fun getLessonsForCourseFlow(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonsForCourse(courseId: Int): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE userEmail = :email")
    fun getEnrollmentsForUserFlow(email: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE userEmail = :email AND courseId = :courseId")
    suspend fun getEnrollment(email: String, courseId: Int): EnrollmentEntity?

    @Query("SELECT * FROM enrollments WHERE userEmail = :email AND courseId = :courseId")
    fun getEnrollmentFlow(email: String, courseId: Int): Flow<EnrollmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Update
    suspend fun updateEnrollment(enrollment: EnrollmentEntity)

    @Query("UPDATE enrollments SET completedLessonIds = :completedIds, progress = :progress WHERE userEmail = :email AND courseId = :courseId")
    suspend fun updateProgress(email: String, courseId: Int, completedIds: String, progress: Int)

    @Query("UPDATE enrollments SET notes = :notes WHERE userEmail = :email AND courseId = :courseId")
    suspend fun updateNotes(email: String, courseId: Int, notes: String)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getReviewsForCourseFlow(courseId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

@Dao
interface LiveSessionDao {
    @Query("SELECT * FROM live_sessions ORDER BY scheduledAt ASC")
    fun getAllSessionsFlow(): Flow<List<LiveSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LiveSessionEntity)

    @Update
    suspend fun updateSession(session: LiveSessionEntity)

    @Query("DELETE FROM live_sessions WHERE id = :id")
    suspend fun deleteSession(id: Int)
}

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payouts ORDER BY requestedAt DESC")
    fun getAllPayoutsFlow(): Flow<List<PayoutEntity>>

    @Query("SELECT * FROM payouts WHERE instructorId = :instructorId ORDER BY requestedAt DESC")
    fun getPayoutsForInstructorFlow(instructorId: String): Flow<List<PayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: PayoutEntity)

    @Query("UPDATE payouts SET status = :status, processedAt = :processedAt, transactionId = :transactionId WHERE id = :id")
    suspend fun updatePayoutStatus(id: Int, status: String, processedAt: Long, transactionId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getNotificationsForUserFlow(email: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email")
    suspend fun markAllAsRead(email: String)
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogsFlow(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)
}
