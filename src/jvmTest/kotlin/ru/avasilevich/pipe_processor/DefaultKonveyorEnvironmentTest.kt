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

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.expect

internal class DefaultProcessorEnvironmentJvmTest {

    @Test
    fun hasTest() {
        DefaultProcessorEnvironment["some"] = 12
        expect(12) {
            DefaultProcessorEnvironment["some"]
        }
        expect(13) {
            DefaultProcessorEnvironment.get("some1", 13)
        }
        expect(null) {
            DefaultProcessorEnvironment.getOrNull<Int>("some1")
        }
        expect(14) {
            DefaultProcessorEnvironment.get("some1") {
                14
            }
        }
        assertFails {
            val x: Int = DefaultProcessorEnvironment["some1"]
        }
        assertFails {
            val x: Long = DefaultProcessorEnvironment["some"]
        }
    }

}