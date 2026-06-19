package com.interview.logviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.interview.logviewer.presentation.LogViewerScreen
import com.interview.logviewer.presentation.LogViewerViewModel
import com.interview.logviewer.ui.theme.LogViewerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LogViewerTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val viewModel: LogViewerViewModel = hiltViewModel()
                        LogViewerScreen(viewModel = viewModel)
                    }
                }
            }
        }

    }
}
