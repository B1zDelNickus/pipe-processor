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

/**
 * The builder which builds Handler object
 *
 */
@ru.avasilevich.pipe_processor.ProcessorTagMarker
open class HandlerBuilder<T : Any> : ru.avasilevich.pipe_processor.BaseBuilder<T>(),
    IHandlerBuilder<T> {

    private var executor: ProcessorExecutorType<T> = { }

    /**
     * With this methods one can set the lambda for matcher [[IProcessorHandler.match]] having access to
     * [[IProcessorEnvironment]] through lambda parameter to the handler
     */
    override fun onEnv(block: ProcessorMatcherType<T>) {
        matcher = block
    }

    /**
     * With this methods one can set the lambda for executor [[IProcessorHandler.exec]] having access to
     * [[IProcessorEnvironment]] through lambda parameter to the handler
     */
    override fun execEnv(block: ProcessorExecutorType<T>) {
        executor = block
    }

    /**
     * Builds the [[IProcessorHandler]] implementation
     */
    override fun build(): IProcessorHandler<T> =
        ProcessorHandlerWrapper<T>(
            matcher = matcher,
            executor = executor,
            timeout = timeout
        )
}
