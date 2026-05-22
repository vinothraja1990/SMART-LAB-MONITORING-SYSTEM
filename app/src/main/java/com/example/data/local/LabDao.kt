package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LabDao {
    // Labs
    @Query("SELECT * FROM labs")
    fun getAllLabs(): Flow<List<Lab>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabs(labs: List<Lab>)

    // Systems
    @Query("SELECT * FROM systems")
    fun getAllSystems(): Flow<List<SystemEntity>>

    @Query("SELECT * FROM systems WHERE labId = :labId ORDER BY systemNumber ASC")
    fun getSystemsForLab(labId: String): Flow<List<SystemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystem(system: SystemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystems(systems: List<SystemEntity>)

    @Update
    suspend fun updateSystem(system: SystemEntity)

    @Query("SELECT * FROM systems WHERE id = :id")
    suspend fun getSystemById(id: String): SystemEntity?

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY loginTime DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    // Maintenance
    @Query("SELECT * FROM maintenance ORDER BY scheduledDate DESC")
    fun getAllMaintenanceLogs(): Flow<List<MaintenanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceLog(log: MaintenanceLog)

    @Update
    suspend fun updateMaintenanceLog(log: MaintenanceLog)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getActivityLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog)

    // Faculty & Students
    @Query("SELECT * FROM faculty")
    fun getAllFaculty(): Flow<List<FacultyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaculty(faculty: List<FacultyEntity>)

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)
}
