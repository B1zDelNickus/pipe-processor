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

import kotlinx.coroutines.withTimeout

/**
 * Main Processor class that includes all workflow of the pipe line
 */
open class Processor<T>(
    private val matcher: ProcessorMatcherType<T> = { true },
    private val handlers: List<IProcessorHandler<T>> = listOf(),
    private val timeout: Long = 0L
) : IProcessorHandler<T> {
    override fun match(context: T, env: IProcessorEnvironment): Boolean = context.matcher(env)

    override suspend fun exec(context: T, env: IProcessorEnvironment) {
        if (timeout > 0L) {
            withTimeout(timeout) {
                handlers.forEach {
                    if (it.match(context, env)) it.exec(context, env)
                }
            }
        } else {
            handlers.forEach {
                if (it.match(context, env)) it.exec(context, env)
            }
        }
    }
}

