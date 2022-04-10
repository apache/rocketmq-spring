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

package org.apache.rocketmq.spring.qsf.callback;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.rocketmq.spring.qsf.callback.domain.QSFCallBackObject;

/**
 * @desc After the mq lister processes the message, it calls back SyncQSFConsumerCallBack to notify the completion of the message processing and pass the return value
 */
public interface SyncQSFConsumerCallBack {
    /**
     * <key, the waiting lock of the thread sending the message> map, key={traceId}:{threadId}
     * After the current thread sends a message, Condition.await waits,
     * until the mq listener completes the message processing, call the SyncQSFConsumerCallBack provider and pass the return value back
     */
    ConcurrentMap<String, QSFCallBackObject> CALLBACK_MANAGE_MAP = new ConcurrentHashMap<>(96);

    /**
     * Call the callback after mq processing, and pass the return value
     * @param sourceAoneApp call source app
     * @param callKey = {traceId}:{threadId}
     * @param returnValue
     */
    void syncValueCallBack(String sourceAoneApp, String callKey, Object returnValue);
}