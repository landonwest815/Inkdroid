package com.example.drawingappall.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingappall.R
import com.example.drawingappall.viewModels.SocialViewModel
import com.example.drawingappall.viewModels.SocialViewModelProvider
import kotlinx.coroutines.launch

/**
 * Splash screen for login or registration.
 * Automatically navigates to gallery when authenticated.
 */
@Composable
fun SplashScreen(
    onNavigateToGallery: () -> Unit,
    viewModel: SocialViewModel = viewModel(factory = SocialViewModelProvider.Factory)
) {
    // UI state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // ViewModel state
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val authError by viewModel.authError.collectAsState()

    // Navigate when authentication succeeds
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onNavigateToGallery()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App icon and title
            Image(
                painter = painterResource(R.drawable.painticon),
                contentDescription = "Paint Icon",
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "drawALL",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            // Team credits
            Text(
                text = "Andy Chadwick\nLandon Evans\nLandon West",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Submit button
            Button(
                onClick = {
                    scope.launch {
                        if (isLoginMode) viewModel.login(username, password)
                        else viewModel.register(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(if (isLoginMode) "Log In" else "Register")
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Toggle between modes
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(
                    text = if (isLoginMode)
                        "Need an account? Register"
                    else
                        "Have an account? Log in"
                )
            }

            // Error message
            authError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onNavigateToGallery = {})
}
