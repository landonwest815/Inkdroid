package com.example.customviewdemo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.customviewdemo.databinding.FragmentDrawBinding
import kotlinx.coroutines.launch
import kotlin.random.Random


class DrawFragment : Fragment() {

    //OK for this demo, but views are created/destroyed during lifecycle events so
    //this should really be stored in a viewmodel!
    private val bitmap =
        Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
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
        val bitmapModel: BitmapModel by activityViewModels()

        lifecycleScope.launch {
            bitmapModel.bitmap.collect { //new bitmap is passed as `it`
                binding.customView.setBitmap(it)

                //trigger the view's onDraw to run eventually
                binding.customView.invalidate()
            }
        }

        /*
        // when the view draws itself, copy our bitmap onto
        // the screen

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
        */

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDrawBinding.bind(view)

        //in onViewCreated so that view isn't null
        view.setOnTouchListener { _, event ->
            // draw on drag
            if (event.action == MotionEvent.ACTION_MOVE) {
                drawPen(event.x, event.y)
            }
            true
        }
    }

    //draws to our bitmap.  If the bitmap is stored in a VM, this method should probably get
    //moved to the fragment or possibly the VM
    fun drawCircle(color: Color, scale: Float){
        val bitmapModel: BitmapModel by activityViewModels()
        bitmapModel.drawCircle(color, scale, paint)
    }

    private fun drawPen(x : Float, y : Float){
        val viewModel : SimpleViewModel by activityViewModels()

        lifecycleScope.launch {
            viewModel.color.collect { //new color is passed as `it`
                paint.color = it.toArgb()

                val bitmapModel: BitmapModel by activityViewModels()
                bitmapModel.drawPen(x, y, paint)
            }
        }
    }
}