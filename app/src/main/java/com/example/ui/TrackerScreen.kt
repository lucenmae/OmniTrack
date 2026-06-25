package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Tracker
import com.example.viewmodel.ActivityLogItem
import com.example.viewmodel.TrackerViewModel
import com.example.viewmodel.TrackerWithStats
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val trackers by viewModel.trackersWithStats.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentActivity.collectAsStateWithLifecycle()
    val completionRate by viewModel.todayCompletionRate.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var trackerToLogValue by remember { mutableStateOf<Tracker?>(null) }
    var trackerToEdit by remember { mutableStateOf<Tracker?>(null) }
    var showLogsHistory by remember { mutableStateOf(false) }

    // Modern color scheme mapping helper
    val colorPalette = listOf(
        "#10B981" to "Emerald Green",
        "#3B82F6" to "Sky Blue",
        "#F59E0B" to "Sunset Amber",
        "#EC4899" to "Rose Coral",
        "#8B5CF6" to "Purple Mist",
        "#06B6D4" to "Ocean Teal",
        "#EF4444" to "Crimson Red",
        "#8B4513" to "Coffee Brown",
        "#64748B" to "Slate Gray"
    )

    val iconList = listOf(
        "DirectionsWalk" to Icons.Default.DirectionsWalk,
        "LocalDrink" to Icons.Default.LocalDrink,
        "Wc" to Icons.Default.Wc,
        "LocalLaundryService" to Icons.Default.LocalLaundryService,
        "Bedtime" to Icons.Default.Bedtime,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "MedicalServices" to Icons.Default.MedicalServices,
        "Restaurant" to Icons.Default.Restaurant,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Book" to Icons.Default.Book,
        "AttachMoney" to Icons.Default.AttachMoney,
        "Pets" to Icons.Default.Pets,
        "SmokeFree" to Icons.Default.SmokeFree,
        "EmojiEmotions" to Icons.Default.EmojiEmotions
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            val todayString = sdf.format(Date())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OmniTrack",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = todayString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                    IconButton(
                        onClick = {
                            val nextMode = when (currentThemeMode) {
                                "SYSTEM" -> "LIGHT"
                                "LIGHT" -> "DARK"
                                else -> "SYSTEM"
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        val themeIcon = when (currentThemeMode) {
                            "LIGHT" -> Icons.Default.LightMode
                            "DARK" -> Icons.Default.DarkMode
                            else -> Icons.Default.BrightnessMedium
                        }
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = "Toggle Theme Mode (Current: $currentThemeMode)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showLogsHistory = !showLogsHistory },
                        modifier = Modifier.testTag("toggle_history_button")
                    ) {
                        Icon(
                            imageVector = if (showLogsHistory) Icons.Default.Dashboard else Icons.Default.History,
                            contentDescription = "Toggle History View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showLogsHistory) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("add_tracker_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Tracker")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = showLogsHistory,
                transitionSpec = {
                    slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                            slideOutVertically(targetOffsetY = { it }) + fadeOut()
                },
                label = "ScreenState"
            ) { isHistoryOpen ->
                if (isHistoryOpen) {
                    HistoryView(
                        recentLogs = recentLogs,
                        onDeleteLog = { viewModel.deleteLog(it) },
                        onClose = { showLogsHistory = false }
                    )
                } else {
                    DashboardView(
                        trackers = trackers,
                        completionRate = completionRate,
                        onLogInstant = { tracker, valToLog ->
                            viewModel.logActivity(tracker.id, valToLog)
                        },
                        onOpenLogQuantity = { trackerToLogValue = it },
                        onEditTracker = { trackerToEdit = it }
                    )
                }
            }
        }
    }

    // CREATE TRACKER DIALOG
    if (showCreateDialog) {
        CreateTrackerDialog(
            colorPalette = colorPalette,
            iconList = iconList,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, type, unit, iconName, colorHex, targetValue ->
                viewModel.addTracker(name, type, unit, iconName, colorHex, targetValue)
                showCreateDialog = false
            }
        )
    }

    // LOG QUANTITY DIALOG
    if (trackerToLogValue != null) {
        LogValueDialog(
            tracker = trackerToLogValue!!,
            onDismiss = { trackerToLogValue = null },
            onConfirm = { value, note ->
                viewModel.logActivity(trackerToLogValue!!.id, value, note)
                trackerToLogValue = null
            }
        )
    }

    // EDIT/DELETE TRACKER DIALOG
    if (trackerToEdit != null) {
        EditTrackerDialog(
            tracker = trackerToEdit!!,
            colorPalette = colorPalette,
            iconList = iconList,
            onDismiss = { trackerToEdit = null },
            onSave = { updated ->
                viewModel.updateTracker(updated)
                trackerToEdit = null
            },
            onDelete = {
                viewModel.deleteTracker(trackerToEdit!!)
                trackerToEdit = null
            }
        )
    }
}

