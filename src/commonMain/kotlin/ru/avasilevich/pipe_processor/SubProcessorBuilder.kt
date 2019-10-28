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

import kotlin.coroutines.EmptyCoroutineContext


@ProcessorTagMarker
class SubProcessorBuilder<T : Any, S : Any> : ru.avasilevich.pipe_processor.BaseBuilder<T>(), IHandlerContainerBuilder<S> {

    private var splitter: SubProcessorSplitterType<T, S> = { sequence { } }
    private var joiner: SubProcessorJoinerType<T, S> = { _: S, _: IProcessorEnvironment -> }
    private var contexter: SubProcessorCoroutineContextType<T> = { EmptyCoroutineContext }
    private var bufferSizer: SubProcessorCoroutineBufferSize<T> = { 1 }
    private var consumer: SubProcessorCoroutineConsumer<T> = { 1 }
    private var handlers: MutableList<IProcessorHandler<S>> = mutableListOf()

    override fun build(): SubProcessorWrapper<T, S> = SubProcessorWrapper(
        matcher = matcher,
        handlers = handlers,
        splitter = splitter,
        joiner = joiner,
        bufferSizer = bufferSizer,
        contexter = contexter,
        consumer = consumer
    )

    fun bufferSize(block: SubProcessorCoroutineBufferSize<T>) {
        bufferSizer = block
    }

    fun coroutineContext(block: SubProcessorCoroutineContextType<T>) {
        contexter = block
    }

    fun joinersNumber(block: SubProcessorCoroutineConsumer<T>) {
        consumer = block
    }

    fun split(block: SubProcessorSplitterShortType<T, S>) {
        splitEnv { env ->
            block()
        }
    }

    fun splitEnv(block: SubProcessorSplitterType<T, S>) {
        splitter = block
    }

    fun join(block: SubProcessorJoinerShortType<T, S>) {
        joinEnv { joining, env ->
            block(joining)
        }
    }

    fun joinEnv(block: SubProcessorJoinerType<T, S>) {
        joiner = block
    }

    override fun add(handler: IProcessorHandler<S>) {
        handlers.add(handler)
    }

}
