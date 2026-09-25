package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.NoorWaktuMainScreen
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.NoorWaktuTheme
import com.example.myapplication.ui.theme.SurfaceParchment

class MainActivity : ComponentActivity() {

    private val viewModel: NoorWaktuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            NoorWaktuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceParchment
                ) {
                    NoorWaktuMainScreen(
                        viewModel = viewModel,
                        onRequestNotificationPermission = { requestNotificationPermissionIfNeeded() }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.refreshPermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndFetchInitialGps()
        viewModel.refreshPermissionStatus()
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (com.example.myapplication.core.alarm.AzanBroadcastReceiver.isAzanPlaying.value &&
            event.action == android.view.KeyEvent.ACTION_DOWN
        ) {
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_VOLUME_DOWN,
                android.view.KeyEvent.KEYCODE_VOLUME_UP,
                android.view.KeyEvent.KEYCODE_VOLUME_MUTE -> {
                    com.example.myapplication.core.alarm.AzanBroadcastReceiver.stopActiveAzan(this)
                    android.widget.Toast.makeText(this, "Suara azan dihentikan", android.widget.Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}