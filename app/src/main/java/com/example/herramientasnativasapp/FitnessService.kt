package com.example.herramientasnativasapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class FitnessService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1f
    private var totalStepsToday = 0
    private var latestAccumulatedSteps = 0 // Variable para el último valor correcto

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "FitnessServiceChannel"
        const val ACTION_BROADCAST = "com.example.herramientasnativasapp.FITNESS_UPDATE"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_LOCATION = "extra_location"
        const val PREFS_NAME = "FitnessPrefs"
        const val KEY_STEPS_TODAY = "steps_today"
        const val KEY_DATE = "date"
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupStepCounter()
        setupLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        loadDailySteps()
        createNotificationChannel()
        val notification = createNotification("Seguimiento activo...", "Iniciando...")
        startForeground(1, notification)
        startStepCounting()
        startLocationUpdates()
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsFromSensor = event.values[0]

            if (initialSteps == -1f) {
                initialSteps = totalStepsFromSensor
                loadDailySteps()
            }

            val stepsThisSession = (totalStepsFromSensor - initialSteps).toInt()
            latestAccumulatedSteps = totalStepsToday + stepsThisSession

            broadcastUpdate(latestAccumulatedSteps, lastLocation)
            saveDailySteps(latestAccumulatedSteps)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveDailySteps(latestAccumulatedSteps)
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    lastLocation = location
                    broadcastUpdate(latestAccumulatedSteps, lastLocation)
                }
            }
        }
    }

    private fun saveDailySteps(steps: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_STEPS_TODAY, steps)
        editor.putString(KEY_DATE, getCurrentDate())
        editor.apply()
    }

    private fun loadDailySteps() {
        val savedDate = sharedPreferences.getString(KEY_DATE, "")
        val currentDate = getCurrentDate()

        if (savedDate == currentDate) {
            totalStepsToday = sharedPreferences.getInt(KEY_STEPS_TODAY, 0)
        } else {
            totalStepsToday = 0
            initialSteps = -1f
            saveDailySteps(0)
        }
        latestAccumulatedSteps = totalStepsToday
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun broadcastUpdate(steps: Int, location: Location?) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_STEPS, steps)
        if (location != null) {
            intent.putExtra(EXTRA_LOCATION, location)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        val locationText = if (location != null) "Lat: %.4f".format(location.latitude) else "Buscando..."
        updateNotification("$steps pasos | $locationText")
    }

    private fun createNotification(title: String, text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification("Seguimiento activo...", text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun setupStepCounter() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Log.e("FitnessService", "El dispositivo no tiene sensor de pasos.")
        }
    }

    private fun startStepCounting() {
        stepSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (unlikely: SecurityException) {
            Log.e("FitnessService", "Permiso de ubicación perdido. $unlikely")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal de Servicio de Fitness",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
        }
    }
}