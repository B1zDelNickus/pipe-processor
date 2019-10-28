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

import kotlin.coroutines.CoroutineContext

fun <T : Any> processorBuilder(body: ProcessorBuilder<T>.() -> Unit): ProcessorBuilder<T> =
    ProcessorBuilder<T>().apply(body)

/**
 * Global method for creation of a processor
 */
fun <T : Any> processor(body: ProcessorBuilder<T>.() -> Unit): Processor<T> = processorBuilder(body).build()

/**
 * Global method for creation of a handler instance
 */
fun <T : Any> handler(body: HandlerBuilder<T>.() -> Unit): IProcessorHandler<T> {
    val builder = HandlerBuilder<T>()
    builder.body()
    return builder.build()
}

typealias ProcessorExecutorType<T> = suspend T.(IProcessorEnvironment) -> Unit
typealias ProcessorMatcherType<T> = T.(IProcessorEnvironment) -> Boolean
typealias ProcessorExecutorShortType<T> = suspend T.() -> Unit
typealias ProcessorMatcherShortType<T> = T.() -> Boolean
typealias ProcessorTimeoutType = () -> Long

typealias SubProcessorJoinerType<T, S> = suspend T.(joining: S, env: IProcessorEnvironment) -> Unit
typealias SubProcessorSplitterType<T, S> = suspend T.(env: IProcessorEnvironment) -> Sequence<S>
typealias SubProcessorJoinerShortType<T, S> = suspend T.(joining: S) -> Unit
typealias SubProcessorSplitterShortType<T, S> = suspend T.() -> Sequence<S>
typealias SubProcessorCoroutineContextType<T> = suspend T.(env: IProcessorEnvironment) -> CoroutineContext
typealias SubProcessorCoroutineBufferSize<T> = suspend T.(env: IProcessorEnvironment) -> Int
typealias SubProcessorCoroutineConsumer<T> = suspend T.(env: IProcessorEnvironment) -> Int
