package ru.avasilevich.pipe_processor

import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessBuilderTest {

    @Test
    fun testForBuilders() {
        val procBuilder = processorBuilder<MyContext> {
            exec { x += 3 }
        }

        val executor = object: IProcessorExecutor<MyContext> {
            override suspend fun exec(context: MyContext, env: IProcessorEnvironment) {
                context.x += 12
            }

        }

        val proc = processor<MyContext> {
            exec { x += 7 }
            +executor
            +processorBuilder<MyContext> {
                exec { x += 113 }
            }
            +procBuilder
            +procBuilder {
                exec { x += 1021 }
            }
            handler { exec { x += 10117 } }
            processor { exec { x += 100321 } }
            subProcessor<MySubContext> {
                split { sequenceOf(MySubContext(x=15)) }
                handler { exec { x += 17 } }
                exec { x += 172 }
                subProcessor<MySubSubContext> {
                    on { false }
                }
                join { it -> x += it.x }
            }
            add { _: IProcessorEnvironment ->
                x += 1000437
            }
            add { ->
                x += 1000385
            }
        }
        val context = MyContext(x=0)
        runMultiplatformBlocking { proc.exec(context) }

        assertEquals(7 +12 +113 +3 +3+1021 + 10117 + 100321 +15+17+172+1000437+1000385, context.x)
    }

    internal data class MyContext(var x: Int)
    internal data class MySubContext(var x: Int)
    internal data class MySubSubContext(var x: Int)
}