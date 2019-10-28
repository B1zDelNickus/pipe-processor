/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.avasilevich.pipe_processor

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ProcessorTest {

    @Test
    fun execTest() {
        val myContext = MyContext(id = "1", value = 1)
        val processor = processor<MyContext> {
            exec {
                value++
            }
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(2, myContext.value)

    }

    @Test
    fun handlerTest() {
        val myContext1 = MyContext(id = "1", value = 1)
        val myContext2 = MyContext(id = "2", value = 1)
        val processor = processor<MyContext> {
            handler {
                on {
                    id == "1"
                }
                exec {
                    value++
                }
            }
        }

        runMultiplatformBlocking { processor.exec(myContext1) }
        runMultiplatformBlocking { processor.exec(myContext2) }

        assertEquals(2, myContext1.value)
        assertEquals(1, myContext2.value)

    }

    @Test
    fun subProcessorTest() {
        val myContext = MyContext(id = "1", value = 1, list = mutableListOf(12L, 13L, 14L))
        val processor = processor<MyContext> {
            subProcessor<MySubContext> {

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

        assertEquals(79, myContext.value)

    }

    @Test
    @Ignore
    // Test is ignored since SubProcessorBuilder::on is not working
    fun `subProcessorTest use on`() {
        val myContext = MyContext(id = "1", value = 1, list = mutableListOf(12L, 13L, 14L))
        val processor = processor<MyContext> {
            subProcessor<MySubContext> {
                on { false }
                split {
                    list
                        .map {
                            MySubContext(
                                subId = it.toString(),
                                subValue = it
                            )
                        }
                        .asSequence()
                }

                exec {
                    subValue *= 2
                }

                join { joining: MySubContext ->
                    value += joining.subValue.toInt()
                }
            }
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(1, myContext.value)

    }

    @Test
    fun emptySubProcessorTest() {
        val myContext = MyContext(id = "1", value = 1, list = mutableListOf(12L, 13L, 14L))
        val processor = processor<MyContext> {
            subProcessor<MySubContext> {
            }
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(1, myContext.value)

    }

    @Test
    fun processorTest() {
        val myContext = MyContext(id = "1", value = 1)
        val processor = processor<MyContext> {
            exec { value = 12 }
            processor {
                exec {
                    value *= 2
                }
                handler {
                    on { value <= 10 }
                    exec { value *= 4 }
                }
                handler {
                    on { value > 10 }
                    exec { value /= 4 }
                }
            }
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(6, myContext.value)

    }

    @Test
    fun addHandlerTest() {
        val myContext = MyContext(id = "1", value = 1)
        val processor = processor<MyContext> {
            add(SomeHandler())
            +SomeHandler()
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(2001, myContext.value)
    }

    @Test
    fun addProcessorTest() {
        val myContext = MyContext(id = "1", value = 1)
        val addedProcessor = processor<MyContext> {
            add(SomeHandler())
            +SomeHandler()
        }
        val processor = processor<MyContext> {
            add(addedProcessor)
            +addedProcessor
        }

        runMultiplatformBlocking { processor.exec(myContext) }

        assertEquals(4001, myContext.value)
    }

    internal data class MyContext(
        var id: String = "",
        var value: Int = 0,
        var list: MutableList<Long> = mutableListOf()
    )

    internal data class MySubContext(
        var subId: String = "",
        var subValue: Long = 0
    )

    internal class SomeHandler() : IProcessorHandler<MyContext> {
        override fun match(context: MyContext, env: IProcessorEnvironment): Boolean = true

        override suspend fun exec(context: MyContext, env: IProcessorEnvironment) {
            context.value += 1000
        }

    }

}
