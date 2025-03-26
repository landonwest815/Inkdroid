package com.example.drawingappall

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun GalleryScreen(
    vm: DrawingFileViewModel = viewModel(factory = DrawingViewModelProvider.Factory),
    navController: NavController) {

    Scaffold(
        // add new drawing button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val drawing: Drawing = vm.createFile("Drawing_${System.currentTimeMillis()}")
                    val encodedFilePath = Uri.encode(drawing.filePath)
                    navController.navigate("draw/${encodedFilePath}/${drawing.fileName}")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Drawing")
            }
        }
    ) {
        Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFEEEEEE)) // gray background
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

            // header
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "My Drawings",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // grid of all drawings
            val list by vm.drawings.collectAsState(listOf())
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                for (file in list.asReversed()) {
                    item {
                        DrawingFileCard(file, navController, vm, modifier = Modifier.animateItem())
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
    vm: DrawingFileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f), // enforce square size
        ) {
            Box(
                modifier = Modifier
                    .clickable {
                        val encodedFilePath = Uri.encode(file.filePath)
                        navController.navigate("draw/${encodedFilePath}/${file.fileName}")
                    }
                    .fillMaxSize()
                    .paint(
                        painter = rememberAsyncImagePainter(
                            model = File(
                                file.filePath,
                                file.fileName
                            ).absolutePath
                        )
                    )
            ) {

                // delete button
                IconButton(
                    onClick = { vm.deleteFile(file) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(0.dp) // small padding from the edges
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
        }

        // drawing title
        Text(
            text = file.fileName,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GalleryScreenPreview() {
//
//    val navController = rememberNavController()
//
//    GalleryScreen(
//        navController = navController
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DrawingPreview() {
//
//    val navController = rememberNavController()
//
//    DrawingFileCard(
//        Drawing("MyDrawing", "default_preview.png"),
//        navController,
//        vm = viewModel(factory = DrawingViewModelProvider.Factory),
//    )
//}