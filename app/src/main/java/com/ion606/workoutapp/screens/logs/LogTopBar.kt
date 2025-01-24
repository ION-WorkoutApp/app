package com.ion606.workoutapp.screens.logs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyCalendar(
    year: Int,
    month: Int,
    workoutDays: List<ZonedDateTime>,
    onDayClick: (Pair<String, String>) -> Unit,
    modifier: Modifier,
    tooltipVal: MutableState<String>
) {
    val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        val calendarDays = (1..daysInMonth).toList()

        // filter workoutDays for the current month and year
        val workoutDaysForMonth = workoutDays.filter {
            it.year == year && it.monthValue == month
        }.map { it.dayOfMonth }

        // create rows for the month
        items(calendarDays.chunked(7)) { week ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    val isWorkoutDay = workoutDaysForMonth.contains(day)

                    Box(
                        modifier = modifier
                            .size(48.dp)
                            .background(
                                if (isWorkoutDay) {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1DB954), Color(0xFF1DB954).copy(alpha = 0.7f)
                                        )
                                    )
                                } else {
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF2C2C2C), Color(0xFF2C2C2C).copy(alpha = 0.7f)
                                        )
                                    )
                                }, shape = CircleShape
                            )
                            .clickable(
                                indication = null, // disables the ripple effect
                                interactionSource = remember { MutableInteractionSource() } // provides an empty interaction source
                            ) {
                                if (isWorkoutDay) {
                                    onDayClick(getLocalDayRangeInUTC(year, month, day))
                                } else {
                                    tooltipVal.value = "$month/$day/$year"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = if (isWorkoutDay) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeableMonthlyCalendar(
    parsedWorkoutDays: List<ZonedDateTime>, // all workout days as ZonedDateTime
    tooltipVal: MutableState<String>,
    modifier: Modifier,
    onDayClick: (Pair<String, String>) -> Unit,

) {
    val currentDate = remember { java.time.LocalDate.now() }
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue

    // calculate the total number of months from a fixed start date (e.g., January 2010) to now
    val startYear = 2010
    val startMonth = 1
    val totalMonths = (currentYear - startYear) * 12 + currentMonth - startMonth + 1

    // pager state to handle swipes
    val pagerState = rememberPagerState(
        initialPage = totalMonths - 1, // set to the current month
        pageCount = { totalMonths } // only allow swiping to previous months
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // display the current month and year
        val displayedPage = pagerState.currentPage
        val displayedDate = YearMonth.of(startYear, startMonth).plusMonths(displayedPage.toLong())
        val displayedYear = displayedDate.year

        Text(
            text = "${displayedDate.month} $displayedYear",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 0.dp)
        )

        // horizontal pager for swiping
        HorizontalPager(
            state = pagerState, modifier = Modifier.padding(top = 8.dp)
        ) { page ->
            // calculate the month/year for the given page
            val currentDisplayedDate = YearMonth.of(startYear, startMonth).plusMonths(page.toLong())
            val year = currentDisplayedDate.year
            val month = currentDisplayedDate.monthValue

            // filter workout days for the current displayed month and year
            val workoutDaysForMonth = parsedWorkoutDays.filter { workoutDay ->
                workoutDay.year == year && workoutDay.monthValue == month
            }

            // render the calendar for the current swipe index
            MonthlyCalendar(
                year = year,
                month = month,
                workoutDays = workoutDaysForMonth,
                onDayClick = onDayClick,
                modifier = modifier,
                tooltipVal = tooltipVal
            )
        }

        // add a "Today" button to animate reset to the current month
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .padding(8.dp) // outer padding
                .background(Color.Transparent, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
                .clickable {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(totalMonths - 1) // animate to the current month page
                    }
                }
                .padding(12.dp) // inner padding to create space between text and border
        ) {
            Text(
                text = "Today",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center) // center the text within the box
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun transformTime(currentMonth: Int, currentYear: Int, currentDay: Int = 1): ZonedDateTime {
    return ZonedDateTime.now().withMonth(currentMonth).withYear(currentYear)
        .withDayOfMonth(currentDay)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getLocalDayRangeInUTC(
    year: Int? = null, month: Int? = null, day: Int? = null
): Pair<String, String> {
    // get the current date and time in the local timezone
    val now = ZonedDateTime.now(ZoneId.systemDefault())

    // determine the date to use (default to current local date if not provided)
    val targetDate = if (year != null && month != null && day != null) {
        ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.systemDefault())
    } else {
        now.toLocalDate().atStartOfDay(ZoneId.systemDefault())
    }

    // calculate the start and end of the local day
    val startOfLocalDay = targetDate
    val endOfLocalDay = startOfLocalDay.plusDays(1)

    // convert both to UTC
    val startOfDayUTC = startOfLocalDay.withZoneSameInstant(ZoneOffset.UTC)
    val endOfDayUTC = endOfLocalDay.withZoneSameInstant(ZoneOffset.UTC)

    // format both as ISO 8601 strings
    val formatter = DateTimeFormatter.ISO_INSTANT
    val startOfDayISO = startOfDayUTC.format(formatter)
    val endOfDayISO = endOfDayUTC.format(formatter)

    return Pair(startOfDayISO, endOfDayISO)
}


@SuppressLint("NewApi")
fun getCurrentWeekDays(currentMonth: Int, currentYear: Int): List<Int> {
    val now = transformTime(currentMonth, currentYear, ZonedDateTime.now().dayOfMonth)
    val firstDayOfWeek = now.minusDays((now.dayOfWeek.value - 1).toLong())
    return (0..6).map { firstDayOfWeek.plusDays(it.toLong()).dayOfMonth }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogTopBar(
    workoutDays: List<ZonedDateTime>, onDayClick: (Pair<String, String>) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currentMonth = remember { ZonedDateTime.now().monthValue }
    val currentYear = remember { ZonedDateTime.now().year }
    val tooltipVal = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(top = 16.dp)
    ) {
        // week view
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121212))
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            isExpanded = (dragAmount > 0)
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val currentWeek = getCurrentWeekDays(currentMonth, currentYear)

            currentWeek.forEach { day ->
                // check if any workoutDay matches the current day, month, and year
                val isWorkoutDay = workoutDays.any { workoutDay ->
                    workoutDay.dayOfMonth == day && workoutDay.monthValue == currentMonth && workoutDay.year == currentYear
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isWorkoutDay) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1DB954), Color(0xFF1DB954).copy(alpha = 0.7f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2C2C2C), Color(0xFF2C2C2C).copy(alpha = 0.7f)
                                    )
                                )
                            }, shape = CircleShape
                        )
                        .clickable {
                            if (isWorkoutDay) {
                                onDayClick(
                                    getLocalDayRangeInUTC(
                                        currentYear, currentMonth, day
                                    )
                                )
                            }
                            else {
                                tooltipVal.value = "$currentMonth/$day/$currentYear"
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = if (isWorkoutDay) Color.Black else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // expand/collapse button
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse Calendar" else "Expand Calendar",
                tint = Color.White
            )
        }

        // expanded calendar view
        AnimatedVisibility(visible = isExpanded) {
            SwipeableMonthlyCalendar(
                workoutDays,
                tooltipVal,
                Modifier.pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            isExpanded = (dragAmount > 0)
                        }
                    )
                },
            ) {
                onDayClick(it)
                isExpanded = false
            }
        }

        // show the tooltip if needed
        if (tooltipVal.value.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { tooltipVal.value = "" }
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Gray, shape = CircleShape)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "No workout found for ${tooltipVal.value}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}