package com.example.customviewdemo

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class SizeViewmodelTest {

    @Test
    fun `sizeVM test doesnt require instrumentation`() {
        val vm = SizeViewModel()
        var callbackFired = false
        runBlocking {
            val before = vm.circleSize.value

            //initial value
            Assert.assertEquals(0.25f, before)
            //We'll cover this later in the course
            // but set up an observer callback for the state flow
            launch {
                //Actual test code stuff happening here!
                // take(1) means stop listening after 1 update
                vm.circleSize.take(1).collect {
                    callbackFired = true
                    Assert.assertEquals(0.75f, it)
                }
            }
            vm.update(0.75f) //should trigger the callback
        }
        // outside of "runBlocking" for reasons we'll see soon
        Assert.assertTrue(callbackFired)
    }
}