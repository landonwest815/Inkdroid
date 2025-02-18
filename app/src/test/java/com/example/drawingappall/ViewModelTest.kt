package com.example.drawingappall

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test

class ViewModelTest {

//    val vm = SimpleViewModel()
//    val lifecycelOwner = TestLifecycleOwner()
//    @Test
//    fun basicToolsViewModelTest(){
//        val before = vm.color.value!!
//        var callbackFired = false
//        vm.color.observe(lifecycelOwner){
//            callbackFired = true
//        }
//        vm.pickColor()
//        assertTrue(callbackFired)
//        assertNotSame(before, vm.color.value!!)
//
//    }

    @Test
    fun `test VMss flow and update method`() {
        //fails because we call Color constructor
        //and Color is in the Android namespace
        val vm = SimpleViewModel()
        //runBlocking {
        val lifecycleOwner = TestLifecycleOwner()//Lifecycle.State.CREATED,this.coroutineContext)
        val before = vm.color.value
        var callbackFired = false

        //We'll cover this later in the course
        // but set up an observer callback for the state flow
        lifecycleOwner.lifecycleScope.launch {

            //Actual test code stuff happening here!

            vm.color.collect {
                callbackFired = true
            }
        }

        vm.pickColor() //should trigger the callback

        Assert.assertTrue(callbackFired)

        //Uses == not .equals since the random color could
        //in theory be exactly the same (though VERY unlikely)
        Assert.assertNotSame(before, vm.color.value)

        // }
    }
}