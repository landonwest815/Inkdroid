package com.example.drawingappall

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingappall.TestUtils.testDrawingVM
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SizeTests {

    @Test
    fun defaultSize(){
        val vm = testDrawingVM();
        val start = vm.shapeSize.value
        val size = 66f
        assertEquals(size, start)
    }

    @Test
    fun sizeUpsizeDown(){
        val vm = testDrawingVM();
        vm.updateSize(89f)
        assertEquals(89f, vm.shapeSize.value)

        vm.updateSize(34f)
        assertEquals(34f, vm.shapeSize.value)
    }

    @Test
    fun sizeup(){
        val vm = testDrawingVM();
        vm.updateSize(67f)
        assertEquals(67f, vm.shapeSize.value)

    }

    @Test
    fun sizeDown(){
        val vm = testDrawingVM();
        vm.updateSize(25f)
        assertEquals(25f, vm.shapeSize.value)
    }

    @Test
    fun maxSize(){
        val vm = testDrawingVM();
        vm.updateSize(100f)
        assertEquals(100f, vm.shapeSize.value)

        vm.updateSize(101f)
        assertEquals(100f, vm.shapeSize.value)
    }

    @Test
    fun minSize(){
        val vm = testDrawingVM();
        vm.updateSize(5f)
        assertEquals(5f, vm.shapeSize.value)

        vm.updateSize(4f)
        assertEquals(5f, vm.shapeSize.value)
    }

}