package com.example.customviewdemo

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun `test VMss flow and update method`() {
        val vm = SimpleViewModel()
        val before = vm.color.value
        var callbackFired = false
        runBlocking {
            //We'll cover this later in the course
            // but set up an observer callback for the state flow
            launch {
                //Actual test code stuff happening here!
                vm.color.take(1).collect {
                    callbackFired = true
                }
            }
            vm.pickColor() //should trigger the callback
        }
        assertTrue(callbackFired)
        //Uses == not .equals since the random color could
        //in theory be exactly the same (though VERY unlikely)
        assertNotSame(before, vm.color.value)
    }
}