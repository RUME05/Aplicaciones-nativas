package com.example.herramientasnativasapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnIniciarDetener: Button
    private lateinit var tvPasos: TextView
    private lateinit var tvUbicacion: TextView

    private var isServiceRunning = false
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnIniciarDetener = findViewById(R.id.btnIniciarDetener)
        tvPasos = findViewById(R.id.tvPasos)
        tvUbicacion = findViewById(R.id.tvUbicacion)

        btnIniciarDetener.setOnClickListener {
            if (isServiceRunning) {
                stopFitnessService()
            } else {
                checkPermissionsAndStartService()
            }
        }
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

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startFitnessService()
        }
    }

    private fun startFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        btnIniciarDetener.text = "Detener Seguimiento"
        isServiceRunning = true
        Toast.makeText(this, "Seguimiento iniciado.", Toast.LENGTH_SHORT).show()
    }

    private fun stopFitnessService() {
        val serviceIntent = Intent(this, FitnessService::class.java)
        stopService(serviceIntent)
        btnIniciarDetener.text = "Iniciar Seguimiento"
        isServiceRunning = false
        Toast.makeText(this, "Seguimiento detenido.", Toast.LENGTH_SHORT).show()
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