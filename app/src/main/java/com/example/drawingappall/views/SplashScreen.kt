package com.example.drawingappall.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Splash screen handling user login/registration before navigating to the gallery.
 */
@Composable
fun SplashScreen(
    onNavigateToGallery: () -> Unit,
    viewModel: SocialViewModel = viewModel(factory = SocialViewModelProvider.Factory)
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val error by viewModel.authError.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Automatically navigates to gallery when login or registration succeeds
    LaunchedEffect(Unit) {
        snapshotFlow { isAuthenticated }
            .collectLatest { authenticated ->
                if (authenticated) onNavigateToGallery()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(id = R.drawable.painticon),
                contentDescription = "Paint Icon",
                modifier = Modifier.size(100.dp)
            )

            Text(
                text = "drawALL",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Andy Chadwick\nLandon Evans\nLandon West",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLoginMode) "Log In" else "Register",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (isLoginMode) {
                            viewModel.login(username, password)
                        } else {
                            viewModel.register(username, password)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(if (isLoginMode) "Log In" else "Register")
            }

            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(
                    if (isLoginMode)
                        "Need an account? Register"
                    else
                        "Have an account? Log in"
                )
            }

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToGallery = {})
}
