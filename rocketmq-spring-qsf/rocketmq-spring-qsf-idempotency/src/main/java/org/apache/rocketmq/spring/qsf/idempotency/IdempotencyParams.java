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

package org.apache.rocketmq.spring.qsf.idempotency;

import lombok.Data;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * @desc
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdempotencyParams {
    /**
     * null means no need for idempotency, true means need for idempotency
     * use idempotency should import rocketmq-spring-qsf-idempotency
     */
    private boolean idempotent;

    /**
     * idempotency expiration milliseconds, null means no need for idempotency, 0 or negative means no expiration
     */
    private long idempotencyMillisecondsToExpire;

    /**
     * idempotent method execute timeout
     */
    private long idempotentMethodExecuteTimeout;
}