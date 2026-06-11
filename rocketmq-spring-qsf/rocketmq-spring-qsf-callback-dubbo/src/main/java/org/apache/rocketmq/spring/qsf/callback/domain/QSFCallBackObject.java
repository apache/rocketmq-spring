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

package org.apache.rocketmq.spring.qsf.callback.domain;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @desc QSF callback info
 **/

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QSFCallBackObject {
    /**
     * After the thread that initiated the QSF call sends the message, CountDownLatch.await waits,
     * until mq listener completes message processing, call SyncQSFConsumerCallBack.syncValueCallBack and pass the return value back
     */
    private CountDownLatch callBackCountDownLatch;

    /**
     * used to pass return value
     */
    private Object returnValue;

    /**
     * When mq listener calls SyncQSFConsumerCallBack.syncValueCallBack,
     * only the mq listener which is in validCallbackSourceApps set is allowed to participate in waking up the thread that initiated the QSF call
     */
    private Set<String> validCallbackSourceApps;

    /**
     * Specify the callback return value of the mq listener, if not specified, the return value will be the last callback
     */
    private String callBackReturnValueAppName;
}
