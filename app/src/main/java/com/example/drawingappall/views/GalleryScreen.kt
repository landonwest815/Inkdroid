package com.example.drawingappall.views

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.viewModels.DrawingFileViewModel
import com.example.drawingappall.viewModels.DrawingViewModelProvider
import com.example.drawingappall.viewModels.SocialViewModel
import com.example.drawingappall.viewModels.SocialViewModelProvider
import java.io.File


@Composable
fun GalleryScreen(
    dvm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    svm: SocialViewModel = viewModel(factory = SocialViewModelProvider .Factory),
    navController: NavController
) {
    var localImages by remember { mutableStateOf<Boolean>(true) } // Toggle between "My Images" and "Uploaded Images"

    Scaffold(
        // Floating action button to add a new drawing
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val drawing = dvm.createFile("Drawing_${System.currentTimeMillis()}")
                    val encodedFilePath = Uri.encode(drawing.filePath)
                    navController.navigate("draw/${encodedFilePath}/${drawing.fileName}")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Drawing")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE))
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header with toggle
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (localImages) "My Drawings" else "Uploaded Images",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                // Toggle Button
                IconButton(onClick = {
                    localImages = !localImages
                    svm.fetchFiles()
                }) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Toggle Images")
                }

                // Logout Button
                IconButton(onClick = {
                    svm.logout()
                    navController.navigate("splash") {
                        popUpTo("gallery") { inclusive = true }
                    }
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Logout", tint = Color.Red)
                }
            }

            // Observe and display list of drawings
            val list by if (localImages) dvm.drawings.collectAsState(emptyList()) else dvm.serverDrawings.collectAsState(emptyList())

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                for (file in list.asReversed()) {
                    item {
                        DrawingFileCard(
                            file,
                            navController,
                            dvm,
                            svm,
                            localImages
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawingFileCard(
    file: Drawing,
    navController: NavController,
    dvm: DrawingFileViewModel,
    svm: SocialViewModel,
    localImages: Boolean,
    modifier: Modifier = Modifier
) {
    var isDownloaded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Card(
            modifier = Modifier.aspectRatio(1f) // enforce square aspect ratio
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val encodedFilePath = Uri.encode(file.filePath)
                        navController.navigate("draw/${encodedFilePath}/${file.fileName}")
                    }
                    .testTag("DrawingCard_${file.fileName}")
                    .paint(
                        painter = rememberAsyncImagePainter(
                            model = File(file.filePath, file.fileName).absolutePath
                        )
                    )
            ) {
                // Delete icon
                IconButton(
                    onClick = { dvm.deleteFile(file) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp)
                        .testTag("Delete_${file.fileName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }

                if (localImages) {
                    // Upload button for local images
                    IconButton(
                        onClick = { svm.uploadFile(file) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(0.dp)
                            .testTag("UploadButton_${file.fileName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Upload"
                        )
                    }
                } else {
                    // Download button for non-local images
                    IconButton(
                        onClick =
                            {
                                svm.downloadFile(file.toString())
                                isDownloaded = true // can implement !isDownloaded and un-downloading a file
                            },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(0.dp)
                            .testTag("DownloadButton_${file.fileName}")
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Download"
                        )
                    }
                }
            }
        }

        // Filename displayed under the card
        Text(
            text = file.fileName,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .testTag("FileName_${file.fileName}")
        )
    }
}
