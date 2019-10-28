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
import kotlin.test.*

/**
 * Tests for ProcessorEnvironment
 */
class EnvironmentTest {

    private val env = MyEnvironment(strVar = "myStr", intVar = 153, longVar = -999654L)

    @Test
    fun createEnvoronment() {
        assertTrue(env.has("strVar"))
    }

    @Test
    fun get() {
        expect("myStr") { env.get("strVar") }
        assertFails { env.get<Int>("strVar") }
        expect(153) { env.get("intVar") }
        expect("153") { env.get<String>("intVar") }
        expect(153L) { env.get<Long>("intVar") }
        expect(-999654) { env.get<Int>("longVar") }
        expect("-999654") { env.get<String>("longVar") }
        expect(-999654L) { env.get<Long>("longVar") }
    }

    @Test
    fun getOrDefault() {
        expect(listOf<Int>(12, 13)) { env.get<List<Int>>("strInt", listOf<Int>(12, 13)) }
        expect(-999654L) { env.get<Long>("longVar", 234L) }
    }

    @Test
    fun getOrNull() {
        expect(null) { env.getOrNull<List<Int>>("strInt") }
        expect(-999654L) { env.getOrNull<Long>("longVar") }
    }

    internal class MyEnvironment(
        var strVar: String = "",
        var intVar: Int = -1,
        var longVar: Long = -1L
    ) : IProcessorEnvironment {

        override fun has(name: String, klazz: KClass<*>): Boolean {
            if (klazz !in arrayOf(String::class, Int::class, Long::class)) return false
            return name in arrayOf("strVar", "intVar", "longVar")
        }

        override fun has(name: String): Boolean = name in arrayOf("strVar", "intVar", "longVar")

        override fun <T> get(name: String, klazz: KClass<*>): T = when {
            name == "strVar" && klazz == String::class -> strVar as T
            name == "strVar" && klazz == Int::class -> strVar.toInt() as T
            name == "strVar" && klazz == Long::class -> strVar.toLong() as T
            name == "intVar" && klazz == String::class -> intVar.toString() as T
            name == "intVar" && klazz == Int::class -> intVar as T
            name == "intVar" && klazz == Long::class -> intVar.toLong() as T
            name == "longVar" && klazz == String::class -> longVar.toString() as T
            name == "longVar" && klazz == Int::class -> longVar.toInt() as T
            name == "longVar" && klazz == Long::class -> longVar as T
            else -> throw RuntimeException("Wrong parameter")
        }

    }


}