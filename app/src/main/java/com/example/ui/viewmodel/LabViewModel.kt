package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiRetrofitClient
import com.example.data.local.LabDatabase
import com.example.data.model.*
import com.example.data.repository.LabRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class UserRole {
    ADMIN, FACULTY, LAB_ASSISTANT
}

enum class ActivePage {
    DASHBOARD, SYSTEMS_SEATS, ATTENDANCE, ANALYTICS, MAINTENANCE, REPORTS, AI_INSIGHTS, NOTIFICATIONS
}

class LabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LabRepository

    val allLabs: StateFlow<List<Lab>>
    val allSystems: StateFlow<List<SystemEntity>>
    val allAttendance: StateFlow<List<AttendanceRecord>>
    val allMaintenanceLogs: StateFlow<List<MaintenanceLog>>
    val allNotifications: StateFlow<List<NotificationEntity>>
    val activityLogs: StateFlow<List<ActivityLog>>
    val allFaculty: StateFlow<List<FacultyEntity>>
    val allStudents: StateFlow<List<StudentEntity>>

    // UI States
    private val _selectedRole = MutableStateFlow(UserRole.ADMIN)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    private val _selectedLabId = MutableStateFlow("ML1")
    val selectedLabId: StateFlow<String> = _selectedLabId.asStateFlow()

    private val _activePage = MutableStateFlow(ActivePage.DASHBOARD)
    val activePage: StateFlow<ActivePage> = _activePage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // System details selection state for interactive popups
    private val _selectedSystem = MutableStateFlow<SystemEntity?>(null)
    val selectedSystem: StateFlow<SystemEntity?> = _selectedSystem.asStateFlow()

    // Reports UI Filters state
    private val _reportLabFilter = MutableStateFlow("")
    val reportLabFilter = _reportLabFilter.asStateFlow()

    private val _reportDeptFilter = MutableStateFlow("")
    val reportDeptFilter = _reportDeptFilter.asStateFlow()

    private val _reportStudentFilter = MutableStateFlow("")
    val reportStudentFilter = _reportStudentFilter.asStateFlow()

    private val _reportSysFilter = MutableStateFlow("")
    val reportSysFilter = _reportSysFilter.asStateFlow()

    private val _generatedReportsList = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val generatedReportsList = _generatedReportsList.asStateFlow()

    // AI States
    private val _aiInsightsText = MutableStateFlow<String>("")
    val aiInsightsText = _aiInsightsText.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi = _isGeneratingAi.asStateFlow()

    // Theme Mode
    private val _isDarkMode = MutableStateFlow(true) // Start in cool dark academic theme by default
    val isDarkMode = _isDarkMode.asStateFlow()

    init {
        val database = LabDatabase.getDatabase(application)
        repository = LabRepository(database.labDao())

        allLabs = repository.allLabs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allSystems = repository.allSystems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allMaintenanceLogs = repository.allMaintenanceLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        activityLogs = repository.activityLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allFaculty = repository.allFaculty.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allStudents = repository.allStudents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Initialize and simulate live data
        viewModelScope.launch {
            repository.checkAndPrepopulateAll()
            startRealTimeSimulation()
        }
    }

    // Dynamic state simulations
    private fun startRealTimeSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(6000) // Pulse activity changes every 6s
                repository.simulateLiveMetricsPulse()
            }
        }
    }

    // Role, Lab, Navigation actions
    fun setRole(role: UserRole) {
        _selectedRole.value = role
        viewModelScope.launch {
            repository.logActivity("System", "User switched active session profile to $role", "SYSTEM")
        }
    }

    fun selectLab(labId: String) {
        _selectedLabId.value = labId
    }

    fun setActivePage(page: ActivePage) {
        _activePage.value = page
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectSystem(system: SystemEntity?) {
        _selectedSystem.value = system
    }

    // Report filters setters
    fun setReportLabFilter(lab: String) { _reportLabFilter.value = lab }
    fun setReportDeptFilter(dept: String) { _reportDeptFilter.value = dept }
    fun setReportStudentFilter(student: String) { _reportStudentFilter.value = student }
    fun setReportSysFilter(sys: String) { _reportSysFilter.value = sys }

    fun generateReport() {
        viewModelScope.launch {
            val records = allAttendance.value
            val filtered = records.filter { record ->
                (reportLabFilter.value.isEmpty() || record.labId.contains(reportLabFilter.value, true)) &&
                (reportDeptFilter.value.isEmpty() || record.department.contains(reportDeptFilter.value, true)) &&
                (reportStudentFilter.value.isEmpty() || record.studentName.contains(reportStudentFilter.value, true) || record.registerNumber.contains(reportStudentFilter.value, true)) &&
                (reportSysFilter.value.isEmpty() || record.systemId.contains(reportSysFilter.value, true))
            }
            _generatedReportsList.value = filtered
            repository.logActivity(
                user = selectedRole.value.name,
                message = "Generated detailed reports log. Count: ${filtered.size} records matching filters.",
                category = "NOTIFICATION"
            )
        }
    }

    // Database mutations
    fun allocateSystem(
        systemId: String,
        studentName: String,
        registerNo: String,
        dept: String,
        hour: Int
    ) {
        viewModelScope.launch {
            val fName = allFaculty.value.firstOrNull()?.name ?: "Lab Administrator"
            repository.allocateSystem(systemId, studentName, registerNo, dept, fName, hour, _selectedLabId.value)
            // Refresh detailed select system info if it is open
            val updated = allSystems.value.find { it.id == systemId }
            if (updated != null) {
                _selectedSystem.value = updated
            }
        }
    }

    fun releaseSystem(systemId: String) {
        viewModelScope.launch {
            repository.checkoutStudent(systemId, selectedRole.value.name)
            // Refresh select state
            _selectedSystem.value = null
        }
    }

    fun updateSystemStatus(systemId: String, status: String) {
        viewModelScope.launch {
            repository.updateSystemStatus(systemId, status, selectedRole.value.name)
            val updated = allSystems.value.find { it.id == systemId }
            if (updated != null) {
                // If maintenance status requested
                _selectedSystem.value = updated.copy(status = status)
            }
        }
    }

    fun powerOffSystem(systemId: String) {
        viewModelScope.launch {
            repository.updateSystemMetrics(systemId, 0, 0, "OFFLINE")
            repository.updateSystemStatus(systemId, "AVAILABLE", selectedRole.value.name)
            _selectedSystem.value = null
        }
    }

    fun completeMaintenance(logId: Int, hardware: String?, software: String?) {
        viewModelScope.launch {
            repository.completeMaintenance(logId, hardware, software, selectedRole.value.name)
        }
    }

    fun submitAlertManual(title: String, message: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(title, message, type)
            repository.logActivity(selectedRole.value.name, "Custom Alert broadcasted: $title", "SYSTEM")
        }
    }

    // Ask Gemini Swami AI Assistant
    fun generateAiInsights() {
        if (_isGeneratingAi.value) return
        _isGeneratingAi.value = true
        _aiInsightsText.value = "Analyzing systems load metrics, attendance registers, and maintenance schedules for Swami Dayananda College..."

        viewModelScope.launch {
            val systemsList = allSystems.value
            val totalSystems = systemsList.size
            val occupied = systemsList.count { it.status == "OCCUPIED" }
            val maintenance = systemsList.count { it.status == "MAINTENANCE" }
            val available = systemsList.count { it.status == "AVAILABLE" }
            val offline = systemsList.count { it.networkStatus == "OFFLINE" }

            val attendanceList = allAttendance.value
            val recentAttendees = attendanceList.take(5).joinToString(", ") { 
                "${it.studentName} (${it.department} on System ${it.systemId})" 
            }

            val maintenanceLogs = allMaintenanceLogs.value
            val pendingMaintenance = maintenanceLogs.count { it.status == "PENDING" }

            val prompt = """
                Generate a comprehensive and smart analytics briefing for the Computer Labs of Swami Dayananda College.
                
                Current Live Metrics:
                - Total Labs: 3 (Main Lab 1: 67 seats, Lab 2: 67 seats, AI Lab: 42 seats)
                - Total Configured PC Nodes: $totalSystems
                - Active Occupied Nodes: $occupied
                - Nodes Under Maintenance: $maintenance
                - Free Available Seats: $available
                - Systems Offline Warning: $offline
                - Pending Maintenance Tickets: $pendingMaintenance
                - Recent Attendee Log Ins: $recentAttendees
                
                Please evaluate and write detailed, professional analytical sections covering:
                1. Predict peak lab usage times based on current student count.
                2. Detect inactive systems (if any offline warnings or maintenance issues are elevated).
                3. Smart attendance analysis (conclusions about which department is utilizing the lab most).
                4. AI-powered resource allocation and energy-saving usage suggestions for Lab Staff.
                5. Auto Report summary explaining today's general laboratory productivity.
                
                Format the answer with clean Markdown display headers (e.g. ## Peak Lab Usage) and bullet points. Make it sound extremely helpful, academic, and professional.
            """.trimIndent()

            val response = GeminiRetrofitClient.getAIAnalysis(prompt)
            _aiInsightsText.value = response
            _isGeneratingAi.value = false
            repository.logActivity("AI-SwamiAssistant", "Executed automated real-time lab health reasoning report", "SYSTEM")
        }
    }
}
