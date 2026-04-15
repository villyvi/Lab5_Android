package com.example.sensorgforceapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.graphics.Color

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.Description

import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var xText: TextView
    private lateinit var yText: TextView
    private lateinit var zText: TextView
    private lateinit var gText: TextView
    private lateinit var maxGText: TextView
    private lateinit var resetBtn: Button

    private lateinit var chart: LineChart
    private lateinit var dataSet: LineDataSet
    private lateinit var lineData: LineData

    private var index = 0f
    private var maxGValue = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        xText = findViewById(R.id.xValue)
        yText = findViewById(R.id.yValue)
        zText = findViewById(R.id.zValue)
        gText = findViewById(R.id.gForce)
        maxGText = findViewById(R.id.maxG)
        resetBtn = findViewById(R.id.resetBtn)
        chart = findViewById(R.id.chart)

        setupChart()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        resetBtn.setOnClickListener {
            maxGValue = 0.0
            maxGText.text = "Max G: 0.000"
        }
        if (savedInstanceState != null) {
            maxGValue = savedInstanceState.getDouble("MAX_G", 0.0)
            maxGText.text = "Max G: %.6f".format(maxGValue)
        }
    }

    private fun setupChart() {
        dataSet = LineDataSet(ArrayList(), "G-Force")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        lineData = LineData(dataSet)
        chart.data = lineData
        val description = Description()
        description.text = "Real-time G-Force"
        chart.description = description
        chart.axisRight.isEnabled = false
        chart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("MAX_G", maxGValue)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        xText.text = "X: %.2f".format(x)
        yText.text = "Y: %.2f".format(y)
        zText.text = "Z: %.2f".format(z)

        val gForce = sqrt((x * x + y * y + z * z).toDouble()) / 9.80665
        gText.text = "G: %.6f".format(gForce)

        if (gForce > maxGValue) {
            maxGValue = gForce
            maxGText.text = "Max G: %.6f".format(maxGValue)
        }

        addEntry(gForce.toFloat())
    }

    private fun addEntry(value: Float) {
        dataSet.addEntry(Entry(index, value))
        index++
        lineData.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.setVisibleXRangeMaximum(60f)
        chart.moveViewToX(index)
        chart.invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}