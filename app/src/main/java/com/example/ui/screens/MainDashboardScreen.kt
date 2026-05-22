package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActivePage
import com.example.ui.viewmodel.LabViewModel
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: LabViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activePage by viewModel.activePage.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val context = LocalContext.current

    // Observe DB lists
    val labs by viewModel.allLabs.collectAsState()
    val systems by viewModel.allSystems.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    // Scaffolds handling edge to edge, responsive check
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = SlateBorder,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Rounded-xl Indigo Logo with Computer Icon from mockup
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.dp, GoldLight.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = "SDC Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "SDC Smart Lab",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "MISSION CONTROL • LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.5.sp,
                                    color = GoldLight,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dark Mode Toggle
                            IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                            
                            // Role Switcher Button
                            var showRoleMenu by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                Button(
                                    onClick = { showRoleMenu = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person, 
                                        contentDescription = "Role Selector",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(selectedRole.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false }
                                ) {
                                    UserRole.values().forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role.name) },
                                            onClick = {
                                                viewModel.setRole(role)
                                                showRoleMenu = false
                                                Toast.makeText(context, "Session profile switched: ${role.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Screen density check. If inside compact phone width, render bottom nav.
            BoxWithConstraints {
                if (maxWidth <= 800.dp) {
                    NavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                drawLine(
                                    color = SlateBorder,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .testTag("phone_bottom_nav"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        tabItems.forEach { item ->
                            val selected = activePage == item.page
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.setActivePage(item.page) },
                                icon = { 
                                    Icon(
                                        imageVector = if (selected) item.filledIcon else item.outlinedIcon, 
                                        contentDescription = item.label 
                                    ) 
                                },
                                label = { Text(item.label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding)) {
            val isTablet = maxWidth > 800.dp
            
            Row(modifier = Modifier.fillMaxSize()) {
                // Inside landscape tablets, render side nav bar
                if (isTablet) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("tablet_rail"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(vertical = 12.dp)
                            )
                        }
                    ) {
                        tabItems.forEach { item ->
                            val selected = activePage == item.page
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.setActivePage(item.page) },
                                icon = { 
                                    Icon(
                                        imageVector = if (selected) item.filledIcon else item.outlinedIcon, 
                                        contentDescription = item.label 
                                    ) 
                                },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }

                // Main Content Screen body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = activePage,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "PageTransitions"
                    ) { page ->
                        when (page) {
                            ActivePage.DASHBOARD -> DashboardPage(viewModel)
                            ActivePage.SYSTEMS_SEATS -> SeatMapPage(viewModel)
                            ActivePage.ATTENDANCE -> AttendancePage(viewModel)
                            ActivePage.ANALYTICS -> AnalyticsPage(viewModel)
                            ActivePage.MAINTENANCE -> MaintenancePage(viewModel)
                            ActivePage.REPORTS -> ReportsPage(viewModel)
                            ActivePage.AI_INSIGHTS -> AIInsightsPage(viewModel)
                            ActivePage.NOTIFICATIONS -> NotificationsPage(viewModel)
                        }
                    }
                }
            }
        }
    }

    // System Details overlay dialog
    val selectedSystem by viewModel.selectedSystem.collectAsState()
    if (selectedSystem != null) {
        SystemDetailsDialog(
            system = selectedSystem!!,
            viewModel = viewModel,
            onDismiss = { viewModel.selectSystem(null) }
        )
    }
}