// MAIN DASHBOARD VIEW
@Composable
fun DashboardView(
    trackers: List<TrackerWithStats>,
    completionRate: Float,
    onLogInstant: (Tracker, Float) -> Unit,
    onOpenLogQuantity: (Tracker) -> Unit,
    onEditTracker: (Tracker) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            HeroCard(completionRate = completionRate, trackersCount = trackers.size)
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracked Activities",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${trackers.size} Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List / Grid of Trackers
        if (trackers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trackers set up yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the + button to build a customizable tracker!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        } else {
            items(trackers) { model ->
                TrackerItemCard(
                    model = model,
                    onLogInstant = onLogInstant,
                    onOpenLogQuantity = onOpenLogQuantity,
                    onEditTracker = onEditTracker
                )
            }
        }

        // Extra spacing at the bottom for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// HERO HEADER BLOCK
@Composable
fun HeroCard(completionRate: Float, trackersCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val percentageText = (completionRate * 100).toInt()
                Text(
                    text = if (trackersCount == 0) "Setup your targets!" else "$percentageText% Done",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (trackersCount == 0) "Create trackers to begin tracking your habits."
                    else if (completionRate >= 1.0f) "Incredible! All of today's targets reached!"
                    else "Keep going! You're making progress today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Animated circle drawing
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                val circleColor = MaterialTheme.colorScheme.onPrimaryContainer
                val strokeColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = strokeColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = circleColor,
                        startAngle = -90f,
                        sweepAngle = completionRate * 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// INDIVIDUAL TRACKER LIST CARD
@Composable
fun TrackerItemCard(
    model: TrackerWithStats,
    onLogInstant: (Tracker, Float) -> Unit,
    onOpenLogQuantity: (Tracker) -> Unit,
    onEditTracker: (Tracker) -> Unit
) {
    val tracker = model.tracker
    val tintColor = Color(android.graphics.Color.parseColor(tracker.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("tracker_card_${tracker.id}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Icon, Title, and Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(tintColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByName(tracker.iconName),
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tracker.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Type: ${tracker.type.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (model.streakDays > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🔥 ${model.streakDays}d",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = { onEditTracker(tracker) },
                    modifier = Modifier.testTag("edit_tracker_${tracker.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit tracker details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Logging statistics and Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    val formattedValue = if (model.todayValue % 1f == 0f) model.todayValue.toInt().toString() else "%.1f".format(model.todayValue)
                    Text(
                        text = buildString {
                            append("Today: ")
                            append(formattedValue)
                            if (tracker.targetValue != null) {
                                val targetFormatted = if (tracker.targetValue % 1f == 0f) tracker.targetValue.toInt().toString() else "%.1f".format(tracker.targetValue)
                                append(" / ")
                                append(targetFormatted)
                            }
                            append(" ")
                            append(tracker.unit)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (model.lastLoggedAt != null) {
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        Text(
                            text = "Last logged: ${sdf.format(Date(model.lastLoggedAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "No logs logged today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Interactive Buttons
                when (tracker.type) {
                    "COUNTER" -> {
                        Button(
                            onClick = { onLogInstant(tracker, 1f) },
                            colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("quick_log_btn_${tracker.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log +1", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    "BOOLEAN" -> {
                        val completedToday = model.todayValue > 0f
                        FilledTonalButton(
                            onClick = { onLogInstant(tracker, if (completedToday) -1f else 1f) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (completedToday) tintColor.copy(alpha = 0.15f) else tintColor,
                                contentColor = if (completedToday) tintColor else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("quick_log_btn_${tracker.id}")
                        ) {
                            Icon(
                                imageVector = if (completedToday) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (completedToday) "Logged" else "Log Done",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        // "QUANTITY" or "TIMER" or others
                        Button(
                            onClick = { onOpenLogQuantity(tracker) },
                            colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("quick_log_btn_${tracker.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Value", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Visual Progress Bar
            if (tracker.targetValue != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val progressFraction = (model.todayValue / tracker.targetValue).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = tintColor,
                    trackColor = tintColor.copy(alpha = 0.12f)
                )
            }

            // Row 3: Little 7 Days Activity Dots
            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly view",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    model.weeklyHistory.forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (day.isTargetMet) tintColor else tintColor.copy(alpha = 0.12f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (day.isTargetMet) Color.Transparent else tintColor.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// LOG VALUE MODAL DIALOG
@Composable
fun LogValueDialog(
    tracker: Tracker,
    onDismiss: () -> Unit,
    onConfirm: (Float, String?) -> Unit
) {
    var loggedText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val tintColor = Color(android.graphics.Color.parseColor(tracker.colorHex))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("log_value_dialog"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Log ${tracker.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = loggedText,
                    onValueChange = {
                        loggedText = it
                        errorMsg = null
                    },
                    label = { Text("Log quantity (${tracker.unit})") },
                    placeholder = { Text("e.g. 250 or 1.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tintColor,
                        focusedLabelColor = tintColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_value_input")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Add optional note") },
                    placeholder = { Text("e.g. before bed, morning stroll") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_note_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = loggedText.toFloatOrNull()
                            if (parsed == null || parsed < 0f) {
                                errorMsg = "Please enter a valid positive number"
                            } else {
                                onConfirm(parsed, noteText.trim().ifEmpty { null })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                        modifier = Modifier.testTag("log_value_confirm_btn")
                    ) {
                        Text("Save Entry", color = Color.White)
                    }
                }
            }
        }
    }
}

// CREATE TRACKER MODAL DIALOG
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTrackerDialog(
    colorPalette: List<Pair<String, String>>,
    iconList: List<Pair<String, ImageVector>>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Float?) -> Unit
) {
    var trackerName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("COUNTER") } // COUNTER, QUANTITY, BOOLEAN
    var trackerUnit by remember { mutableStateOf("times") }
    var targetText by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#10B981") }
    var selectedIconName by remember { mutableStateOf("Star") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("create_tracker_dialog"),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Build Custom Tracker",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Name Input
                item {
                    OutlinedTextField(
                        value = trackerName,
                        onValueChange = {
                            trackerName = it
                            errorMsg = null
                        },
                        label = { Text("What are you tracking?") },
                        placeholder = { Text("e.g. Walking, Coffee, Reading") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_name_input")
                    )
                }

                // Tracker Type Select
                item {
                    Text(
                        text = "Measurement Style",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("COUNTER" to "Simple +1", "QUANTITY" to "Quantity", "BOOLEAN" to "Checkbox").forEach { (typeKey, typeLabel) ->
                            val isSelected = selectedType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedType = typeKey
                                    // Update defaults depending on type selected
                                    trackerUnit = when (typeKey) {
                                        "COUNTER" -> "times"
                                        "BOOLEAN" -> "done"
                                        else -> "ml"
                                    }
                                },
                                label = { Text(typeLabel) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_type_$typeKey")
                            )
                        }
                    }
                }

                // Unit of measurement & Target Input
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedType != "BOOLEAN") {
                            OutlinedTextField(
                                value = trackerUnit,
                                onValueChange = { trackerUnit = it },
                                label = { Text("Unit") },
                                placeholder = { Text("e.g. ml, steps, hours") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tracker_unit_input")
                            )
                        }
                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            label = { Text("Daily Goal") },
                            placeholder = { Text("Optional") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("tracker_target_input")
                        )
                    }
                }

                // Icon Picker Grid
                item {
                    Text(
                        text = "Representing Icon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        iconList.forEach { (iconKey, vector) ->
                            val isSelected = selectedIconName == iconKey
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(android.graphics.Color.parseColor(selectedColorHex))
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    )
                                    .clickable { selectedIconName = iconKey }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.15f
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = iconKey,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.7f
                                    ),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Color Picker Grid
                item {
                    Text(
                        text = "Theme Accent Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorPalette.forEach { (colorHex, colorName) ->
                            val isSelected = selectedColorHex == colorHex
                            val hexColor = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(hexColor)
                                    .clickable { selectedColorHex = colorHex }
                                    .border(
                                        width = 2.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                if (errorMsg != null) {
                    item {
                        Text(
                            text = errorMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Actions
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (trackerName.trim().isEmpty()) {
                                    errorMsg = "Please enter an activity name"
                                } else {
                                    val targetVal = targetText.toFloatOrNull()
                                    onConfirm(
                                        trackerName.trim(),
                                        selectedType,
                                        trackerUnit.trim(),
                                        selectedIconName,
                                        selectedColorHex,
                                        targetVal
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(android.graphics.Color.parseColor(selectedColorHex))
                            ),
                            modifier = Modifier.testTag("tracker_save_button")
                        ) {
                            Text("Create", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// EDIT/DELETE TRACKER DIALOG
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTrackerDialog(
    tracker: Tracker,
    colorPalette: List<Pair<String, String>>,
    iconList: List<Pair<String, ImageVector>>,
    onDismiss: () -> Unit,
    onSave: (Tracker) -> Unit,
    onDelete: () -> Unit
) {
    var trackerName by remember { mutableStateOf(tracker.name) }
    var trackerUnit by remember { mutableStateOf(tracker.unit) }
    var targetText by remember { mutableStateOf(tracker.targetValue?.toString() ?: "") }
    var selectedColorHex by remember { mutableStateOf(tracker.colorHex) }
    var selectedIconName by remember { mutableStateOf(tracker.iconName) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_tracker_dialog"),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (showDeleteConfirmation) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Are you absolutely sure?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deleting this tracker will irreversibly delete all associated tracking log entries.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("No, Cancel")
                        }
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("confirm_delete_tracker")
                        ) {
                            Text("Yes, Delete", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Edit Tracker",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = { showDeleteConfirmation = true },
                                modifier = Modifier.testTag("delete_tracker_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete tracker",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Name Input
                    item {
                        OutlinedTextField(
                            value = trackerName,
                            onValueChange = {
                                trackerName = it
                                errorMsg = null
                            },
                            label = { Text("What are you tracking?") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_tracker_name")
                        )
                    }

                    // Unit of measurement & Target Input
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (tracker.type != "BOOLEAN") {
                                OutlinedTextField(
                                    value = trackerUnit,
                                    onValueChange = { trackerUnit = it },
                                    label = { Text("Unit") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_tracker_unit")
                                )
                            }
                            OutlinedTextField(
                                value = targetText,
                                onValueChange = { targetText = it },
                                label = { Text("Daily Goal") },
                                placeholder = { Text("Optional") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("edit_tracker_target")
                            )
                        }
                    }

                    // Icon Picker Grid
                    item {
                        Text(
                            text = "Representing Icon",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            iconList.forEach { (iconKey, vector) ->
                                val isSelected = selectedIconName == iconKey
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(android.graphics.Color.parseColor(selectedColorHex))
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        )
                                        .clickable { selectedIconName = iconKey }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.15f
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = iconKey,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.7f
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Color Picker Grid
                    item {
                        Text(
                            text = "Theme Accent Color",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            colorPalette.forEach { (colorHex, colorName) ->
                                val isSelected = selectedColorHex == colorHex
                                val hexColor = Color(android.graphics.Color.parseColor(colorHex))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(hexColor)
                                        .clickable { selectedColorHex = colorHex }
                                        .border(
                                            width = 2.5.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    if (errorMsg != null) {
                        item {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Actions
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (trackerName.trim().isEmpty()) {
                                        errorMsg = "Please enter an activity name"
                                    } else {
                                        val targetVal = targetText.toFloatOrNull()
                                        onSave(
                                            tracker.copy(
                                                name = trackerName.trim(),
                                                unit = trackerUnit.trim(),
                                                iconName = selectedIconName,
                                                colorHex = selectedColorHex,
                                                targetValue = targetVal
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(android.graphics.Color.parseColor(selectedColorHex))
                                ),
                                modifier = Modifier.testTag("save_tracker_btn")
                            ) {
                                Text("Save Changes", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// COLLAPSIBLE LOGS HISTORY VIEW
@Composable
fun HistoryView(
    recentLogs: List<ActivityLogItem>,
    onDeleteLog: (Long) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("history_list_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tracked Logs History",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            FilledTonalButton(
                onClick = onClose,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dashboard", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recentLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No history recorded yet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Once you log activities, your historical entries will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentLogs, key = { it.logId }) { log ->
                    val colorHex = Color(android.graphics.Color.parseColor(log.trackerColor))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_item_${log.logId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorHex.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(log.trackerIcon),
                                    contentDescription = null,
                                    tint = colorHex,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = log.trackerName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                                    Text(
                                        text = sdf.format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                val valueDisplay = if (log.loggedValue % 1f == 0f) log.loggedValue.toInt().toString() else "%.1f".format(log.loggedValue)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Logged: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$valueDisplay ${log.unit}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorHex
                                    )
                                }

                                if (!log.note.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "Note: ${log.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onDeleteLog(log.logId) },
                                modifier = Modifier.testTag("delete_log_btn_${log.logId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete entry",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// RESOLVE DYNAMIC MATERIAL SYMBOL ICONS
fun getIconByName(name: String): ImageVector {
    return when (name) {
        "DirectionsWalk" -> Icons.Default.DirectionsWalk
        "LocalDrink" -> Icons.Default.LocalDrink
        "Wc" -> Icons.Default.Wc
        "LocalLaundryService" -> Icons.Default.LocalLaundryService
        "Bedtime" -> Icons.Default.Bedtime
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "MedicalServices" -> Icons.Default.MedicalServices
        "Restaurant" -> Icons.Default.Restaurant
        "Star" -> Icons.Default.Star
        "Favorite" -> Icons.Default.Favorite
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Book" -> Icons.Default.Book
        "AttachMoney" -> Icons.Default.AttachMoney
        "Pets" -> Icons.Default.Pets
        "SmokeFree" -> Icons.Default.SmokeFree
        "EmojiEmotions" -> Icons.Default.EmojiEmotions
        else -> Icons.Default.Star
    }
}
