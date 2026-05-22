package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labs")
data class Lab(
    @PrimaryKey val id: String, // ML1, L2, AIL
    val name: String,
    val totalSystems: Int,
    val location: String
)

@Entity(tableName = "systems")
data class SystemEntity(
    @PrimaryKey val id: String, // e.g. "ML1-01"
    val systemNumber: Int,
    val labId: String,          // ML1, L2, AIL
    val studentName: String? = null,
    val registerNumber: String? = null,
    val department: String? = null,
    val loginTime: Long? = null,
    val logoutTime: Long? = null,
    val status: String = "AVAILABLE", // AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
    val cpuUsage: Int = 0,
    val ramUsage: Int = 0,
    val networkStatus: String = "ONLINE", // ONLINE, OFFLINE
    val maintenanceDue: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val registerNumber: String,
    val department: String,
    val labId: String,
    val systemId: String,
    val loginTime: Long,
    val logoutTime: Long? = null,
    val status: String, // PRESENT, ABSENT, LATE
    val hour: Int,      // 1-5 hours
    val facultyName: String,
    val date: String    // YYYY-MM-DD
)

@Entity(tableName = "maintenance")
data class MaintenanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val systemId: String,
    val labId: String,
    val issue: String,
    val scheduledDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null,
    val status: String = "PENDING", // PENDING, COMPLETED
    val hardwareReplaced: String? = null,
    val softwareUpdated: String? = null,
    val antivirusStatus: String = "UP_TO_DATE" // UP_TO_DATE, EXPIRED
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // OFFLINE, MAINTENANCE, UNAUTHORIZED, OVERUSE, SHORTAGE
    val isRead: Boolean = false
)

@Entity(tableName = "faculty")
data class FacultyEntity(
    @PrimaryKey val id: String, // e.g. FAC01
    val name: String,
    val department: String,
    val email: String,
    val initialAssignedLab: String? = null
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String, // Register No, e.g. "SDC2026001"
    val name: String,
    val department: String,
    val year: Int
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String // SYSTEM, ATTENDANCE, MAINTENANCE, ALLOCATION
)
