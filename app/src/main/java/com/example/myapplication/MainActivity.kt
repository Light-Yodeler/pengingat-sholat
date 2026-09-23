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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        enableEdgeToEdge()
        setContent {
            NoorWaktuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SurfaceParchment
                ) {
                    NoorWaktuMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndFetchInitialGps()
    }
}