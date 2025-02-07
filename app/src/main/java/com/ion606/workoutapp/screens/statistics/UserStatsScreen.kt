package com.ion606.workoutapp.screens.statistics;

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ion606.workoutapp.dataObjects.User.DailyStat
import com.ion606.workoutapp.dataObjects.User.MonthlyWorkout
import com.ion606.workoutapp.dataObjects.User.PersonalBest
import com.ion606.workoutapp.dataObjects.User.UserStats
import com.ion606.workoutapp.helpers.generateRandomVibrantColor
import com.ion606.workoutapp.helpers.saveJsonContentToDownloads
import com.ion606.workoutapp.managers.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class UserStatsScreen {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun WorkoutStatsScreen(
            context: Context,
            dataManager: DataManager,
            navController: NavController
        ) {
            val scope = rememberCoroutineScope();
            val statsMut: MutableState<UserStats?> = remember { mutableStateOf(null) };

            LaunchedEffect(Unit) {
                scope.launch {
                    val r = dataManager.getUserStats();

                    when (r is DataManager.Result.Success) {
                        true -> statsMut.value = r.data;
                        false -> {
                            Log.d("USERSTATSSCREEN", "Failed to fetch user stats"); }
                    }
                }
            }

            // Inside WorkoutStatsScreen composable
            val tabTitles = listOf("Stats", "Recovery")
            val (selectedTabIndex, setSelectedTab) = remember { mutableStateOf(0) }

            Scaffold(modifier = Modifier.padding(0.dp), topBar = {
                Column {
                    TopAppBar(
                        title = { Text("workout stats") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                );
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    );

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { setSelectedTab(index) },
                                text = { Text(title.uppercase()) }
                            )
                        }
                    }
                }
            }) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTabIndex) {
                        0 -> StatsContent(
                            statsMut,
                            innerPadding = innerPadding,
                            scope = scope,
                            dataManager = dataManager,
                            context = context
                        )

                        1 -> RecoveryContent(statsMut.value!!)
                    }
                }
            }
        }

        @Composable
        private fun StatsContent(
            statsMut: MutableState<UserStats?>,
            innerPadding: PaddingValues,
            scope: CoroutineScope,
            dataManager: DataManager,
            context: Context
        ) {
            statsMut.value?.let { stats ->
                LazyColumn(
                    modifier = Modifier
                        .padding(0.dp)
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. overall activity card - grid replacement
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "🏋️ Activity Overview",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // responsive grid using row and weight modifiers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(
                                        value = stats.overallActivity.totalWorkouts.toString(),
                                        label = "Total Workouts",
                                        icon = Icons.Outlined.FitnessCenter,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    StatItem(
                                        value = stats.overallActivity.longestStreak.toString(),
                                        label = "Longest Streak",
                                        icon = Icons.Outlined.Whatshot,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    StatItem(
                                        value = stats.overallActivity.currentStreak.toString(),
                                        label = "Current Streak",
                                        icon = Icons.Outlined.Star,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 2. time & duration section
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "⏱ Time Metrics",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // time chart using a library like MPAndroidChart
                                LineChartView(
                                    stats.timeAndDuration.dailyStats,
                                    label = "Daily Workout Time (min)"
                                )

                                KeyMetricRow(
                                    label = "Average Session",
                                    value = "${stats.timeAndDuration.averageWorkoutTime.roundToInt()} min"
                                )
                            }
                        }
                    }

                    // 3. exercise distribution
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "💪 Exercise Breakdown",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                // body part distribution
                                DonutChart(
                                    data = stats.exerciseDistribution.byBodyPart,
                                    title = "By Body Part"
                                )

                                Spacer(modifier = Modifier.height(16.dp));

                                // exercise type distribution
                                HorizontalBarChart(
                                    data = stats.exerciseDistribution.byType,
                                    title = "By Exercise Type"
                                )

                                Spacer(modifier = Modifier.height(30.dp));

                                // top exercises
                                Text(
                                    "Top Exercises:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                stats.exerciseDistribution.topExercises.forEachIndexed { index, exercise ->
                                    ExerciseListItem(
                                        rank = index + 1,
                                        name = exercise.title,
                                        count = exercise.count
                                    )
                                }
                            }
                        }
                    }

                    // 4. personal bests
                    item {
                        PersonalBestsSection(stats.performance.personalBests)
                    }

                    // 5. consistency
                    item {
                        MonthlyConsistencyChart(stats.consistency.monthlyWorkouts)
                    }

                    // 6. button
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                                scope.launch {
                                    val r = dataManager.getUserStats(true);

                                    // result.error is for raw
                                    when (r is DataManager.Result.Error) {
                                        true -> saveJsonContentToDownloads(
                                            context,
                                            r.message,
                                            "user_stats.json"
                                        );
                                        false -> {
                                            Log.d(
                                                "USERSTATSSCREEN",
                                                "Failed to fetch user stats"
                                            );
                                        }
                                    }
                                }
                            }) {
                                Text("Get JSON Data")
                            }
                        }
                    }
                }
            }
        }

        @Composable
        private fun StatItem(
            value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier
        ) {
            Column(
                modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        @Composable
        fun LineChartView(
            dailyStats: List<DailyStat>, label: String
        ) {
            // sort daily stats by date; adjust parsing if needed for your date format
            val sortedStats = dailyStats.sortedBy { it.date }
            val cscheme = MaterialTheme.colorScheme.primary

            // if all values are zero, set yMax to 1 so division won’t be 0
            val rawYMax = sortedStats.maxOfOrNull { it.totalWorkoutTime } ?: 0
            val yMax = if (rawYMax == 0) 1 else rawYMax
            val yMin = 0

            // add a border to debug the canvas boundaries
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (sortedStats.size == 1) {
                    Text(
                        text = "Insufficient data",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    return@Box
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (sortedStats.isEmpty()) return@Canvas

                    // leave padding so that our drawing is not clipped
                    val xPadding = 32f
                    val yPadding = 32f

                    val chartWidth = size.width - 2 * xPadding
                    val chartHeight = size.height - 2 * yPadding

                    val points = mutableListOf<Offset>()
                    sortedStats.forEachIndexed { index, stat ->
                        val xRatio = index.toFloat() / (sortedStats.size - 1).coerceAtLeast(1)
                        val yRatio = stat.totalWorkoutTime.toFloat() / (yMax - yMin)
                        val xPos = xPadding + (chartWidth * xRatio)
                        val yPos = yPadding + (chartHeight * (1f - yRatio))
                        points.add(Offset(xPos, yPos))
                    }

                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path, color = cscheme, style = Stroke(width = 4f)
                        )
                    }

                    // draw small circles at each data point
                    points.forEach { point ->
                        drawCircle(
                            color = cscheme, radius = 6f, center = point
                        )
                    }
                }

                // label at the top center
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )
            }
        }

        @Composable
        fun DonutChart(
            data: Map<String, Int>, title: String, modifier: Modifier = Modifier
        ) {
            val total = data.values.sum().toFloat();
            // generate vibrant colors for each data key
            val colors = remember(data) { data.keys.map { generateRandomVibrantColor() } };

            Column(
                modifier = modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 60.dp, top = 20.dp)
                )

                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val innerRadius = size.minDimension * 0.4f;
                        var startAngle = -90f;

                        data.values.forEachIndexed { index, value ->
                            val sweep = (value / total) * 360f;
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = innerRadius, cap = StrokeCap.Butt)
                            );
                            startAngle += sweep;
                        }
                    }

                    // center text
                    Text(
                        text = "${total.roundToInt()} Exercises",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // legend
                Column(modifier = Modifier.padding(top = 40.dp)) {
                    data.entries.sortedByDescending { it.value }
                        .forEachIndexed { _, (label, value) ->
                            LegendItem(
                                label = label,
                                value = value,
                                color = colors[data.keys.indexOf(label) % colors.size]
                            );
                        }
                }
            }
        }

        @Composable
        private fun LegendItem(label: String, value: Int, color: Color) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        @Composable
        fun HorizontalBarChart(
            data: Map<String, Int>, title: String, modifier: Modifier = Modifier
        ) {
            val maxValue = data.values.maxOrNull()?.toFloat() ?: 1f;
            // build a map from each label to a vibrant color
            val colorsMap =
                remember(data) { data.keys.associateWith { generateRandomVibrantColor() } };

            Column(
                modifier = modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.entries.forEach { (label, value) ->
                        val vibrantColor = colorsMap[label]!!;
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(100.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .weight(1f)
                                    .background(
                                        color = vibrantColor,
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = value / maxValue)
                                        .fillMaxHeight()
                                        .background(
                                            color = vibrantColor,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$value", style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }


        @Composable
        fun MonthlyConsistencyChart(
            monthlyWorkouts: List<MonthlyWorkout>, modifier: Modifier = Modifier
        ) {
            val maxWorkouts = monthlyWorkouts.maxOfOrNull { it.workoutCount } ?: 1;
            // revert to original color scale for the dots with arrows
            val colorScale = listOf(
                Color(0xFFEBEDF0),  // lightest
                Color(0xFFC6E48B),  // light
                Color(0xFF7BC96F),  // medium
                Color(0xFF239A3B),  // dark
                Color(0xFF196127)   // darkest
            );

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Monthly Consistency",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // year and month labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jan", style = MaterialTheme.typography.bodySmall)
                    Text("Dec", style = MaterialTheme.typography.bodySmall)
                }

                // heatmap grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(12), // 12 months
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(12) { monthIndex ->
                        val monthData = monthlyWorkouts.find {
                            it.month.endsWith("-${(monthIndex + 1).toString().padStart(2, '0')}")
                        }
                        val workoutCount = monthData?.workoutCount ?: 0
                        val colorIndex =
                            (workoutCount.toFloat() / maxWorkouts * (colorScale.size - 1)).toInt();

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    color = colorScale[colorIndex],
                                    shape = MaterialTheme.shapes.small
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Text(
                                text = workoutCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (colorIndex < 2) Color.Black else Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                // legend with arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorScale.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(color, MaterialTheme.shapes.small)
                        );
                        if (index < colorScale.size - 1) {
                            Text(
                                text = "→",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            );
                        }
                    }
                }
            }
        }

        @Composable
        fun KeyMetricRow(
            label: String,
            value: String,
            icon: ImageVector? = null,
            modifier: Modifier = Modifier,
            onClick: (() -> Unit)? = null
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick?.invoke() })
                    .background(Color.Transparent)
                    .padding(vertical = 16.dp, horizontal = 20.dp), // more spacing
                shape = MaterialTheme.shapes.medium,
                tonalElevation = if (isPressed) 3.dp else 1.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp) // slightly larger for better visibility
                                .padding(end = 16.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // increased contrast
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (onClick != null) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        @Composable
        fun ExerciseListItem(
            rank: Int, name: String, count: Int, modifier: Modifier = Modifier
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // rank indicator in a circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // exercise details: name and count
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$count times",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // personal bests section
        @Composable
        private fun PersonalBestsSection(bests: List<PersonalBest>) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🥇 Personal Bests", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    bests.forEach { best ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(best.title, style = MaterialTheme.typography.bodyLarge)
                            Text("${best.maxWeight ?: best.maxReps} ${if (best.maxWeight != null) "kg" else "reps"}")
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
