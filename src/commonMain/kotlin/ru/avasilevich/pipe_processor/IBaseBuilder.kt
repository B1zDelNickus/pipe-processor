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

interface IBaseBuilder<T : Any> {
    /**
     * With this methods one can set the lambda for matcher [[IProcessorHandler.match]] to the handler
     */
    fun on(block: ProcessorMatcherShortType<T>) {
        onEnv { block() }
    }

    /**
     * With this methods one can set the lambda for matcher [[IProcessorHandler.match]] having access to
     * [[IProcessorEnvironment]] through lambda parameter to the handler
     */
    fun onEnv(block: ProcessorMatcherType<T>)

    /**
     * Set up timeout for handlers and processors
     */
    fun timeout(block: ProcessorTimeoutType)

    fun build(): IProcessorHandler<T>

    operator fun invoke(block: (IBaseBuilder<T>) -> Unit): IBaseBuilder<T> {
        block(this)
        return this
    }
}