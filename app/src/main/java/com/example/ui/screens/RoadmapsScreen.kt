package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AouRoadmapsData
import com.example.data.UiStrings
import com.example.model.CourseCategory
import com.example.model.CourseNode
import com.example.model.CourseStatus
import com.example.ui.theme.LocalUniSyncColors
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusNextTerm
import com.example.ui.theme.StatusNotStarted
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RoadmapsScreen(
    selectedMajorId: String,
    courseStatuses: Map<String, CourseStatus>,
    isRtl: Boolean,
    strings: UiStrings,
    onSelectMajor: (String) -> Unit,
    onUpdateCourseStatus: (String, String, CourseStatus) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val major = AouRoadmapsData.allMajors.firstOrNull { it.id == selectedMajorId }
        ?: AouRoadmapsData.allMajors.first()

    var majorDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<CourseNode?>(null) }
    var targetZoom by remember { mutableFloatStateOf(1.0f) }
    val zoom by animateFloatAsState(targetValue = targetZoom, label = "roadmap_zoom")

    val stageEntries = remember(major) {
        major.courses.groupBy { it.stage }.toSortedMap().entries.map { it.key to it.value }
    }
    val completedCount = major.courses.count {
        (courseStatuses[it.code] ?: CourseStatus.NOT_STARTED) == CourseStatus.COMPLETED
    }
    val inProgressCount = major.courses.count {
        (courseStatuses[it.code] ?: CourseStatus.NOT_STARTED) == CourseStatus.IN_PROGRESS
    }
    val nextTermCount = major.courses.count {
        (courseStatuses[it.code] ?: CourseStatus.NOT_STARTED) == CourseStatus.NEXT_TERM
    }
    val notStartedCount = major.courses.size - completedCount - inProgressCount - nextTermCount
    val progress = if (major.courses.isNotEmpty()) completedCount.toFloat() / major.courses.size else 0f

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()
    var containerCoords by remember(major.id) { mutableStateOf<LayoutCoordinates?>(null) }
    val nodeBounds = remember(major.id) { mutableStateMapOf<String, Rect>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Zoom Controls (+, -, Reset 100%)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.roadmaps,
                style = MaterialTheme.typography.titleLarge,
                color = ext.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ext.cardBorder),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { targetZoom = (targetZoom - 0.15f).coerceAtLeast(0.6f) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("roadmap_zoom_out")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Zoom Out",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .testTag("roadmap_zoom_reset")
                            .clickable { targetZoom = 1.0f }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Reset 100%",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${(targetZoom * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = { targetZoom = (targetZoom + 0.15f).coerceAtMost(1.75f) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("roadmap_zoom_in")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Zoom In",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Major Selector Dropdown (All 10 AOU Majors)
        Box {
            OutlinedButton(
                onClick = { majorDropdownExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("major_selector_button")
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRtl) major.nameAr else major.nameEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${major.facultyEn} • ${major.courses.size} courses",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.textSecondary
                    )
                }
                Icon(Icons.Filled.ExpandMore, contentDescription = "Select Major")
            }

            DropdownMenu(
                expanded = majorDropdownExpanded,
                onDismissRequest = { majorDropdownExpanded = false }
            ) {
                AouRoadmapsData.allMajors.forEach { m ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = if (isRtl) m.nameAr else m.nameEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (m.id == major.id) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${m.courses.size} courses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.textSecondary
                                )
                            }
                        },
                        onClick = {
                            onSelectMajor(m.id)
                            majorDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Live Completion Counters + Progress Bar + Category Legend
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ext.cardBorder),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRtl) "تقدم الخطة الدراسية" else "Degree Progress",
                        style = MaterialTheme.typography.labelLarge,
                        color = ext.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedCount / ${major.courses.size} (${(progress * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("roadmap_completion_counter")
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCounterChip(
                        label = if (isRtl) CourseStatus.COMPLETED.labelAr else CourseStatus.COMPLETED.labelEn,
                        count = completedCount,
                        color = StatusCompleted
                    )
                    StatusCounterChip(
                        label = if (isRtl) CourseStatus.IN_PROGRESS.labelAr else CourseStatus.IN_PROGRESS.labelEn,
                        count = inProgressCount,
                        color = StatusInProgress
                    )
                    StatusCounterChip(
                        label = if (isRtl) CourseStatus.NEXT_TERM.labelAr else CourseStatus.NEXT_TERM.labelEn,
                        count = nextTermCount,
                        color = StatusNextTerm
                    )
                    StatusCounterChip(
                        label = if (isRtl) CourseStatus.NOT_STARTED.labelAr else CourseStatus.NOT_STARTED.labelEn,
                        count = notStartedCount,
                        color = StatusNotStarted
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CourseCategory.entries.forEach { cat ->
                        val catColor = categoryColor(cat)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catColor, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = if (isRtl) cat.labelAr else cat.labelEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // True 2D Visual Flowchart Diagram with Prerequisite Bezier Connectors & Stage Arrows
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds(),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, ext.cardBorder),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(vScrollState)
                    .horizontalScroll(hScrollState)
            ) {
                val prereqConnectorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                val stageArrowColor = ext.textSecondary.copy(alpha = 0.65f)

                Box(
                    modifier = Modifier
                        .padding((16f * zoom).dp)
                        .onGloballyPositioned { coords -> containerCoords = coords }
                ) {
                    // Canvas Layer drawing exact connected prerequisite arrows & stage links between nodes
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val strokePx = (2.4f * zoom).coerceAtLeast(1.5f)
                        val arrowLen = (9f * zoom).coerceAtLeast(6f)

                        // 1. Direct Prerequisite Arrows across stages / courses
                        major.courses.forEach { targetCourse ->
                            val targetRect = nodeBounds[targetCourse.code] ?: return@forEach
                            targetCourse.prerequisites.forEach { preCode ->
                                val sourceRect = nodeBounds[preCode] ?: return@forEach
                                val sameRow = kotlin.math.abs(sourceRect.top - targetRect.top) < 24f * zoom
                                val start = if (sameRow) {
                                    Offset(sourceRect.right, sourceRect.center.y)
                                } else {
                                    Offset(sourceRect.center.x, sourceRect.bottom)
                                }
                                val end = if (sameRow) {
                                    Offset(targetRect.left, targetRect.center.y)
                                } else {
                                    Offset(targetRect.center.x, targetRect.top)
                                }

                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    if (sameRow) {
                                        lineTo(end.x, end.y)
                                    } else {
                                        val midY = (start.y + end.y) / 2f
                                        cubicTo(
                                            start.x, midY,
                                            end.x, midY,
                                            end.x, end.y
                                        )
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = prereqConnectorColor,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                                drawArrowHead(
                                    tip = end,
                                    from = if (sameRow) start else Offset(end.x, (start.y + end.y) / 2f),
                                    color = prereqConnectorColor,
                                    size = arrowLen
                                )
                            }
                        }

                        // 2. Fallback Stage-to-Stage Backbone Arrows when adjacent stage courses lack explicit prereq links
                        stageEntries.zipWithNext().forEach { (currStage, nextStage) ->
                            val currFirst = currStage.second.firstOrNull()?.let { nodeBounds[it.code] }
                            val nextFirst = nextStage.second.firstOrNull()?.let { nodeBounds[it.code] }
                            if (currFirst != null && nextFirst != null) {
                                val start = Offset(currFirst.center.x, currFirst.bottom)
                                val end = Offset(nextFirst.center.x, nextFirst.top)
                                drawLine(
                                    color = stageArrowColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = strokePx * 0.75f,
                                    cap = StrokeCap.Round
                                )
                                drawArrowHead(tip = end, from = start, color = stageArrowColor, size = arrowLen * 0.85f)
                            }
                        }
                    }

                    // Structured Level / Stage Node Rows
                    Column(
                        verticalArrangement = Arrangement.spacedBy((34f * zoom).dp)
                    ) {
                        stageEntries.forEach { (stageNum, courses) ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy((8f * zoom).dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isRtl) "المرحلة $stageNum" else "Stage $stageNum",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = (13f * zoom).coerceIn(10f, 20f).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = (10f * zoom).dp,
                                            vertical = (4f * zoom).dp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy((26f * zoom).dp)
                                ) {
                                    courses.forEach { course ->
                                        val status = courseStatuses[course.code] ?: CourseStatus.NOT_STARTED
                                        FlowchartCourseNodeCard(
                                            course = course,
                                            status = status,
                                            zoom = zoom,
                                            isRtl = isRtl,
                                            onPositioned = { childCoords ->
                                                val parent = containerCoords
                                                if (parent != null && parent.isAttached && childCoords.isAttached) {
                                                    nodeBounds[course.code] = parent.localBoundingBoxOf(childCoords, false)
                                                }
                                            },
                                            onOpenDetails = { selectedCourse = course },
                                            onQuickStatusChange = { newStatus ->
                                                onUpdateCourseStatus(major.id, course.code, newStatus)
                                            }
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

    // Course Detail + Prerequisite Modal + All 4 Course Statuses
    selectedCourse?.let { course ->
        val currentStatus = courseStatuses[course.code] ?: CourseStatus.NOT_STARTED
        AlertDialog(
            onDismissRequest = { selectedCourse = null },
            title = {
                Column {
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${course.credits} Credit Hours • ${if (isRtl) course.category.labelAr else course.category.labelEn}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ext.textSecondary
                    )
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.textPrimary
                    )
                    Text(
                        text = if (course.prerequisites.isEmpty()) {
                            "Prerequisites: None (Entry Course)"
                        } else {
                            "Prerequisites: ${course.prerequisites.joinToString(" → ")}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (isRtl) "حالة المادة" else "Course Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    CourseStatus.entries.forEach { st ->
                        val active = st == currentStatus
                        val color = statusColor(st)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("modal_status_${st.key}")
                                .clickable {
                                    onUpdateCourseStatus(major.id, course.code, st)
                                    selectedCourse = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.background,
                            border = BorderStroke(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) color else ext.cardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${st.labelEn} (${st.labelAr})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (active) color else ext.textPrimary,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                                if (active) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCourse = null }) { Text("Close") }
            }
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
    tip: Offset,
    from: Offset,
    color: Color,
    size: Float
) {
    val angle = atan2((tip.y - from.y).toDouble(), (tip.x - from.x).toDouble())
    val spread = Math.PI / 6.0
    val p1 = Offset(
        x = tip.x - (size * cos(angle - spread)).toFloat(),
        y = tip.y - (size * sin(angle - spread)).toFloat()
    )
    val p2 = Offset(
        x = tip.x - (size * cos(angle + spread)).toFloat(),
        y = tip.y - (size * sin(angle + spread)).toFloat()
    )
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
private fun StatusCounterChip(
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FlowchartCourseNodeCard(
    course: CourseNode,
    status: CourseStatus,
    zoom: Float,
    isRtl: Boolean,
    onPositioned: (LayoutCoordinates) -> Unit,
    onOpenDetails: () -> Unit,
    onQuickStatusChange: (CourseStatus) -> Unit
) {
    val ext = LocalUniSyncColors.current
    val catColor = categoryColor(course.category)
    val stColor = statusColor(status)
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width((168f * zoom).dp)
            .onGloballyPositioned(onPositioned)
            .testTag("course_node_${course.code}")
            .clickable { onOpenDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (status == CourseStatus.NOT_STARTED) 1.dp else 1.8.dp,
            color = if (status == CourseStatus.NOT_STARTED) ext.cardBorder else stColor
        ),
        shape = RoundedCornerShape((12f * zoom).dp)
    ) {
        Column(
            modifier = Modifier.padding((10f * zoom).dp),
            verticalArrangement = Arrangement.spacedBy((5f * zoom).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = catColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = (12f * zoom).coerceIn(9f, 18f).sp
                        ),
                        color = catColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = (6f * zoom).dp, vertical = (2f * zoom).dp)
                    )
                }
                Text(
                    text = "${course.credits}cr",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (11f * zoom).coerceIn(8f, 16f).sp
                    ),
                    color = ext.textSecondary
                )
            }

            Text(
                text = course.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (12f * zoom).coerceIn(9f, 18f).sp,
                    lineHeight = (16f * zoom).coerceIn(12f, 24f).sp
                ),
                color = ext.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height((36f * zoom).dp)
            )

            if (course.prerequisites.isNotEmpty()) {
                Text(
                    text = "→ from ${course.prerequisites.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (10f * zoom).coerceIn(8f, 15f).sp
                    ),
                    color = ext.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Immediate 1-tap Status Selector Pill directly on every node
            Box {
                Surface(
                    color = stColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, stColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_status_pill_${course.code}")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { statusMenuExpanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (7f * zoom).dp, vertical = (4f * zoom).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRtl) status.labelAr else status.labelEn,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (11f * zoom).coerceIn(8f, 16f).sp
                            ),
                            color = stColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Change course status",
                            tint = stColor,
                            modifier = Modifier.size((14f * zoom).coerceIn(11f, 20f).dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false }
                ) {
                    CourseStatus.entries.forEach { st ->
                        val active = st == status
                        val c = statusColor(st)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${st.labelEn} (${st.labelAr})",
                                    color = if (active) c else ext.textPrimary,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(c, RoundedCornerShape(3.dp))
                                )
                            },
                            onClick = {
                                onQuickStatusChange(st)
                                statusMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun categoryColor(category: CourseCategory): Color = when (category) {
    CourseCategory.UNIVERSITY_REQ -> Color(0xFF0E7490)
    CourseCategory.FACULTY_REQ -> Color(0xFF1E4E8C)
    CourseCategory.MAJOR_CORE -> Color(0xFF1F6E5A)
    CourseCategory.ELECTIVE_PROJECT -> Color(0xFFC86432)
}

private fun statusColor(status: CourseStatus): Color = when (status) {
    CourseStatus.COMPLETED -> StatusCompleted
    CourseStatus.IN_PROGRESS -> StatusInProgress
    CourseStatus.NEXT_TERM -> StatusNextTerm
    CourseStatus.NOT_STARTED -> StatusNotStarted
}
