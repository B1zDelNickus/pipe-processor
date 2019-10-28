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

import ru.avasilevich.pipe_processor.exceptions.NotFoundEnvElementException
import kotlin.reflect.KClass

object DefaultProcessorEnvironment : IProcessorEnvironment {

    private val map = mutableMapOf<String, Pair<KClass<*>, Any>>()

    override fun has(name: String): Boolean = map.contains(name)

    override fun has(name: String, klazz: KClass<*>): Boolean = map[name]?.first == klazz ?: false

    override fun <T> get(name: String, klazz: KClass<*>): T = map[name]
        ?.let {
            if (it.first == klazz) it.second as T else null
        } ?: throw NotFoundEnvElementException(name)

    fun setWithClass(name: String, klazz: KClass<*>, value: Any) {
        map[name] = klazz to value
    }

    inline operator fun <reified T : Any> get(name: String): T = (this as IProcessorEnvironment).get(name)

    inline operator fun <reified T : Any> set(name: String, value: T) =
        setWithClass(name, T::class, value)
}
