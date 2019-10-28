package ru.avasilevich.pipe_processor

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TimeoutTests {
    @Test
    fun timeoutOkTest() {
        val myContext = ProcessorTest.MyContext()
        val timeoutProcessor = processor<ProcessorTest.MyContext> {
            timeout { 1000 }
            exec { delay(100) }
        }

        runMultiplatformBlocking { timeoutProcessor.exec(myContext) }
    }

    @Test
    fun timeoutCancelTest() {
        val myContext = ProcessorTest.MyContext()
        val timeoutProcessor = processor<ProcessorTest.MyContext> {
            timeout { 100 }
            exec { delay(1000) }
        }
        assertFailsWith<TimeoutCancellationException> {
            runMultiplatformBlocking { timeoutProcessor.exec(myContext) }
        }
    }

}