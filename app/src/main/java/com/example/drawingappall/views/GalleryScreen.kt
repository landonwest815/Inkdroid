package com.example.drawingappall.views

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.viewModels.*

import java.io.File

/**
 * Displays the gallery of either local or server-based drawings.
 */
@Composable
fun GalleryScreen(
    dvm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    svm: SocialViewModel = viewModel(factory = SocialViewModelProvider.Factory),
    navController: NavController
) {
    var localImages by remember { mutableStateOf(true) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val drawing = dvm.createFile("Drawing_${System.currentTimeMillis()}")
                    val encodedFilePath = Uri.encode(drawing.filePath)
                    navController.navigate("draw/$encodedFilePath/${drawing.fileName}")
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                TextButton(
                    onClick = {
                        svm.logout()
                        navController.navigate("splash") {
                            popUpTo("gallery") { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Out", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tabs = listOf("My Drawings", "Uploaded")
            val selectedTabIndex = if (localImages) 0 else 1

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            localImages = index == 0
                            if (!localImages) svm.fetchFiles()
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val list by if (localImages)
                dvm.drawings.collectAsState(emptyList())
            else
                dvm.serverDrawings.collectAsState(emptyList())

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                for (file in list.asReversed()) {
                    item {
                        DrawingFileCard(file, navController, dvm, svm, localImages)
                    }
                }
            }
        }
    }
}

/**
 * A card displaying a single drawing, including action icons and metadata.
 */
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
        Card(modifier = Modifier.aspectRatio(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val encodedFilePath = Uri.encode(file.filePath)
                        navController.navigate("draw/$encodedFilePath/${file.fileName}")
                    }
                    .testTag("DrawingCard_${file.fileName}")
                    .paint(
                        painter = rememberAsyncImagePainter(
                            model = File(file.filePath, file.fileName).absolutePath
                        )
                    )
            ) {
                val currentUser = TokenStore.username

                // delete icon stays the same
                if (localImages || file.ownerUsername == currentUser) {
                    IconButton(
                        onClick = { dvm.deleteFile(file) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("Delete_${file.fileName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color.Gray
                        )
                    }
                }

                // upload icon stays the same
                if (localImages) {
                    IconButton(
                        onClick = { svm.uploadFile(file) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .testTag("UploadButton_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Upload")
                    }
                } else {
                    // ← here’s the ONLY change: call downloadFile(uploader, filename)
                    IconButton(
                        onClick = {
                            svm.downloadFile(
                                file.ownerUsername ?: return@IconButton,
                                file.fileName
                            )
                            isDownloaded = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
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

        Text(
            text = file.fileName,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .testTag("FileName_${file.fileName}")
        )

        // only show “By …” in the cloud gallery
        if (!localImages) {
            file.ownerUsername?.let {
                Text(
                    text = "By $it",
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}