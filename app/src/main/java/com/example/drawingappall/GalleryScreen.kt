package com.example.drawingappall

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFEEEEEE)) // gray background
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("Gallery",
                color = Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.weight(1f))

            // New Drawing button
            IconButton(onClick = {
                val drawing: Drawing = vm.createFile("Drawing")
                val encodedFilePath = Uri.encode(drawing.filePath)
                navController.navigate("draw/${encodedFilePath}/${drawing.fileName}")
            }) {
                Icon(imageVector = Icons.Default.Add,
                    contentDescription = "Save",
                    tint = Color.Gray,
                    )
            }
        }

        val list by vm.drawings.collectAsState(listOf())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (file in list.asReversed()) {
                item {
                    DrawingFileCard(file, navController, vm)
                }
            }
        }

    }
}

@Composable
fun DrawingFileCard(file: Drawing, navController: NavController, vm: DrawingFileViewModel) {
    Card(modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(8.dp)
        .aspectRatio(1f),       // enforce square size
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint( painter = rememberAsyncImagePainter(model = File(file.filePath, file.fileName).absolutePath))
        ){
            Row {
                Text(
                    text = file.fileName,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Delete Button
                IconButton(onClick = {
                    vm.deleteFile(file) }) {
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                    )
                }

                // Edit Button
                IconButton(onClick = {
                    // Navigate
                    val encodedFilePath = Uri.encode(file.filePath)
                    navController.navigate("draw/${encodedFilePath}/${file.fileName}") }) {
                    Icon(imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryScreenPreview() {

    val navController = rememberNavController()

    GalleryScreen(
        navController = navController
    )
}

@Preview(showBackground = true)
@Composable
fun DrawingPreview() {

    val navController = rememberNavController()

    DrawingFileCard(
        Drawing("MyDrawing", "default_preview.png"),
        navController,
        vm = viewModel(factory = DrawingViewModelProvider.Factory),
    )
}