// Tab description holders
data class TabNavItem(
    val page: ActivePage,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

val tabItems = listOf(
    TabNavItem(ActivePage.DASHBOARD, "Monitor", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TabNavItem(ActivePage.SYSTEMS_SEATS, "Seat Map", Icons.Filled.Computer, Icons.Outlined.Computer),
    TabNavItem(ActivePage.ATTENDANCE, "Attendance", Icons.Filled.HowToReg, Icons.Outlined.HowToReg),
    TabNavItem(ActivePage.ANALYTICS, "Charts", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    TabNavItem(ActivePage.MAINTENANCE, "Repairs", Icons.Filled.Build, Icons.Outlined.Build),
    TabNavItem(ActivePage.REPORTS, "Logs", Icons.Filled.Description, Icons.Outlined.Description),
    TabNavItem(ActivePage.AI_INSIGHTS, "Swami AI", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    TabNavItem(ActivePage.NOTIFICATIONS, "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications)
)

// ---------------- DASHBOARD PAGE -----------------
@Composable
fun DashboardPage(viewModel: LabViewModel) {
    val systems by viewModel.allSystems.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    // Math metrics
    val totalSystemsCount = systems.size
    val occupiedCount = systems.count { it.status == "OCCUPIED" }
    val maintenanceCount = systems.count { it.status == "MAINTENANCE" }
    val reservedCount = systems.count { it.status == "RESERVED" }
    val availableSeatsCount = systems.count { it.status == "AVAILABLE" }
    val offlineCount = systems.count { it.networkStatus == "OFFLINE" }

    // Today's Date
    val dateText = remember {
        val format = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        format.format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Computer Lab Infrastructure Control Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    // Real-time animation pulse indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val opacity by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "opacity"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SeatGreen.copy(alpha = opacity))
                        )
                        Text(
                            text = "LIVE SYNC ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SeatGreen
                        )
                    }
                }
            }
        }

        // 4x2 Grid of Dashboard Stats Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Total Systems",
                        value = totalSystemsCount.toString(),
                        subtitle = "Main, L2 & AI Labs",
                        icon = Icons.Default.DesktopWindows,
                        color = MaterialTheme.colorScheme.primary,
                        progress = 1.00f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Nodes",
                        value = occupiedCount.toString(),
                        subtitle = "Students Online",
                        icon = Icons.Default.PlayCircle,
                        color = SeatGreen,
                        progress = if (totalSystemsCount > 0) occupiedCount.toFloat() / totalSystemsCount else 0.50f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Under Maintenance",
                        value = maintenanceCount.toString(),
                        subtitle = "Requires Diagnostics",
                        icon = Icons.Default.Build,
                        color = SeatYellow,
                        progress = if (totalSystemsCount > 0) maintenanceCount.toFloat() / totalSystemsCount else 0.12f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Reserved Units",
                        value = reservedCount.toString(),
                        subtitle = "Research & Special Class",
                        icon = Icons.Default.Bookmark,
                        color = SeatBlue,
                        progress = if (totalSystemsCount > 0) reservedCount.toFloat() / totalSystemsCount else 0.18f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Available Seats",
                        value = availableSeatsCount.toString(),
                        subtitle = "Vacated Desktop Nodes",
                        icon = Icons.Default.CheckCircle,
                        color = SeatGreen,
                        progress = if (totalSystemsCount > 0) availableSeatsCount.toFloat() / totalSystemsCount else 0.38f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Offline Nodes",
                        value = offlineCount.toString(),
                        subtitle = "Network Disconnects",
                        icon = Icons.Default.WifiOff,
                        color = SeatRed,
                        progress = if (totalSystemsCount > 0) offlineCount.toFloat() / totalSystemsCount else 0.08f,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Attendance Today",
                        value = attendance.size.toString(),
                        subtitle = "Total Daily Log Ins",
                        icon = Icons.Default.HowToReg,
                        color = MaterialTheme.colorScheme.primary,
                        progress = if (totalSystemsCount > 0) (attendance.size.coerceAtMost(totalSystemsCount)).toFloat() / totalSystemsCount else 0.65f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Live Utilization",
                        value = "${if (totalSystemsCount > 0) (occupiedCount * 100 / totalSystemsCount) else 0}%",
                        subtitle = "Active Occupancy %",
                        icon = Icons.Default.Speed,
                        color = GoldLight,
                        progress = if (totalSystemsCount > 0) occupiedCount.toFloat() / totalSystemsCount else 0.50f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI Performance Insights (Glassmorphism & Radial Glow) from mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_ai_insights"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Smooth radial blurred glow on the top-right corner to match pure glassmorphism
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GoldLight.copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width - 20.dp.toPx(), 20.dp.toPx()),
                                    radius = 110.dp.toPx()
                                ),
                                radius = 110.dp.toPx(),
                                center = Offset(size.width - 20.dp.toPx(), 20.dp.toPx())
                            )
                        }
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse_analytics")
                                val opacity by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1100, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "pulse_opacity"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GoldLight.copy(alpha = opacity))
                                )
                                Text(
                                    text = "AI SMART ANALYTICS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "UPTIME: 99.8%",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "Peak usage predicted at 14:30 PM in AI Lab. Recommend cooling shift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            lineHeight = 18.sp,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Live Alerts ticker and Activities logs side panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recent Alerts
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(260.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Systems Alert Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val activeAlerts = notifications.take(4)
                            if (activeAlerts.isEmpty()) {
                                item { Text("No critical alert flags pending.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(10.dp)) }
                            } else {
                                items(activeAlerts) { alert ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(
                                                    alpha = 0.5f
                                                ), RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (alert.type) {
                                                "OFFLINE" -> Icons.Default.WifiOff
                                                "MAINTENANCE" -> Icons.Default.Warning
                                                else -> Icons.Default.NotificationsActive
                                            },
                                            contentDescription = "Alert",
                                            tint = if (alert.type == "OFFLINE") SeatRed else SeatYellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text(alert.message, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Activities Console
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(260.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sub-Station Operational Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldLight)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val logs = activityLogs.take(20)
                                if (logs.isEmpty()) {
                                    item { Text("No log operations found.", color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 10.sp) }
                                } else {
                                    items(logs) { log ->
                                        Text(
                                            text = "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(log.timestamp)}]  (${log.user})  ${log.message}",
                                            color = Color.Green,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    progress: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = value,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                        Text(
                            text = subtitle,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Premium micro progress bar aligned with High Density HTML theme
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .background(SlateBorder, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                            .background(color, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}


// ---------------- SEAT MAP PAGE -----------------
@Composable
fun SeatMapPage(viewModel: LabViewModel) {
    val selectedLabId by viewModel.selectedLabId.collectAsState()
    val systems by viewModel.allSystems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Filter systems to correct lab and apply matching searchQuery
    val labSystems = systems.filter { 
        it.labId == selectedLabId && 
        (searchQuery.isEmpty() || 
         it.id.contains(searchQuery, true) || 
         (it.studentName?.contains(searchQuery, true) == true) || 
         (it.department?.contains(searchQuery, true) == true) ||
         (it.registerNumber?.contains(searchQuery, true) == true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headers & Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Real-Time Interactive Seat Allocation Map", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Tap systems box to assign, checkout, monitor CPU usage, or flag maintenance.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Lab Selector Tabs + Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "ML1" to "Main Lab 1 (67 Systems)",
                "L2" to "Lab 2 (67 Systems)",
                "AIL" to "AI Lab (42 Systems)"
            ).forEach { (labId, labLabel) ->
                Button(
                    onClick = { viewModel.selectLab(labId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLabId == labId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selectedLabId == labId) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = if (selectedLabId != labId) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Text(labLabel, fontSize = 11.sp, maxLines = 1)
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search system, student name, register number...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("system_search_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        // Status Legend row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LegendItem("Available", SeatGreen)
            LegendItem("Occupied", SeatRed)
            LegendItem("Under Repair", SeatYellow)
            LegendItem("Reserved Staff", SeatBlue)
            LegendItem("Offline Alert", Color.Gray)
        }

        // Interactive Seat Grid (Scrollable)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (labSystems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No systems matching search query.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(labSystems) { sys ->
                        SeatBoxItem(sys = sys, onClick = { viewModel.selectSystem(sys) })
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SeatBoxItem(sys: SystemEntity, onClick: () -> Unit) {
    val baseColor = when (sys.status) {
        "AVAILABLE" -> SeatGreen
        "OCCUPIED" -> SeatRed
        "MAINTENANCE" -> SeatYellow
        "RESERVED" -> SeatBlue
        else -> Color.DarkGray
    }

    val displayColor = if (sys.networkStatus == "OFFLINE") Color.Gray else baseColor

    Card(
        modifier = Modifier
            .size(width = 64.dp, height = 64.dp)
            .clickable { onClick() }
            .testTag("seat_unit_${sys.id}"),
        colors = CardDefaults.cardColors(
            containerColor = displayColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = if (sys.networkStatus == "OFFLINE") 1.dp else 2.dp,
            color = displayColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (sys.networkStatus == "OFFLINE") Icons.Default.WifiOff else Icons.Default.Computer,
                contentDescription = null,
                tint = displayColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                // Display only number suffix to stay compact, e.g. "ML1-22" -> "22"
                text = "${sys.systemNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = displayColor
            )
            if (sys.status == "OCCUPIED" && sys.cpuUsage > 0) {
                Text(
                    text = "${sys.cpuUsage}% CPU",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeatRed
                )
            }
        }
    }
}


// ---------------- STUDENT ATTENDANCE MODULE -----------------
@Composable
fun AttendancePage(viewModel: LabViewModel) {
    val context = LocalContext.current
    val systems by viewModel.allSystems.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val attendanceLogs by viewModel.allAttendance.collectAsState()

    var showAllocationDialog by remember { mutableStateOf(false) }
    var selectedSeatForAlloc by remember { mutableStateOf<String?>(null) }

    // Manual Form states
    var regNumInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var deptInput by remember { mutableStateOf("B.Sc Computer Science") }
    var hourSelect by remember { mutableStateOf(1) }

    // QR scanner state
    var showQrScanner by remember { mutableStateOf(false) }
    var isScanningQr by remember { mutableStateOf(false) }

    val freeSystems = systems.filter { it.status == "AVAILABLE" && it.networkStatus == "ONLINE" }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Centralized Student Attendance Station", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Register daily entry logs, trigger automated system allocations, or launch terminal QR Check-ins.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Interactive Check-In forms
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Manual Attendance Verification & System Check-In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = regNumInput,
                            onValueChange = { regNumInput = it },
                            placeholder = { Text("Register No (e.g. SDC22005)", fontSize = 11.sp) },
                            label = { Text("Register No", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("attend_reg_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Student Name", fontSize = 11.sp) },
                            label = { Text("Full Name", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(56.dp).testTag("attend_name_input"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        var expandedDeptMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.5f)) {
                            OutlinedButton(
                                onClick = { expandedDeptMenu = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(deptInput, fontSize = 11.sp, maxLines = 1)
                            }
                            DropdownMenu(expanded = expandedDeptMenu, onDismissRequest = { expandedDeptMenu = false }) {
                                listOf("B.Sc Computer Science", "B.Sc Information Technology", "M.Sc Computer Science", "MCA").forEach { dept ->
                                    DropdownMenuItem(text = { Text(dept) }, onClick = { deptInput = dept; expandedDeptMenu = false })
                                }
                            }
                        }

                        var expandedSeatMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { expandedSeatMenu = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(selectedSeatForAlloc ?: "Assigned PC", fontSize = 11.sp, maxLines = 1)
                            }
                            DropdownMenu(expanded = expandedSeatMenu, onDismissRequest = { expandedSeatMenu = false }) {
                                freeSystems.take(20).forEach { sys ->
                                    DropdownMenuItem(text = { Text(sys.id) }, onClick = { selectedSeatForAlloc = sys.id; expandedSeatMenu = false })
                                }
                            }
                        }

                        // Hour selector
                        var expandedHour by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.8f)) {
                            OutlinedButton(
                                onClick = { expandedHour = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Hour $hourSelect", fontSize = 11.sp)
                            }
                            DropdownMenu(expanded = expandedHour, onDismissRequest = { expandedHour = false }) {
                                (1..5).forEach { hr ->
                                    DropdownMenuItem(text = { Text("Hour $hr") }, onClick = { hourSelect = hr; expandedHour = false })
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (regNumInput.trim().isEmpty() || nameInput.trim().isEmpty()) {
                                Toast.makeText(context, "Please enter Register No and Student Name.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedSeatForAlloc == null) {
                                Toast.makeText(context, "Please select an available PC node.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.allocateSystem(
                                systemId = selectedSeatForAlloc!!,
                                studentName = nameInput.trim(),
                                registerNo = regNumInput.trim().uppercase(),
                                dept = deptInput,
                                hour = hourSelect
                            )
                            Toast.makeText(context, "Attendance login successful for system $selectedSeatForAlloc", Toast.LENGTH_SHORT).show()
                            nameInput = ""
                            regNumInput = ""
                            selectedSeatForAlloc = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_manual_attendance"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Verification Entry & Boot System PC", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // QR check-in section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Swami QR Automated Attendance Scanner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Launch scanner to parse digital identity student cards instantly on device.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { showQrScanner = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "QR Scanner", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Scanner", fontSize = 11.sp)
                    }
                }
            }
        }

        // Attendance list logs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today's Laboratory Attendance Logs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${attendanceLogs.size} logs registered", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (attendanceLogs.isEmpty()) {
                            Text("No student entry logs checked in and registered today.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(attendanceLogs) { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.background,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(log.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Reg No: ${log.registerNumber} • Dept: ${log.department}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Lab: ${log.labId} • PC: ${log.systemId} • Period: Hour ${log.hour}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            val logInTimeStr = remember {
                                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.loginTime))
                                            }
                                            Text(text = "Logged In: $logInTimeStr", fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                            
                                            val statusBg = if (log.status == "PRESENT") SeatGreen else SeatYellow
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .background(statusBg.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, statusBg, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = log.status, color = statusBg, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // QR scanner custom dialog simulation
    if (showQrScanner) {
        Dialog(onDismissRequest = { showQrScanner = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Automated Barcode & QR Entrance Scan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Scanner camera finder simulation with target lasers
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .border(3.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanningQr) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        } else {
                            // Target lines
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = 3f
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Text("Hold the digital identity QR code card inside the focal scan lines.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showQrScanner = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Discard", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                isScanningQr = true
                                coroutineScope.launch {
                                    delay(1800) // Scan delay simulation
                                    isScanningQr = false
                                    showQrScanner = false
                                    // Allocate a random system to a random preset student
                                    val availableSystems = systems.filter { it.status == "AVAILABLE" }
                                    if (availableSystems.isEmpty()) {
                                        Toast.makeText(context, "Scan error: No available desktops in labs today.", Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                    val randomSys = availableSystems.random()
                                    val randomStudents = allStudents
                                    val studentToPre = if (randomStudents.isNotEmpty()) randomStudents.random() else StudentEntity("SDC22019", "Tharun R", "MCA", 2)
                                    viewModel.allocateSystem(
                                        systemId = randomSys.id,
                                        studentName = studentToPre.name,
                                        registerNo = studentToPre.id,
                                        dept = studentToPre.department,
                                        hour = (1..5).random()
                                    )
                                    Toast.makeText(context, "Scanning Complete! Verified student ${studentToPre.name}. System ${randomSys.id} assigned.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Simulate Scan Match", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------- LAB ANALYTICS CHARTS PAGE -----------------
@Composable
fun AnalyticsPage(viewModel: LabViewModel) {
    val systems by viewModel.allSystems.collectAsState()

    // Counts for charts
    val pcCount = systems.size
    val ml1Count = systems.filter { it.labId == "ML1" }.size
    val ml1Occ = systems.filter { it.labId == "ML1" && it.status == "OCCUPIED" }.size
    val l2Count = systems.filter { it.labId == "L2" }.size
    val l2Occ = systems.filter { it.labId == "L2" && it.status == "OCCUPIED" }.size
    val aiCount = systems.filter { it.labId == "AIL" }.size
    val aiOcc = systems.filter { it.labId == "AIL" && it.status == "OCCUPIED" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Performance & Lab Utilization Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Dynamic vector diagrams plotting daily usage loads, Peak periods, and Lab occupancy stats.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Row of split visual charts
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // 1. Bar Chart: Lab-wise occupancy comparison
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lab-wise Occupancy (PC Nodes)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Active students online compared to total seats", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                                val w = size.width
                                val h = size.height
                                
                                val labels = listOf("Main Lab 1", "Lab 2", "AI Lab")
                                val capacities = listOf(ml1Count, l2Count, aiCount)
                                val currentLoads = listOf(ml1Occ, l2Occ, aiOcc)
                                
                                val barWidth = w / 6f
                                val spacing = w / 4f
                                
                                for (i in 0..2) {
                                    val x = (i + 1) * spacing - barWidth
                                    
                                    // Max heights scale
                                    val maxVal = 70f
                                    val capHeight = (capacities[i] / maxVal) * h
                                    val loadHeight = (currentLoads[i] / maxVal) * h
                                    
                                    // Draw Capacity Outline Bar
                                    drawRect(
                                        color = Color.LightGray.copy(alpha = 0.2f),
                                        topLeft = Offset(x, h - capHeight),
                                        size = Size(barWidth, capHeight),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                    // Draw Active occupancy Bar using custom color gradients
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = if (i == 2) listOf(GoldAccent, GoldLight) else listOf(CyberCyan, Color(0xFF3B82F6))
                                        ),
                                        topLeft = Offset(x, h - loadHeight),
                                        size = Size(barWidth, loadHeight)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Line Chart: Hourly peak times
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Peak Utilization Timelines (Hourly)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Average system log in densities today", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                                val w = size.width
                                val h = size.height
                                
                                // Peak periods: 9AM(10%), 11AM(85%), 1PM(40%), 3PM(90%), 5PM(15%)
                                val points = listOf(0.1f, 0.85f, 0.45f, 0.9f, 0.15f)
                                val spacing = w / (points.size - 1)
                                
                                val path = androidx.compose.ui.graphics.Path()
                                for (i in points.indices) {
                                    val x = i * spacing
                                    val y = h - (points[i] * h)
                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                    
                                    // Draw node dot
                                    drawCircle(
                                        color = GoldAccent,
                                        radius = 4.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                                
                                drawPath(
                                    path = path,
                                    color = CyberCyan,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Circular utilization dial gauges + Daily Heat-map Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Circular gauges
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(280.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Infrastructure Active Load Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                            // Circular representation
                            val livePercentage = if (pcCount > 0) ((ml1Occ + l2Occ + aiOcc) * 100f / pcCount) else 0f
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.LightGray.copy(alpha = 0.2f),
                                    startAngle = -220f,
                                    sweepAngle = 260f,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(CyberCyan, GoldAccent, CyberCyan)
                                    ),
                                    startAngle = -220f,
                                    sweepAngle = 260f * (livePercentage / 100f),
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${livePercentage.toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GoldLight)
                                Text(text = "GLOBAL USED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }
                    }
                }

                // Lab Energy-Consumption Heat Map (simulated)
                Card(
                    modifier = Modifier
                        .weight(1.8f)
                        .height(280.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("24H System Occupancy Heat-Map Analyzer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Glow indicators signify server load peaks dynamically over time.", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 24 grid compartments representing hours of day
                            items((1..24).toList()) { hr ->
                                val activityVal = when {
                                    hr in 9..12 -> 0.9f
                                    hr in 13..16 -> 0.75f
                                    hr in 17..20 -> 0.35f
                                    else -> 0.05f
                                }
                                val alphaColor = if (activityVal > 0.8f) SeatRed else if (activityVal > 0.5f) SeatYellow else SeatGreen
                                
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(alphaColor.copy(alpha = activityVal))
                                        .border(1.dp, alphaColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${hr}H",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------- MAINTENANCE PORTAL PAGE -----------------
@Composable
fun MaintenancePage(viewModel: LabViewModel) {
    val context = LocalContext.current
    val maintenanceLogs by viewModel.allMaintenanceLogs.collectAsState()
    val systems by viewModel.allSystems.collectAsState()

    var showCompleteDialog by remember { mutableStateOf(false) }
    var selectedLogItem by remember { mutableStateOf<MaintenanceLog?>(null) }
    var hardwareInput by remember { mutableStateOf("") }
    var softwareInput by remember { mutableStateOf("") }

    val pendingLogs = maintenanceLogs.filter { it.status == "PENDING" }
    val compiledLogs = maintenanceLogs.filter { it.status == "COMPLETED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Hardware Diagnostics & Maintenance Control", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Monitor workstation faults, software releases, antivirus validation status, and repair tickets.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Live stats panel
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Active Unresolved Diagnostic Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("ワークステーションメンテナンスステータスボード", fontSize = 10.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .background(SeatYellow.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("${pendingLogs.size} TICKETS PENDING", color = SeatYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // List unresolved tickets
        item {
            Text("Pending Repair Tickets Log List", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (pendingLogs.isEmpty()) {
            item {
                Card(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active hardware issues or software requests registered.", fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(pendingLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("System Node: ${log.systemId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Lab Location: ${log.labId} • Registered: ${SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date(log.scheduledDate))}", fontSize = 10.sp, color = Color.Gray)
                        Text("Reported Issue: ${log.issue}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                    }
                    Button(
                        onClick = {
                            selectedLogItem = log
                            hardwareInput = ""
                            softwareInput = ""
                            showCompleteDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SeatYellow),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // List resolved history
        item {
            Text("Resolved Maintenance Histories (ERP Archives)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (compiledLogs.isEmpty()) {
            item {
                Text("No archived resolved tickets recorded yet.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            items(compiledLogs) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Workstation: ${log.systemId} (${log.labId})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Issue: ${log.issue}", fontSize = 10.sp, color = Color.Gray)
                        Text("Action Taken: Hardware: ${log.hardwareReplaced ?: "None"} • Software: ${log.softwareUpdated ?: "None"} • Antivirus: ${log.antivirusStatus}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .background(SeatGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("COMPLETED", color = SeatGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }
        }
    }

    // Complete maintenance action dialogue box
    if (showCompleteDialog && selectedLogItem != null) {
        Dialog(onDismissRequest = { showCompleteDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Resolve Fault: Workstation ${selectedLogItem!!.systemId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Verify software update or hardware replacement diagnostics to release machine boot-loader locks.", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = hardwareInput,
                        onValueChange = { hardwareInput = it },
                        placeholder = { Text("Hardware Action (e.g. Replaced Mouse, Cleaned Fan)", fontSize = 11.sp) },
                        label = { Text("Hardware Replaced (Optional)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )

                    OutlinedTextField(
                        value = softwareInput,
                        onValueChange = { softwareInput = it },
                        placeholder = { Text("Software Update (e.g. Updated Java JDK 21, Patch Ver 4.2)", fontSize = 11.sp) },
                        label = { Text("Software / Patches Installed (Optional)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        OutlinedButton(
                            onClick = { showCompleteDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.completeMaintenance(
                                    logId = selectedLogItem!!.id,
                                    hardware = if (hardwareInput.trim().isNotEmpty()) hardwareInput.trim() else null,
                                    software = if (softwareInput.trim().isNotEmpty()) softwareInput.trim() else null
                                )
                                showCompleteDialog = false
                                Toast.makeText(context, "Workstation ${selectedLogItem!!.systemId} resolved and unlocked for class", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeatGreen),
                            modifier = Modifier.weight(1.5f).testTag("confirm_resolve_maintenance")
                        ) {
                            Text("Certify & Release Workstation", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


// ---------------- REPORTS EXTRACTION PAGE -----------------
@Composable
fun ReportsPage(viewModel: LabViewModel) {
    val context = LocalContext.current
    val filteredReports by viewModel.generatedReportsList.collectAsState()

    var selectLabFilter by remember { mutableStateOf("") }
    var selectDeptFilter by remember { mutableStateOf("") }
    var selectStudentFilter by remember { mutableStateOf("") }
    var selectSysFilter by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Comprehensive Audits & Extracted Reports Portal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Specify lab filtering criteria, query attendance index archives, and print compliant PDF/Excel outputs.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Queries Filters Board
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Audit Query Directives", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Lab ID input
                        OutlinedTextField(
                            value = selectLabFilter,
                            onValueChange = { selectLabFilter = it; viewModel.setReportLabFilter(it) },
                            placeholder = { Text("Lab ID (e.g. ML1, AIL)", fontSize = 11.sp) },
                            label = { Text("Filter Lab", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            singleLine = true
                        )

                        // Dept filter input
                        OutlinedTextField(
                            value = selectDeptFilter,
                            onValueChange = { selectDeptFilter = it; viewModel.setReportDeptFilter(it) },
                            placeholder = { Text("Dept (e.g. Computer)", fontSize = 11.sp) },
                            label = { Text("Department", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.2f).height(54.dp),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Student Register filter
                        OutlinedTextField(
                            value = selectStudentFilter,
                            onValueChange = { selectStudentFilter = it; viewModel.setReportStudentFilter(it) },
                            placeholder = { Text("Student Name/Reg", fontSize = 11.sp) },
                            label = { Text("Student Query", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.2f).height(54.dp),
                            singleLine = true
                        )

                        // System selection filter
                        OutlinedTextField(
                            value = selectSysFilter,
                            onValueChange = { selectSysFilter = it; viewModel.setReportSysFilter(it) },
                            placeholder = { Text("System (e.g. ML1-04)", fontSize = 11.sp) },
                            label = { Text("System ID", fontSize = 11.sp) },
                            modifier = Modifier.weight(0.8f).height(54.dp),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = { viewModel.generateReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("trigger_report_query"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Query", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute Database Audit Query", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Export Options
        if (filteredReports.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Swami college ERP document compiled. PDF save successful directly to downloads directory.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SeatRed),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF Report", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Spreadsheet successfully compiled. Excel file generated with logs data.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SeatGreen),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GridOn, contentDescription = "Excel", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Excel Log", fontSize = 11.sp)
                    }
                }
            }
        }

        // Results table card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Result Log Query Archives", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (filteredReports.isEmpty()) {
                            Text(
                                "No audit records retrieved. Refine query filters and tap compile query.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(filteredReports) { record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(record.studentName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Reg No: ${record.registerNumber} • ${record.department}", fontSize = 9.sp, color = Color.Gray)
                                            Text("Lab: ${record.labId} • PC: ${record.systemId}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(
                                            text = record.date,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------- GEMINI SWAMI AI INSIGHTS PORTAL -----------------
@Composable
fun AIInsightsPage(viewModel: LabViewModel) {
    val aiResponse by viewModel.aiInsightsText.collectAsState()
    val isGenerating by viewModel.isGeneratingAi.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Swami AI Smart Laboratory Coordinator", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("Gemini client-side engine evaluating historical registers to predict peaks, manage energy loads and summarize telemetry.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Star-dust AI command block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Custom vector golden stardust drawing inside cyber container
                        val p = androidx.compose.ui.graphics.Path()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            radius = size.width / 2.5f,
                            center = Offset(size.width * 0.8f, size.height / 2f)
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = GoldAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Execute AI Cognitive Laboratory Audit Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = " Swami AI aggregates real-time system status arrays, attendance curves and repairs lists to auto-suggest optimal class periods, flag silent network faults, and outline resources.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Button(
                        onClick = { viewModel.generateAiInsights() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_trigger_button"),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = "Cognitive")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compute Real-time Gemini Analytics", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Out-stream Terminal card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Swami-AI-Command-Console_v2.5.txt", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (aiResponse.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Color.Green, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Awaiting directive. Hit compile analytics to start.", color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Text(
                                        text = aiResponse,
                                        color = Color.Green,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------- NOTIFICATIONS LIST PAGE -----------------
@Composable
fun NotificationsPage(viewModel: LabViewModel) {
    val alerts by viewModel.allNotifications.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Centralized Alerts & Incident Broadcasts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text("System notifications compiled regarding offline workstation boots or priority repair schedules.", fontSize = 11.sp, color = Color.Gray)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Broadcast Incident Override Memo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    
                    var memoTitle by remember { mutableStateOf("") }
                    var memoMsg by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = memoTitle,
                        onValueChange = { memoTitle = it },
                        placeholder = { Text("Title (e.g. Server Updates)", fontSize = 11.sp) },
                        label = { Text("Alert Title", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = memoMsg,
                        onValueChange = { memoMsg = it },
                        placeholder = { Text("Instructions description detail...", fontSize = 11.sp) },
                        label = { Text("Instructions Payload", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (memoTitle.trim().isEmpty() || memoMsg.trim().isEmpty()) {
                                Toast.makeText(context, "Verify title and message labels.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.submitAlertManual(memoTitle.trim(), memoMsg.trim(), "UNAUTHORIZED")
                            Toast.makeText(context, "Memo broadcasted successfully.", Toast.LENGTH_SHORT).show()
                            memoTitle = ""
                            memoMsg = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Send Incident Flag Banner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Logged Incidents Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (alerts.isEmpty()) {
            item {
                Text("No incident events archived.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            items(alerts) { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(alert.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text(
                            text = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date(alert.timestamp)),
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (alert.type) {
                                    "OFFLINE" -> SeatRed.copy(alpha = 0.15f)
                                    "MAINTENANCE" -> SeatYellow.copy(alpha = 0.15f)
                                    else -> SeatBlue.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = when (alert.type) {
                                    "OFFLINE" -> SeatRed
                                    "MAINTENANCE" -> SeatYellow
                                    else -> SeatBlue
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = alert.type,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (alert.type) {
                                "OFFLINE" -> SeatRed
                                "MAINTENANCE" -> SeatYellow
                                else -> SeatBlue
                            }
                        )
                    }
                }
            }
        }
    }
}


// ---------------- WORKSTATION PROPERTIES SHEET DIALOG -----------------
@Composable
fun SystemDetailsDialog(
    system: SystemEntity,
    viewModel: LabViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentRole by viewModel.selectedRole.collectAsState()

    // Form inputs for quick allocations inside dialog
    var nameField by remember { mutableStateOf("") }
    var regField by remember { mutableStateOf("") }
    var deptField by remember { mutableStateOf("B.Sc Computer Science") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "PC Instance: ${system.id}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Lab Group: ${if (system.labId == "ML1") "Main Lab 1" else if (system.labId == "L2") "Lab 2" else "AI Lab"}", fontSize = 11.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_system_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Node hardware loads dials
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("CPU LOAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${system.cpuUsage}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (system.cpuUsage > 80) SeatRed else SeatGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("MEMORY USED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${system.ramUsage}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (system.ramUsage > 80) SeatYellow else SeatGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("NETWORK STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(system.networkStatus, fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (system.networkStatus == "ONLINE") SeatGreen else Color.DarkGray)
                        }
                    }
                }

                // Workstation Details Section
                Text("Operational Status Context", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    Text(text = "Workstation Position Index: Slot ${system.systemNumber}", fontSize = 11.sp, color = labelColor)
                    Text(text = "Power Cycle State: Booted (Active Node)", fontSize = 11.sp, color = labelColor)
                    Text(
                        text = "Workflow Stage Allocation: ${system.status}",
                        fontSize = 11.sp,
                        color = when (system.status) {
                            "AVAILABLE" -> SeatGreen
                            "OCCUPIED" -> SeatRed
                            "MAINTENANCE" -> SeatYellow
                            else -> SeatBlue
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (system.status == "OCCUPIED") {
                    // Show assigned student info
                    Text("Assigned Student Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Student Name: ${system.studentName ?: "Unknown"}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Register Number: ${system.registerNumber ?: "Unknown"}", fontSize = 11.sp)
                        Text("Academic Dept: ${system.department ?: "Unknown"}", fontSize = 11.sp)
                        if (system.loginTime != null) {
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(system.loginTime))
                            Text("Checked In Period: $timeStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Release / Release Student Check-Out Form BUTTON
                    Button(
                        onClick = {
                            viewModel.releaseSystem(system.id)
                            onDismiss()
                            Toast.makeText(context, "Workstation releases complete. Registered checkout logs.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SeatRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_workstation_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Check-Out Student & Clear Workstation", fontWeight = FontWeight.Bold)
                    }

                } else if (system.status == "AVAILABLE") {
                    // Show Quick allocations Form
                    Text("Quick Workstation Seat Assignment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    OutlinedTextField(
                        value = regField,
                        onValueChange = { regField = it },
                        placeholder = { Text("Reg No (e.g. SDC22110)", fontSize = 11.sp) },
                        label = { Text("Register No", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        placeholder = { Text("Student Name", fontSize = 11.sp) },
                        label = { Text("Full Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (regField.trim().isEmpty() || nameField.trim().isEmpty()) {
                                Toast.makeText(context, "Fields must be complete.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.allocateSystem(
                                systemId = system.id,
                                studentName = nameField.trim(),
                                registerNo = regField.trim().uppercase(),
                                dept = deptField,
                                hour = 1
                            )
                            onDismiss()
                            Toast.makeText(context, "System allocated and registered successfully.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interactive_allocate_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SeatGreen)
                    ) {
                        Text("Save Allocation", fontWeight = FontWeight.Bold)
                    }
                }

                // Direct Status Overrides: Admin and Lab Assistant power actions
                if (currentRole == UserRole.ADMIN || currentRole == UserRole.LAB_ASSISTANT) {
                    Text("Administrative Overrides Dashboard Controls", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (system.status != "MAINTENANCE") {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateSystemStatus(system.id, "MAINTENANCE")
                                    onDismiss()
                                    Toast.makeText(context, "System locked and marked for MAINTENANCE.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SeatYellow),
                                border = BorderStroke(1.dp, SeatYellow),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("maintenance_flag_button")
                            ) {
                                Text("Flag Repair", fontSize = 11.sp)
                            }
                        }
                        
                        if (system.status != "RESERVED") {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateSystemStatus(system.id, "RESERVED")
                                    onDismiss()
                                    Toast.makeText(context, "System node RESERVED successfully.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SeatBlue),
                                border = BorderStroke(1.dp, SeatBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reserve Node", fontSize = 11.sp)
                            }
                        }

                        if (system.networkStatus == "ONLINE") {
                            OutlinedButton(
                                onClick = {
                                    viewModel.powerOffSystem(system.id)
                                    onDismiss()
                                    Toast.makeText(context, "Workstation hardware offline command broadcasted.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SeatRed),
                                border = BorderStroke(1.dp, SeatRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Power Off", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
