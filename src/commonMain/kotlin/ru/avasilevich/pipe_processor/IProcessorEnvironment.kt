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

/**
 * Interface describing environment for Processor. In the environment you can pass database connections,
 * environment variables and other entities which are not worth to be stored in context.
 */
interface IProcessorEnvironment {

    /**
     * Checks if the element of environment with name `name` is stored
     */
    fun has(name: String): Boolean

    /**
     * Type safe check for the element of environment with name `name` is stored
     */
    fun has(name: String, klazz: KClass<*>): Boolean

    /**
     * Returns the value of the environment's element with name `name`
     */
    fun <T> get(name: String, klazz: KClass<*>): T

}

inline fun <reified T : Any> IProcessorEnvironment.get(name: String): T = get(name, T::class)
inline fun <reified T : Any> IProcessorEnvironment.get(name: String, default: T): T = try {
    get(name, T::class)
} catch (e: Exception) {
    default
}

inline fun <reified T : Any> IProcessorEnvironment.get(name: String, default: () -> T): T = try {
    get(name, T::class)
} catch (e: Exception) {
    default()
}

inline fun <reified T : Any> IProcessorEnvironment.getOrNull(name: String): T? = try {
    get(name, T::class)
} catch (e: Exception) {
    null
}

