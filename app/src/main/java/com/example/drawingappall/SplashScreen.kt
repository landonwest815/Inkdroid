package com.example.drawingappall

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreen(onNavigateToGallery: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // paint icon
            Image(
                painter = painterResource(id = R.drawable.painticon),
                contentDescription = "Paint Icon",
                modifier = Modifier.size(100.dp)
            )

            // welcome prompt
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

            // project authors
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
                    .padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // button to navigate to the draw screen
            Button(
                onClick = onNavigateToGallery,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp) // tighter padding
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Let's Draw",
                        color = Color.Black,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // reduce gap
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Arrow Right",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp) // optional: shrink icon if needed
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        onNavigateToGallery = {}
    )
}