/*
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

package org.apache.rocketmq.spring.qsf.annotation.msgproducer;

/**
 * @desc Specify the method call configuration. If you need a return value, you must specify methodSpecials.syncCall=true
 */
public @interface QSFMethodInvokeSpecial {
    /**
     * true: synchronous call, block the current thread after sending the message, until the message listener processes the message, then notify the message sending thread, and then continue to execute
     * false: asynchronous call, do not wait after sending a message, continue execution directly.
     *
     * Synchronous invocation expands the usage scenarios of mq; please do business evaluation and capacity planning when using it, and make sure that synchronous blocking will not cause business risks.
     *
     * To enable syncCall, you need to import dependency rocketmq-spring-qsf-callback-dubbo.
     */
    boolean syncCall() default false;

    /**
     * When called synchronously (syncCall=true), after sending a message, how many milliseconds will await.
     */
    long syncCallBackTimeoutMs() default 3000L;

    /**
     * The specified method is executed according to the ConsumerMethodSpecial configuration
     */
    String methodName();

    /**
     * Which callbacks from mq listener to wait for to wake up the message sending thread, if not specified, any callback is allowed to wake up the message sending thread.
     * The appName used in the mq producer is ${qsf.project.name} , or an empty string if the configuration does not exist.
     */
    String[] waitCallBackAppNames() default {};

    /**
     * Specify the callback return value of the message producer, if not specified, the thread that send the message will take the last callback as return value.
     */
    String returnValueAppName() default "";

    /**
     * The minimum number of callbacks to wake up the message sending thread.
     */
    int minCallBackTimes() default 1;
}