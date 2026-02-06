package org.umoja4life.drilltutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.umoja4life.drilltutor.ui.theme.DrillTutorTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrillTutorTheme {
                // Get the ViewModel (Scoped to this Activity)
                val viewModel: DrillViewModel = viewModel()

                // Pass it to the screen
                MainScreen(viewModel)
            }
        }
    }
}
