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

interface IHandlerContainerBuilder<T : Any> {
    fun add(handler: IProcessorHandler<T>)
    fun add(handler: IBaseBuilder<T>) = add(handler.build())
    fun add(handler: ProcessorExecutorType<T>) =
        add(HandlerBuilder<T>().apply {
            execEnv(handler)
        })

    fun add(handler: ProcessorExecutorShortType<T>) =
        add(HandlerBuilder<T>().apply {
            exec(handler)
        })

    fun add(handler: IProcessorExecutor<T>) =
        add(HandlerBuilder<T>().apply {
            execEnv { env ->
                handler.exec(this, env)
            }
        })

    operator fun IProcessorHandler<T>.unaryPlus() =
        this@IHandlerContainerBuilder.add(this)

    operator fun IBaseBuilder<T>.unaryPlus() = this@IHandlerContainerBuilder.add(this)
    operator fun IProcessorExecutor<T>.unaryPlus() =
        this@IHandlerContainerBuilder.add(this)

    fun exec(block: ProcessorExecutorShortType<T>) {
        add(block)
    }

    fun execEnv(block: ProcessorExecutorType<T>) {
        add(block)
    }

    fun handler(block: HandlerBuilder<T>.() -> Unit) {
        val builder = HandlerBuilder<T>()
        builder.block()
        val handler = builder.build()
        add(handler)
    }

    fun processor(block: ProcessorBuilder<T>.() -> Unit) {
        val builder = ProcessorBuilder<T>()
        builder.block()
        val handler = builder.build()
        add(handler)
    }

    fun <S : Any> subProcessor(block: SubProcessorBuilder<T, S>.() -> Unit) {
        val builder = SubProcessorBuilder<T, S>()
        builder.block()
        val handler = builder.build()
        add(handler)
    }

}