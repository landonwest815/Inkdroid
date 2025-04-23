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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingappall.R
import com.example.drawingappall.accounts.TokenStore
import com.example.drawingappall.databaseSetup.Drawing
import com.example.drawingappall.databaseSetup.StorageLocation
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

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE))
        ) {
            // — Top row with Logout, Title, (empty) —
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f).testTag("logout"), contentAlignment = Alignment.CenterStart) {
                    TextButton(onClick = {
                        svm.logout()
                        navController.navigate("splash") {
                            popUpTo("gallery") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.Red)
                        Spacer(Modifier.width(4.dp))
                        Text("Log Out", color = Color.Red)
                    }
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Drawing App",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            // — Tabs —
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
                            localImages = (index == 0)
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

            Spacer(Modifier.height(16.dp))

            // — Data for grid —
            val files by if (localImages)
                dvm.localDrawings.collectAsState(emptyList())
            else
                dvm.serverDrawings.collectAsState(emptyList())

            // — Grid with first-item “+” card in My Drawings —
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (localImages) {
                    item {
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    navController.navigate("draw/new")
                                }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add,
                                    contentDescription = "New Drawing",
                                    modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }

                items(files.asReversed()) { file ->
                    DrawingFileCard(
                        file = file,
                        navController = navController,
                        dvm = dvm,
                        svm = svm,
                        localImages = localImages,
                        onUploaded = {
                            localImages = false
                            svm.fetchFiles()
                        },
                        onDownloaded = {
                            localImages = true
                        }
                    )
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
    onUploaded: () -> Unit,
    onDownloaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = TokenStore.username

    Column(modifier = modifier) {
        Card(modifier = Modifier.aspectRatio(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = localImages) {
                        // only navigates when localImages == true
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
                        onClick = {
                            // remember if this drawing was also on the server
                            val wasSharedOnServer = file.storageLocation == StorageLocation.Both

                            // 1) remove local copy (and flip to Server if it was shared)
                            dvm.deleteFile(file)

                            // 2) if it used to live on the server, also delete remotely
                            if (wasSharedOnServer && file.ownerUsername == TokenStore.username) {
                                svm.deleteRemote(file)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag("DeleteLocal_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                    }
                } else {
                IconButton(
                    onClick = {
                        // remember if this drawing was also on the server
                        val wasSharedOnServer = file.storageLocation == StorageLocation.Both

                        // 1) remove local copy (and flip to Server if it was shared)
                        dvm.deleteFile(file)

                        // 2) if it used to live on the server, also delete remotely
                        if (wasSharedOnServer && file.ownerUsername == TokenStore.username) {
                            svm.deleteRemote(file)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .testTag("DeleteServer_${file.fileName}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                }
            }

                // Upload action for local drawings
                if (localImages) {
                    IconButton(
                        onClick = {
                            svm.uploadFile(file)
                            onUploaded()
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .testTag("Upload_${file.fileName}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Upload")
                    }
                } else {
                IconButton(
                    onClick = {
                        svm.copyFile(file.filePath, file.fileName)
                        onDownloaded()
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .testTag("Download_${file.fileName}")
                ) {
                    Icon(painter = painterResource(R.drawable.download), contentDescription = "Download")
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
