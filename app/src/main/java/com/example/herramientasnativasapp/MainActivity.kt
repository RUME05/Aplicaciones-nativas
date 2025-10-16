package com.example.herramientasnativasapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.herramientasnativasapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

private const val PREFS_THEME = "ThemePrefs"
private const val KEY_THEME_MODE = "ThemeMode" // Solo guardaremos el modo

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var themePrefs: SharedPreferences

    private var isServiceRunning = false
    private val PERMISSION_REQUEST_CODE = 101

    // El receptor para los datos del servicio de fitness (sin cambios)
    private val fitnessUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FitnessService.ACTION_BROADCAST) {
                val steps = intent.getIntExtra(FitnessService.EXTRA_STEPS, 0)
                binding.tvPasos.text = "Pasos hoy: $steps"

                val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(FitnessService.EXTRA_LOCATION, Location::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(FitnessService.EXTRA_LOCATION)
                }

                if (location != null) {
                    binding.tvUbicacion.text = "Lat: %.4f, Lon: %.4f".format(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicamos el tema ANTES de crear la vista
        themePrefs = getSharedPreferences(PREFS_THEME, Context.MODE_PRIVATE)
        applyTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Lógica del Switch para el Tema ---
        setupThemeSwitch()

        // --- Lógica del Botón de Seguimiento (sin cambios) ---
        binding.btnIniciarDetener.setOnClickListener {
            if (isServiceRunning) {
                stopFitnessService()
            } else {
                checkPermissionsAndStartService()
            }
        }
        updateUIWithSavedSteps()
    }

    private fun applyTheme() {
        // Mantenemos el color Guinda por defecto
        setTheme(R.style.Theme_App_Guinda)

        // Leemos el modo guardado (Claro/Oscuro) y lo aplicamos
        val themeMode = themePrefs.getString(KEY_THEME_MODE, "SYSTEM")
        when (themeMode) {
            "LIGHT" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "DARK" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "SYSTEM" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupThemeSwitch() {
        // 1. Establecer el estado inicial del Switch
        // Comprobamos si el modo actual de la app es oscuro
        val isNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        binding.themeSwitch.isChecked = isNightMode

        // 2. Escuchar los cambios en el Switch
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si se activa, aplicamos modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                themePrefs.edit().putString(KEY_THEME_MODE, "DARK").apply()
            } else {
                // Si se desactiva, aplicamos modo claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                themePrefs.edit().putString(KEY_THEME_MODE, "LIGHT").apply()
            }
        }
    }

    // --- El resto del código se mantiene igual ---

    private fun updateUIWithSavedSteps() {
        val sharedPreferences = getSharedPreferences(FitnessService.PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = sharedPreferences.getString(FitnessService.KEY_DATE, "")
        val currentDate = getCurrentDate()
        if (savedDate == currentDate) {
            val stepsToday = sharedPreferences.getInt(FitnessService.KEY_STEPS_TODAY, 0)
            binding.tvPasos.text = "Pasos hoy: $stepsToday"
        } else {
            binding.tvPasos.text = "Pasos hoy: 0"
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            fitnessUpdateReceiver, IntentFilter(FitnessService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fitnessUpdateReceiver)
    }

    private fun checkPermissionsAndStartService() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startFitnessService()
        }
    }

    private fun startFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        binding.btnIniciarDetener.text = "Detener Seguimiento"
        isServiceRunning = true
        Toast.makeText(this, "Seguimiento iniciado.", Toast.LENGTH_SHORT).show()
    }

    private fun stopFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        stopService(serviceIntent)
        binding.btnIniciarDetener.text = "Iniciar Seguimiento"
        isServiceRunning = false
        Toast.makeText(this, "Seguimiento detenido.", Toast.LENGTH_SHORT).show()
        updateUIWithSavedSteps()
        binding.tvUbicacion.text = "Ubicación: Esperando..."
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startFitnessService()
            } else {
                Toast.makeText(this, "Se requieren todos los permisos para iniciar el seguimiento.", Toast.LENGTH_LONG).show()
            }
        }
    }
}