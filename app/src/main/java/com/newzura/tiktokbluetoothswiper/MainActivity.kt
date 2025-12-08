package com.newzura.tiktokbluetoothswiper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.newzura.tiktokbluetoothswiper.ui.theme.TiktokBluetoothSwiperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiktokBluetoothSwiperTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val lastKeyCode = remember { mutableStateOf<Int?>(null) }
    val lastKeyName = remember { mutableStateOf<String?>(null) }
    val isDebugMode = remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AppConstants.ACTION_KEY_EVENT) {
                    lastKeyCode.value = intent.getIntExtra(AppConstants.EXTRA_KEY_CODE, -1)
                    lastKeyName.value = intent.getStringExtra(AppConstants.EXTRA_KEY_NAME)
                }
            }
        }
        val filter = IntentFilter(AppConstants.ACTION_KEY_EVENT)
        val receiverFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.RECEIVER_NOT_EXPORTED
        } else {
            0
        }
        ContextCompat.registerReceiver(context, receiver, filter, receiverFlags)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1a1a1a)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.raven_portrait),
                contentDescription = "Raven portrait Background",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f),
                contentScale = ContentScale.Fit
            )

            if (isDebugMode.value) {
                DebugScreen(
                    lastKeyCode = lastKeyCode.value,
                    lastKeyName = lastKeyName.value,
                    onBack = { isDebugMode.value = false }
                )
            } else {
                MainMenuScreen(
                    lastKeyCode = lastKeyCode.value,
                    lastKeyName = lastKeyName.value,
                    onDebugClick = { isDebugMode.value = true },
                    context = context
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    lastKeyCode: Int?,
    lastKeyName: String?,
    onDebugClick: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Contrôleur TikTok",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }) {
            Text("Ouvrir les paramètres d'accessibilité")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDebugClick) {
            Text("Mode Debug")
        }
    }
}

@Composable
fun DebugScreen(
    lastKeyCode: Int?,
    lastKeyName: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Mode Debug",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Appuie sur les boutons de ton casque",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = Color(0xFF2a2a2a)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dernière commande :", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (lastKeyName != null) "Bouton : $lastKeyName" else "En attente...",
                    fontSize = 18.sp,
                    color = Color.Cyan
                )
                Text(
                    text = if (lastKeyCode != null && lastKeyCode != -1) "Code : $lastKeyCode" else "",
                    fontSize = 16.sp,
                    color = Color.Cyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = Color(0xFF2a2a2a)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Commandes configurées :", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Swipe UP :", fontWeight = FontWeight.SemiBold, color = Color.Cyan)
                Text("• Volume Down (25)", fontSize = 14.sp, color = Color.Gray)
                Text("• Media Next (87)", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Swipe DOWN :", fontWeight = FontWeight.SemiBold, color = Color.Magenta)
                Text("• Volume Up (24)", fontSize = 14.sp, color = Color.Gray)
                Text("• Media Previous (88)", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Play/Pause :", fontWeight = FontWeight.SemiBold, color = Color.Yellow)
                Text("• Media Play/Pause (85)", fontSize = 14.sp, color = Color.Gray)
                Text("• Media Play (126)", fontSize = 14.sp, color = Color.Gray)
                Text("• Media Pause (127)", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Retour")
        }
    }
}
