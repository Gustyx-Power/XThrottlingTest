package id.xms.xtrt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import id.xms.xtrt.ui.screen.ThrottlingTestScreen
import id.xms.xtrt.ui.theme.XThrottlingTestTheme
import id.xms.xtrt.util.CpuInfoProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CpuInfoProvider.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            XThrottlingTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThrottlingTestScreen()
                }
            }
        }
    }
}
