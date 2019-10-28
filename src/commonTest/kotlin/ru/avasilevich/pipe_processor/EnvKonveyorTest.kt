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

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.expect

class EnvProcessorTest {

    @Test
    fun koveyorWithEnv() {
        val processor = processor<MyContext> {
            execEnv { env ->
                y += env.get<Int>("increment")
            }
        }

        val context = MyContext()

        expect(MyContext(0, 8)) {
            runMultiplatformBlocking { processor.exec(context, env = MyEnv) }
            context
        }
    }

    @Test
    fun subKoveyorWithEnv() {
        val processor = processor<MyContext> {
            subProcessor<MySubContext> {
                splitEnv { env ->
                    (1..25)
                        .map {
                            MySubContext()
                        }
                        .asSequence()
                }
                execEnv { env ->
                    z += env.get<Int>("increment")
                }
                handler {
                    onEnv { env ->
                        z == env.get<Int>("increment")
                    }
                    execEnv { env ->
                        k += env.get<Int>("increment")
                    }
                }
                joinEnv { joining, env ->
                    x += joining.z + env.get<Int>("increment")
                    y += joining.k + 2*env.get<Int>("increment")
                }
            }
        }

        val context = MyContext()

        expect(MyContext(400, 600)) {
            runMultiplatformBlocking { processor.exec(context, env = MyEnv) }
            context
        }
    }

    internal object MyEnv : IProcessorEnvironment {
        val increment = 8

        override fun has(name: String): Boolean = name == "increment"

        override fun has(name: String, klazz: KClass<*>): Boolean = name == "increment" && klazz == Int::class

        override fun <T> get(name: String, klazz: KClass<*>): T = when {
            klazz == Int::class -> increment as T
            else -> throw RuntimeException("Wrong class for $name")
        }
    }

    internal data class MyContext(
        var x: Int = 0,
        var y: Int = 0
    )

    internal data class MySubContext(
        var z: Int = 0,
        var k: Int = 0
    )

}
