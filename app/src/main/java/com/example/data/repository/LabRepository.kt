package com.example.data.repository

import com.example.data.local.LabDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LabRepository(private val labDao: LabDao) {

    val allLabs: Flow<List<Lab>> = labDao.getAllLabs()
    val allSystems: Flow<List<SystemEntity>> = labDao.getAllSystems()
    val allAttendance: Flow<List<AttendanceRecord>> = labDao.getAllAttendance()
    val allMaintenanceLogs: Flow<List<MaintenanceLog>> = labDao.getAllMaintenanceLogs()
    val allNotifications: Flow<List<NotificationEntity>> = labDao.getAllNotifications()
    val activityLogs: Flow<List<ActivityLog>> = labDao.getActivityLogs()
    val allFaculty: Flow<List<FacultyEntity>> = labDao.getAllFaculty()
    val allStudents: Flow<List<StudentEntity>> = labDao.getAllStudents()

    fun getSystemsForLab(labId: String): Flow<List<SystemEntity>> = labDao.getSystemsForLab(labId)

    suspend fun addAttendance(record: AttendanceRecord) = withContext(Dispatchers.IO) {
        labDao.insertAttendance(record)
        // Set corresponding system to OCCUPIED
        val system = labDao.getSystemById(record.systemId)
        if (system != null) {
            labDao.updateSystem(system.copy(
                status = "OCCUPIED",
                studentName = record.studentName,
                registerNumber = record.registerNumber,
                department = record.department,
                loginTime = record.loginTime,
                logoutTime = null,
                cpuUsage = Random.nextInt(10, 60),
                ramUsage = Random.nextInt(20, 70),
                networkStatus = "ONLINE"
            ))
        }
        // Log action
        labDao.insertActivityLog(ActivityLog(
            user = record.facultyName,
            message = "Attendance marked: ${record.studentName} checked into ${record.systemId}",
            category = "ATTENDANCE"
        ))
    }

    suspend fun checkoutStudent(systemId: String, facultyOrAdmin: String) = withContext(Dispatchers.IO) {
        val system = labDao.getSystemById(systemId)
        if (system != null && system.registerNumber != null) {
            val studentName = system.studentName ?: "Student"
            
            // Update attendance record with logout time
            val attendanceList = labDao.getAllAttendance().first()
            val activeRecord = attendanceList.firstOrNull { 
                it.systemId == systemId && it.registerNumber == system.registerNumber && it.logoutTime == null 
            }
            if (activeRecord != null) {
                labDao.insertAttendance(activeRecord.copy(logoutTime = System.currentTimeMillis()))
            }

            // Reset system
            labDao.updateSystem(system.copy(
                status = "AVAILABLE",
                studentName = null,
                registerNumber = null,
                department = null,
                loginTime = null,
                logoutTime = null,
                cpuUsage = 0,
                ramUsage = 0
            ))

            // Log action
            labDao.insertActivityLog(ActivityLog(
                user = facultyOrAdmin,
                message = "Released system $systemId. Student $studentName logged out.",
                category = "ALLOCATION"
            ))
        }
    }

    suspend fun allocateSystem(
        systemId: String,
        studentName: String,
        registerNo: String,
        dept: String,
        facultyName: String,
        hour: Int,
        labId: String
    ) = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val record = AttendanceRecord(
            studentName = studentName,
            registerNumber = registerNo,
            department = dept,
            labId = labId,
            systemId = systemId,
            loginTime = System.currentTimeMillis(),
            logoutTime = null,
            status = "PRESENT",
            hour = hour,
            facultyName = facultyName,
            date = today
        )
        addAttendance(record)
    }

    suspend fun updateSystemStatus(systemId: String, status: String, adminName: String) = withContext(Dispatchers.IO) {
        val system = labDao.getSystemById(systemId)
        if (system != null) {
            val updated = system.copy(
                status = status,
                // If maintenance, clear allocation
                studentName = if (status == "MAINTENANCE") null else system.studentName,
                registerNumber = if (status == "MAINTENANCE") null else system.registerNumber,
                department = if (status == "MAINTENANCE") null else system.department,
                loginTime = if (status == "MAINTENANCE") null else system.loginTime
            )
            labDao.updateSystem(updated)

            // Log action
            labDao.insertActivityLog(ActivityLog(
                user = adminName,
                message = "System $systemId status updated to $status",
                category = "SYSTEM"
            ))

            // If Maintenance, insert maintenance record
            if (status == "MAINTENANCE") {
                labDao.insertMaintenanceLog(MaintenanceLog(
                    systemId = systemId,
                    labId = system.labId,
                    issue = "Periodic Hardware/Software Checkup",
                    status = "PENDING"
                ))
                labDao.insertNotification(NotificationEntity(
                    title = "System Under Maintenance",
                    message = "System $systemId has been marked for maintenance checkups.",
                    type = "MAINTENANCE"
                ))
            }
        }
    }

    suspend fun updateSystemMetrics(systemId: String, cpu: Int, ram: Int, network: String) = withContext(Dispatchers.IO) {
        val system = labDao.getSystemById(systemId)
        if (system != null) {
            labDao.updateSystem(system.copy(
                cpuUsage = cpu,
                ramUsage = ram,
                networkStatus = network,
                lastUpdated = System.currentTimeMillis()
            ))
        }
    }

    suspend fun completeMaintenance(logId: Int, hardware: String?, software: String?, adminName: String) = withContext(Dispatchers.IO) {
        val logs = labDao.getAllMaintenanceLogs().first()
        val log = logs.find { it.id == logId }
        if (log != null) {
            labDao.updateMaintenanceLog(log.copy(
                status = "COMPLETED",
                completedDate = System.currentTimeMillis(),
                hardwareReplaced = hardware,
                softwareUpdated = software,
                antivirusStatus = "UP_TO_DATE"
            ))

            // Put system back to available
            val system = labDao.getSystemById(log.systemId)
            if (system != null && system.status == "MAINTENANCE") {
                labDao.updateSystem(system.copy(status = "AVAILABLE"))
            }

            labDao.insertActivityLog(ActivityLog(
                user = adminName,
                message = "Maintenance completed for system ${log.systemId}",
                category = "MAINTENANCE"
            ))
        }
    }

    suspend fun insertNotification(title: String, message: String, type: String) = withContext(Dispatchers.IO) {
        labDao.insertNotification(NotificationEntity(title = title, message = message, type = type))
    }

    suspend fun logActivity(user: String, message: String, category: String) = withContext(Dispatchers.IO) {
        labDao.insertActivityLog(ActivityLog(user = user, message = message, category = category))
    }

    // Pre-populates Swiss-army style professional labs and systems, students, and faculty.
    suspend fun checkAndPrepopulateAll() = withContext(Dispatchers.IO) {
        val currentLabs = labDao.getAllLabs().first()
        if (currentLabs.isNotEmpty()) return@withContext // Already pre-populated

        // Insert Labs
        val labsList = listOf(
            Lab("ML1", "Main Lab 1", 67, "CS Block - Ground Floor"),
            Lab("L2", "Lab 2", 67, "CS Block - First Floor"),
            Lab("AIL", "AI Lab", 42, "B-Block - Second Floor")
        )
        labDao.insertLabs(labsList)

        // Prepopulate Students
        val departments = listOf("B.Sc Computer Science", "B.Sc Information Technology", "M.Sc Computer Science", "MCA")
        val studentNames = listOf(
            "Aravind Swamy", "Bala Murali", "Chitra Devi", "Deepak Raj", "Elango K",
            "Gayathri S", "Hari Prasath", "Indhuja M", "Jegadeesh V", "Karthick R",
            "Lavanya P", "Manoj Kumar", "Nandhini G", "Pradeep S", "Ranjith K",
            "Sangeetha M", "Tharun R", "Vijay Krishnan", "Yoga Lakshmi", "Suresh A"
        )
        val students = studentNames.mapIndexed { idx, name ->
            val dept = departments[idx % departments.size]
            val regNo = "SDC" + (22000 + idx).toString()
            StudentEntity(id = regNo, name = name, department = dept, year = (idx % 3) + 1)
        }
        labDao.insertStudents(students)

        // Prepopulate Faculty
        val facultyNames = listOf("Dr. R. Srinivasan", "Mrs. S. Kamala", "Mr. M. Rajesh", "Dr. K. Anuradha")
        val facultyEntities = facultyNames.mapIndexed { idx, name ->
            val dept = departments[idx % departments.size]
            FacultyEntity(id = "FAC0${idx + 1}", name = name, department = dept, email = "${name.lowercase().replace(" ", "").replace(".", "")}@sdcollege.edu.in")
        }
        labDao.insertFaculty(facultyEntities)

        // Prepopulate Systems and populate some random occupied states, maintenance states
        val systemsToInsert = mutableListOf<SystemEntity>()
        val rand = Random(42) // Seeded for reproducibility

        for (lab in labsList) {
            for (i in 1..lab.totalSystems) {
                val sysId = "${lab.id}-${String.format("%02d", i)}"
                
                // Randomly set initial state
                var status = "AVAILABLE"
                var studName: String? = null
                var reg: String? = null
                var dept: String? = null
                var loginT: Long? = null
                var cpuU = 0
                var ramU = 0

                val draw = rand.nextDouble()
                if (draw < 0.25) { // 25% Occupied
                    val randomStudent = students[rand.nextInt(students.size)]
                    status = "OCCUPIED"
                    studName = randomStudent.name
                    reg = randomStudent.id
                    dept = randomStudent.department
                    loginT = System.currentTimeMillis() - rand.nextLong(15 * 60 * 1000, 3 * 60 * 60 * 1000)
                    cpuU = rand.nextInt(15, 80)
                    ramU = rand.nextInt(35, 85)
                } else if (draw < 0.30) { // 5% Maintenance
                    status = "MAINTENANCE"
                } else if (draw < 0.33) { // 3% Reserved
                    status = "RESERVED"
                }

                systemsToInsert.add(SystemEntity(
                    id = sysId,
                    systemNumber = i,
                    labId = lab.id,
                    status = status,
                    studentName = studName,
                    registerNumber = reg,
                    department = dept,
                    loginTime = loginT,
                    cpuUsage = cpuU,
                    ramUsage = ramU,
                    networkStatus = if (status == "MAINTENANCE" || rand.nextDouble() < 0.03) "OFFLINE" else "ONLINE",
                    maintenanceDue = i % 15 == 0
                ))
            }
        }
        labDao.insertSystems(systemsToInsert)

        // Prepopulate initial notifications
        val initialAlerts = listOf(
            NotificationEntity(title = "System ML1-12 Offline", message = "System ML1-12 in Main Lab 1 has stopped communicating with the server.", type = "OFFLINE"),
            NotificationEntity(title = "Maintenance Check Due", message = "AI Lab systems pending weekly diagnostic review.", type = "MAINTENANCE"),
            NotificationEntity(title = "Antivirus Subscription Expired", message = "Systems in Lab 2 Block A warning: antivirus dat list outdated.", type = "MAINTENANCE")
        )
        initialAlerts.forEach { labDao.insertNotification(it) }

        // Prepopulate initial Attendance logs
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val yesterday = dateFormat.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        val historicalAttendance = listOf(
            AttendanceRecord(studentName = "Aravind Swamy", registerNumber = "SDC22000", department = "B.Sc Computer Science", labId = "ML1", systemId = "ML1-01", loginTime = System.currentTimeMillis() - 4 * 60 * 60 * 1000, logoutTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000, status = "PRESENT", hour = 1, facultyName = "Dr. R. Srinivasan", date = yesterday),
            AttendanceRecord(studentName = "Bala Murali", registerNumber = "SDC22001", department = "B.Sc Information Technology", labId = "L2", systemId = "L2-04", loginTime = System.currentTimeMillis() - 3 * 60 * 60 * 1000, logoutTime = System.currentTimeMillis() - 1 * 60 * 60 * 1000, status = "PRESENT", hour = 2, facultyName = "Mrs. S. Kamala", date = yesterday),
            AttendanceRecord(studentName = "Chitra Devi", registerNumber = "SDC22002", department = "M.Sc Computer Science", labId = "AIL", systemId = "AIL-02", loginTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000, logoutTime = null, status = "PRESENT", hour = 3, facultyName = "Mr. M. Rajesh", date = today),
            AttendanceRecord(studentName = "Deepak Raj", registerNumber = "SDC22003", department = "MCA", labId = "ML1", systemId = "ML1-22", loginTime = System.currentTimeMillis() - 1 * 60 * 60 * 1000, logoutTime = null, status = "LATE", hour = 4, facultyName = "Dr. K. Anuradha", date = today)
        )
        historicalAttendance.forEach { labDao.insertAttendance(it) }

        // Activity Logs
        labDao.insertActivityLog(ActivityLog(user = "System", message = "Smart Lab platform initialized successfully", category = "SYSTEM"))
        labDao.insertActivityLog(ActivityLog(user = "Admin", message = "Populated Swami Dayananda College initial systems & catalogs", category = "SYSTEM"))
    }

    // Trigger simulation of changing CPU, RAM and offline states to simulate real-time socket connections
    suspend fun simulateLiveMetricsPulse() = withContext(Dispatchers.IO) {
        val current = labDao.getAllSystems().first()
        val occupiedSystems = current.filter { it.status == "OCCUPIED" }
        if (occupiedSystems.isEmpty()) return@withContext

        // Pick 3-5 random occupied systems to update metrics
        val systemsToUpdate = occupiedSystems.shuffled().take(Random.nextInt(2, 6))
        for (sys in systemsToUpdate) {
            val nextCpu = Random.nextInt(5, 95)
            val nextRam = Random.nextInt(30, 92)
            labDao.updateSystem(sys.copy(
                cpuUsage = nextCpu,
                ramUsage = nextRam,
                lastUpdated = System.currentTimeMillis()
            ))
        }

        // 1% chance for system going offline warning
        if (Random.nextDouble() < 0.04) {
            val randomSys = current.randomOrNull()
            if (randomSys != null && randomSys.networkStatus == "ONLINE") {
                labDao.updateSystem(randomSys.copy(networkStatus = "OFFLINE", lastUpdated = System.currentTimeMillis()))
                labDao.insertNotification(NotificationEntity(
                    title = "System ${randomSys.id} Offline Alert",
                    message = "System ${randomSys.id} in ${if (randomSys.labId == "ML1") "Main Lab 1" else if (randomSys.labId == "L2") "Lab 2" else "AI Lab"} is unresponsive.",
                    type = "OFFLINE"
                ))
            }
        }
    }
}
