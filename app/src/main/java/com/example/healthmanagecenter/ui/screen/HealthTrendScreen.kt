package com.example.healthmanagecenter.ui.screen

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.dao.HealthRecordDao
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.viewmodel.HealthRecordViewModel // We will reuse this ViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendScreen(userId: Long, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val viewModel: HealthRecordViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Reuse HealthRecordViewModel, but possibly extend or create a new one if logic diverges significantly
            return HealthRecordViewModel(context as Application, userId) as T
        }
    })
    val recentRecords by viewModel.recent30DaysHealthRecords.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Data Trends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Health Data Trend Analysis", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("The following charts show your recent health data trends and analysis reports.",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            // Individual Metric Charts and Analysis
            WeightLineChartCard(recentRecords)
            Spacer(Modifier.height(16.dp))

            HeightDisplayCard(recentRecords)
            Spacer(Modifier.height(16.dp))

            HeartRateLineChartCard(recentRecords)
            Spacer(Modifier.height(16.dp))

            SleepHoursBarChartCard(recentRecords)
            Spacer(Modifier.height(16.dp))

            TrendAnalysisCard(recentRecords)
        }
    }
}

// Move the chart and analysis composables here from HealthMetricsChart.kt
// (Copy the code for WeightLineChartCard, HeightDisplayCard, HeartRateLineChartCard, SleepHoursBarChartCard, TrendAnalysisCard)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLineChartCard(records: List<HealthRecordEntity>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weight (kg)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Weight Line Chart using AndroidView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    LineChart(context).apply {
                        // Chart setup
                        description.isEnabled = false
                        legend.isEnabled = true
                        setTouchEnabled(true)
                        setDragEnabled(true)
                        setScaleEnabled(true)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelRotationAngle = -45f
                        }

                        axisLeft.apply { setDrawGridLines(false) }
                        axisRight.isEnabled = false
                        setNoDataText("No weight data available")
                    }
                },
                update = { chart ->
                    if (records.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }

                    // Sort records by timestamp to ensure correct date order on X-axis
                    val sortedRecords = records.sortedBy { it.timestamp }

                    val weightEntries = sortedRecords.mapIndexed { index, record ->
                         Entry(index.toFloat(), record.weight ?: 0f) // Use index as x-value
                    }

                    val weightDataSet = LineDataSet(weightEntries, "Weight").apply {
                        color = Color.Blue.toArgb()
                        setCircleColor(Color.Blue.toArgb())
                        setDrawValues(false)
                    }

                    val lineData = LineData(weightDataSet)
                    chart.data = lineData

                    // Set X-axis value formatter to display dates
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedRecords.map { SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(it.timestamp)) })

                    chart.invalidate()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeightDisplayCard(records: List<HealthRecordEntity>) {
     val latestHeight = records.maxByOrNull { it.timestamp }?.height

     Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Height (cm)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(latestHeight?.toString() ?: "N/A", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateLineChartCard(records: List<HealthRecordEntity>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Heart Rate (bpm)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Heart Rate Line Chart using AndroidView
             AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    LineChart(context).apply {
                        // Chart setup
                        description.isEnabled = false
                        legend.isEnabled = true
                        setTouchEnabled(true)
                        setDragEnabled(true)
                        setScaleEnabled(true)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelRotationAngle = -45f
                        }

                        axisLeft.apply { setDrawGridLines(false) }
                        axisRight.isEnabled = false
                        setNoDataText("No heart rate data available")
                    }
                },
                update = { chart ->
                    if (records.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }

                    // Sort records by timestamp to ensure correct date order on X-axis
                    val sortedRecords = records.sortedBy { it.timestamp }

                    val heartRateEntries = sortedRecords.mapIndexed { index, record ->
                        Entry(index.toFloat(), record.heartRate?.toFloat() ?: 0f)
                    }

                    val heartRateDataSet = LineDataSet(heartRateEntries, "Heart Rate").apply {
                        color = Color.Red.toArgb()
                        setCircleColor(Color.Red.toArgb())
                        setDrawValues(false)
                    }

                    val lineData = LineData(heartRateDataSet)
                    chart.data = lineData

                    // Set X-axis value formatter to display dates
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedRecords.map { SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(it.timestamp)) })

                    chart.invalidate()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepHoursBarChartCard(records: List<HealthRecordEntity>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sleep Duration (hours)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Sleep Hours Bar Chart using AndroidView
             AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    BarChart(context).apply {
                        // Chart setup
                        description.isEnabled = false
                        legend.isEnabled = true
                        setTouchEnabled(true)
                        setDragEnabled(true)
                        setScaleEnabled(true)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelRotationAngle = -45f
                        }

                        axisLeft.apply { setDrawGridLines(false) }
                        axisRight.isEnabled = false
                        setNoDataText("No sleep hours data available")
                    }
                },
                update = { chart ->
                    if (records.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }

                    // Sort records by timestamp to ensure correct date order on X-axis
                    val sortedRecords = records.sortedBy { it.timestamp }

                    val sleepHoursEntries = sortedRecords.mapIndexed { index, record ->
                        BarEntry(index.toFloat(), record.sleepHours ?: 0f)
                    }

                    val sleepHoursDataSet = BarDataSet(sleepHoursEntries, "Sleep Hours").apply {
                        color = Color.Cyan.toArgb()
                    }

                    val barData = BarData(sleepHoursDataSet)
                    chart.data = barData

                    // Set X-axis value formatter to display dates
                     chart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedRecords.map { SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(it.timestamp)) })

                    chart.invalidate()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendAnalysisCard(records: List<HealthRecordEntity>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trend Analysis", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (records.size < 2) {
                Text("Insufficient data for trend analysis.", style = MaterialTheme.typography.bodyMedium)
            } else {
                // Simple Trend Analysis
                val sortedRecords = records.sortedBy { it.timestamp }
                val latestRecord = sortedRecords.last()
                val previousRecord = sortedRecords[records.size - 2]

                val analysisResult = buildString { // Capture the result here
                    append("Recent Health Data Overview:\n")

                    latestRecord.weight?.let { latestWeight ->
                        previousRecord.weight?.let { previousWeight ->
                            if (latestWeight > previousWeight) {
                                append("Weight has slightly increased, please watch your diet.\n")
                            } else { // Added back the specific check for decrease
                                append("Weight has slightly decreased, please maintain.\n")
                            }
                        }
                    }

                     latestRecord.heartRate?.let { latestHeartRate ->
                        previousRecord.heartRate?.let { previousHeartRate ->
                            if (latestHeartRate > previousHeartRate) {
                                append("Heart rate has slightly increased, please rest more.\n")
                            } else{ // Added back the specific check for decrease
                                append("Heart rate has slightly decreased, please continue to maintain.\n")
                            }
                        }
                    }

                    latestRecord.sleepHours?.let { latestSleepHours ->
                        previousRecord.sleepHours?.let { previousSleepHours ->
                            if (latestSleepHours < previousSleepHours) {
                                append("Sleep duration has slightly decreased, please rest more.\n")
                            } else{ // Added back the specific check for increase
                                append("Sleep duration has slightly increased, please maintain good sleep habits.\n")
                            }
                        }
                    }

                    // Add more analysis for blood pressure if needed
                     latestRecord.bloodPressureHigh?.let { latestHigh ->
                        latestRecord.bloodPressureLow?.let { latestLow ->
                            previousRecord.bloodPressureHigh?.let { previousHigh ->
                                previousRecord.bloodPressureLow?.let { previousLow ->
                                    if ((latestHigh > previousHigh && latestLow > previousLow) || latestHigh > 140 || latestLow > 90) {
                                        append("Blood pressure may be slightly high, please monitor closely.\n")
                                    } else { // Added back the specific check for decrease
                                        append("Blood pressure may be slightly low, please monitor closely.\n")
                                    }
                                }
                            }
                        }
                    }
                }

                // Check the captured result
                if (analysisResult.trim() == "Recent Health Data Overview:") { // Check if only the header is present
                     Text("No significant health data trend detected.", style = MaterialTheme.typography.bodyMedium)
                } else {
                     Text(analysisResult.trim(), style = MaterialTheme.typography.bodyMedium) // Trim to remove leading/trailing whitespace
                }
            }
        }
    }
} 