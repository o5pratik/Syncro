package com.example.syncro

import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncro.ui.theme.SyncroTheme
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.provider.OpenableColumns
import android.util.Log


class MainActivity : ComponentActivity() {
    private var selectedFileBytes: ByteArray? = null
    private val filePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)!!
                val fileBytes = inputStream.readBytes()
                inputStream.close()

                // 🔑 Extract filename
                val fileName = FileUtils.getFileNameFromUri(it, this)


                LocalSender.sendFile(
                    receiverIp = "172.16.46.242", // receiver phone IP
                    fileName = fileName,
                    fileBytes = fileBytes
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ RECEIVER STARTS ONCE HERE
        LocalReceiver.startReceiving { fileName, fileBytes ->
            FileUtils.saveFile(this, fileName, fileBytes)
            Log.d("Syncro", "File received: $fileName")
        }
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var screen by remember { mutableStateOf("start") }

            val backgroundColor = if (isDarkMode) Color(0xFF0F172A) else Color.White
            val textColor = if (isDarkMode) Color.White else Color.Black

            SyncroTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    when (screen) {
                        "start" -> StartScreen(
                            isDarkMode = isDarkMode,
                            textColor = textColor,
                            onToggleTheme = { isDarkMode = !isDarkMode },
                            onSend = { screen = "send" },
                            onReceive = { screen = "receive" }
                        )

                        "send" -> SendOptionsScreen(
                            textColor = textColor,
                            onBack = { screen = "start" },
                            onNearby = {
                                filePicker.launch("*/*")
                            },
                            onOnline = {
                                // later
                            }
                        )


                        "receive" -> {
                            ReceiveScreen(
                                textColor = textColor,
                                onBack = { screen = "start" }
                            )


                        }

                    }
                }
            }
        }
    }
}

@Composable
fun StartScreen(
    isDarkMode: Boolean,
    textColor: Color,
    onToggleTheme: () -> Unit,
    onSend: () -> Unit,
    onReceive: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Syncro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkMode)
                        Icons.Default.LightMode
                    else
                        Icons.Default.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = textColor
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "What would you like to do?",
                fontSize = 16.sp,
                color = textColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionIcon(
                    icon = Icons.Default.Send,
                    label = "Send",
                    color = textColor,
                    onClick = onSend
                )

                ActionIcon(
                    icon = Icons.Default.Download,
                    label = "Receive",
                    color = textColor,
                    onClick = onReceive
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable

fun SendOptionsScreen(
    textColor: Color,
    onBack: () -> Unit,
    onNearby: () -> Unit,
    onOnline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Send with Syncro",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose a transfer method",
            fontSize = 16.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNearby,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nearby Transfer")
                Text(
                    "Fast, local sharing over Wi-Fi",
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onOnline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Online Transfer")
                Text(
                    "Secure sharing over the internet",
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}


@Composable
fun ReceiveScreen(
    textColor: Color,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = "Receive",
            tint = textColor,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Waiting for nearby sender…",
            fontSize = 18.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}


@Composable
fun ActionIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(label, color = color)
    }
}
