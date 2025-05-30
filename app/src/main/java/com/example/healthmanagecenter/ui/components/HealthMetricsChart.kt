package com.example.healthmanagecenter.ui.components

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsOverviewCard(latestRecord: HealthRecordEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Latest Health Data Overview", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            LatestHealthMetricsBarChart(latestRecord = latestRecord)
        }
    }
}

@Composable
fun LatestHealthMetricsBarChart(latestRecord: HealthRecordEntity?) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Adjusted height
        factory = { context ->
            BarChart(context).apply {
                // Basic chart setup
                description.isEnabled = false
                setPinchZoom(false)
                setTouchEnabled(false) // Disable touch for a simple overview
                setDragEnabled(false)
                setScaleEnabled(false)

                // X-axis setup (Metrics)
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(listOf("Weight", "Heart Rate", "Systolic", "Diastolic", "Sleep"))
                    labelRotationAngle = 0f
                    setCenterAxisLabels(true)
                }

                // Left Y-axis setup
                axisLeft.apply {
                    setDrawGridLines(false)
                    axisMinimum = 0f
                }

                // Right Y-axis setup (Disabled)
                axisRight.isEnabled = false

                // Legend setup
                legend.isEnabled = false // Hide legend for simplicity

                setNoDataText("No health data available")
            }
        },
        update = { chart ->
            if (latestRecord == null) {
                chart.data = null
                chart.invalidate()
                return@AndroidView
            }

            val entries = listOf(
                BarEntry(0f, latestRecord.weight ?: 0f), // Weight
                BarEntry(1f, latestRecord.heartRate?.toFloat() ?: 0f), // Heart Rate
                BarEntry(2f, latestRecord.bloodPressureHigh?.toFloat() ?: 0f), // Systolic BP
                BarEntry(3f, latestRecord.bloodPressureLow?.toFloat() ?: 0f), // Diastolic BP
                BarEntry(4f, latestRecord.sleepHours ?: 0f) // Sleep Hours
            )

            val dataSet = BarDataSet(entries, "Latest Data").apply {
                colors = listOf(
                    Color.Blue.toArgb(),
                    Color.Red.toArgb(),
                    Color.Green.toArgb(),
                    Color.Magenta.toArgb(),
                    Color.Cyan.toArgb()
                )
                setDrawValues(true) // Show value on top of bars
                valueTextSize = 10f
                valueTextColor = Color.Black.toArgb()
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.5f // Adjust bar width

            chart.data = barData
            chart.setFitBars(true) // Make bars fit to the available space
            chart.invalidate()
        }
    )
}