package ru.avasilevich.pipe_processor

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JvmParallelKoveyor {
    @Test
    fun parallelSubProcessorTest() {
        val myContext = MyContext(id = "1", value = 1, list = mutableListOf(12L, 13L, 14L, 15L, 16L))
        val processor = processor<MyContext> {
            subProcessor<MySubContext> {
                joinersNumber { 1 }
                bufferSize { 3 }

                split {
                    list
                        .map {
                            println("gen: $it")
                            MySubContext(
                                subId = it.toString(),
                                subValue = it
                            )
                        }
                        .asSequence()
                }

                exec {
                    println("*2: $subValue")
                    subValue *= 2
                }

                join { joining: MySubContext ->
                    println("merge: ${joining.subValue}")
                    value += joining.subValue.toInt()
                }
            }
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(141, myContext.value)

    }

    internal data class MyContext(
        var id: String = "",
        @Volatile
        var value: Int = 0,
        var list: MutableList<Long> = mutableListOf()
    )

    internal data class MySubContext(
        var subId: String = "",
        @Volatile
        var subValue: Long = 0
    )


}
