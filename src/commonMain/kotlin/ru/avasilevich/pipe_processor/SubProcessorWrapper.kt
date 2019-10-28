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

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * SubProcessor class that includes all workflow of the pipe line
 */
class SubProcessorWrapper<T : Any, S : Any>(
    private val matcher: ProcessorMatcherType<T> = { true },
    private val handlers: Collection<IProcessorHandler<S>> = mutableListOf(),
    private val splitter: SubProcessorSplitterType<T, S> = { sequence { } },
    private val joiner: SubProcessorJoinerType<T, S> = { _: S, _: IProcessorEnvironment -> },
    private val bufferSizer: SubProcessorCoroutineBufferSize<T> = { 0 },
    private val contexter: SubProcessorCoroutineContextType<T> = { EmptyCoroutineContext },
    private val consumer: SubProcessorCoroutineConsumer<T> = { 1 }
) : IProcessorHandler<T> {

    override fun match(context: T, env: IProcessorEnvironment): Boolean = context.matcher(env)

    override suspend fun exec(context: T, env: IProcessorEnvironment) {
        val crContext = context.contexter(env)
        val consumers = context.consumer(env)
        val bSize = context.bufferSizer(env)
        withContext(crContext) {

            if (bSize < 0) {
                context
                    .splitter(env)
                    .forEach { s ->
                        handlers.forEach { handler -> if (handler.match(s, env)) handler.exec(s, env) }
                        context.joiner(s, env)
                    }
            } else {
                val src = produce(capacity = bSize) {
                    context.splitter(env).forEach { subContext -> send(subContext) }
                }

                val handlers = produce(capacity = bSize) {
                    for (context in src) {
                        handlers.forEach { handler -> if (handler.match(context, env)) handler.exec(context, env) }
                        send(context)
                    }
                }
                if (consumers > 1) {
                    repeat(consumers) {
                        launch { handlers.consumeEach { context.joiner(it, env) } }
                    }
                } else {
                    handlers.consumeEach { context.joiner(it, env) }
                }
            }
        }
    }
}

