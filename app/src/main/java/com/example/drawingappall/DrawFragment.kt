package com.example.drawingappall

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.drawingappall.databinding.FragmentDrawBinding
import kotlinx.coroutines.launch


class DrawFragment : Fragment() {

    //OK for this demo, but views are created/destroyed during lifecycle events so
    //this should really be stored in a viewmodel!
    private val bitmap =
        Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private val bitmapCanvas = Canvas(bitmap)
    private val paint = Paint()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentDrawBinding.inflate(inflater)

        // usage of activityViewModels would be important
        // to share this data across fragments
        // here it doesn't matter
        val viewModel : SimpleViewModel by activityViewModels()
        val sizeViewModel: SizeViewModel by activityViewModels()

        // when the view draws itself, copy our bitmap onto
        // the screen
        binding.customView.setBitmap(bitmap)
        lifecycleScope.launch {
            viewModel.color.collect { //new color is passed as `it`
                //update the bitmap
                // use the most recent value of the flow here... feels dirty!
                drawCircle(it, sizeViewModel.circleSize.value)
                //trigger the view's onDraw to run eventually
                binding.customView.invalidate()
            }
        }
        lifecycleScope.launch {
            sizeViewModel.circleSize.collect() {
                //update the bitmap
                // use the most recent value of the color flow here... feels dirty!
                drawCircle(viewModel.color.value, it)
                //trigger the view's onDraw to run eventually
                binding.customView.invalidate()
            }
        }
        return binding.root

    }

    //draws to our bitmap.  If the bitmap is stored in a VM, this method should probably get
    //moved to the fragment or possibly the VM
    fun drawCircle(color: Color, scale: Float){
        paint.color = Color.WHITE
        bitmapCanvas.drawRect(0f,0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        paint.color = color.toArgb()
        bitmapCanvas.drawCircle(scale*bitmap.width, scale*bitmap.height,
            0.5f*scale*bitmap.width, paint)

    }

}