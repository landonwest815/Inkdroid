package com.example.drawingappall.views

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.viewModels.DrawingFileViewModel
import com.example.drawingappall.viewModels.DrawingViewModelProvider
import com.example.drawingappall.viewModels.SocialViewModel
import com.example.drawingappall.viewModels.SocialViewModelProvider
import java.io.File

/**
 * Displays the gallery of either local or server-based drawings.
 * On tablets (screen width ≥ 600 dp) it shows 3 columns; otherwise 2.
 */
@Composable
fun GalleryScreen(
    dvm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    svm: SocialViewModel = viewModel(factory = SocialViewModelProvider.Factory),
    navController: NavController
) {
    var localImages by remember { mutableStateOf(true) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val columns = if (screenWidthDp >= 600) 3 else 2

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val drawing = dvm.createFile("Drawing_${System.currentTimeMillis()}")
                val encodedPath = Uri.encode(drawing.filePath)
                navController.navigate("draw/$encodedPath/${drawing.fileName}")
            }) {
                Icon(Icons.Default.Add, contentDescription = "New Drawing")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE))
                .padding(paddingValues)
        ) {
            // Logout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                TextButton(onClick = {
                    svm.logout()
                    navController.navigate("splash") { popUpTo("gallery") { inclusive = true } }
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.Red)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Out", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            val tabs = listOf("My Drawings", "Uploaded")
            val selectedTab = if (localImages) 0 else 1
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            localImages = index == 0
                            if (!localImages) svm.fetchFiles()
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choose which list
            val files by if (localImages)
                dvm.drawings.collectAsState(emptyList())
            else
                dvm.serverDrawings.collectAsState(emptyList())

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(files.asReversed()) { file ->
                    DrawingFileCard(file, navController, dvm, svm, localImages)
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
    val currentUser = TokenStore.username

    Column(modifier = modifier) {
        Card(modifier = Modifier.aspectRatio(1f)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable {
                    val path = Uri.encode(file.filePath)
                    navController.navigate("draw/$path/${file.fileName}")
                }
                .testTag("DrawingCard_${file.fileName}")
                .paint(
                    painter = rememberAsyncImagePainter(
                        model = File(file.filePath, file.fileName).absolutePath
                    )
                )
            ) {
                // Delete icon in top-end
                if (localImages) {
                    IconButton(
                        onClick = { dvm.deleteFile(file) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("DeleteLocal_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete from device", tint = Color.Gray)
                    }
                } else if (file.ownerUsername == currentUser) {
                    IconButton(
                        onClick = {
                            svm.deleteRemote(file)
                            svm.fetchFiles()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("DeleteServer_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete from server", tint = Color.Gray)
                    }
                }

                // Upload (local) or Download (remote) icon in top-start
                if (localImages) {
                    IconButton(
                        onClick = { svm.uploadFile(file) },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .testTag("Upload_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Upload")
                    }
                } else {
                    IconButton(
                        onClick = {
                            svm.downloadFile(file.ownerUsername ?: return@IconButton, file.fileName)
                            isDownloaded = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .testTag("Download_${file.fileName}")
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
