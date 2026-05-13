package com.shreyas.kreedaankana.features.auth.ui

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.auth.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val activity = requireNotNull(LocalActivity.current) { "Activity context is required for Google Sign-In" }
    val googleClient = remember { AuthManager.getGoogleClient(activity) }
    val authState by viewModel.authState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignIn(result.data)
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let {
            onError(it)
            viewModel.resetState()
        }
    }

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            viewModel.resetState()
            onSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7F8)) // 🔹 Standard App Background
    ) {
        // 🔹 HERO HEADER (Dark Green Curved Background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    color = Color(0xFF004D40),
                    shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF00E676), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "KreedaAnkana",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Ultimate Sports Arena",
                    color = Color(0xFFC6FF00), // Lime Accent
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 🔹 FLOATING LOGIN CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .offset(y = 40.dp), // Pushes the card down to overlap the green header
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sign in to manage your teams, post challenges, and dominate the leaderboard.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 🔹 MODERN GOOGLE BUTTON
                OutlinedButton(
                    onClick = { launcher.launch(googleClient.signInIntent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    enabled = !authState.isLoading
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00E676),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Signing in...", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    } else {
                        // 🔹 Simple generic 'G' graphic without needing an external image asset
                        Text("G", color = Color(0xFF1976D2), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Continue with Google", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // 🔹 FOOTER TEXT
        Text(
            text = "By continuing, you agree to our Terms & Privacy Policy.",
            color = Color.Gray,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )
    }
}