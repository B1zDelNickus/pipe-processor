package ru.avasilevich.pipe_processor

import kotlin.test.Test
import kotlin.test.assertEquals

class SubProcessorSyncTest {
    @Test
    fun subProcessorSplitOrderTest() {
        val order: MutableList<String> = mutableListOf()
        val rightOrder = listOf(
            "split: 1",
            "exec: 1",
            "join: 1",
            "split: 2",
            "exec: 2",
            "join: 2",
            "split: 3",
            "exec: 3",
            "join: 3"
        )

        val processor = processor<Unit> {
            subProcessor<Int> {
                bufferSize { -1 }
                split {
                    sequenceOf(1, 2, 3)
                        .onEach {
                            order.add("split: $it")
                            println("split: $it")
                        }
                }

                exec {
                    order.add("exec: $this")
                    println("exec: $this")
                }

                join { context: Int ->
                    order.add("join: $context")
                    println("join: $context")
                }
            }
        }

        runMultiplatformBlocking { processor.exec(Unit) }

        assertEquals(rightOrder, order)
    }

}