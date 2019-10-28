package ru.avasilevich.pipe_processor

import kotlin.test.Test
import kotlin.test.assertEquals

class HandlerTest {

    @Test
    fun createTest() {
        val h1 = handler<MyContext> {
            on { true }
            exec { value += 100 }
        }
        val h2 = handler<MyContext> {
            on { false }
            exec { value += 120 }
        }

        val proc = processor<MyContext> {
            +h1
            +h2
        }
        val context = MyContext()
        runMultiplatformBlocking { proc.exec(context) }
        assertEquals(100, context.value)
    }

    internal data class MyContext(
        var id: String = "",
        var value: Int = 0
    )

    @Test
    fun handlerMatchTest() {
        val proc = processor<MyContext> {
            exec {
                value = 41
            }

            +MyHandler
            exec { assertEquals(-1, value) }

            +MyHandler
            exec { assertEquals(-1, value) }
        }

        runMultiplatformBlocking { proc.exec(MyContext()) }
    }

    @Test
    fun subProcessorHandlerMatchTest() {
        val processor = processor<MyContext> {
            exec {
                value = 41
            }

            subProcessor<MyContext> {
                split {
                    sequenceOf(this)
                }

                +MyHandler
                exec { assertEquals(-1, value) }

                +MyHandler
                exec { assertEquals(-1, value) }
            }
        }

        runMultiplatformBlocking { processor.exec(MyContext()) }
    }

    internal object MyHandler : IProcessorHandler<MyContext> {
        override fun match(context: MyContext, env: IProcessorEnvironment): Boolean = context.value >= 0

        override suspend fun exec(context: MyContext, env: IProcessorEnvironment) {
            context.value -= 42
        }
    }
}
