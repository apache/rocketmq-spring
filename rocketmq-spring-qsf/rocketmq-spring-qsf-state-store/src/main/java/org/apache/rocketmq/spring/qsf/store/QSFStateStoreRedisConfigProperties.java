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

package org.apache.rocketmq.spring.qsf.store;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @desc redis config
 */
@Component
@Data
@ToString
public class QSFStateStoreRedisConfigProperties {
    @Value("${qsf.store.redis.host}")
    private String host;

    @Value("${qsf.store.redis.port}")
    private Integer port = 6379;

    @Value("${qsf.store.redis.cluster.max-attempts}")
    private Integer maxAttempts;

    @Value("${qsf.store.redis.timeout}")
    private Integer timeout;

    @Value("${qsf.store.redis.password:}")
    private String password;

    @Value("${qsf.store.redis.jedis.pool.max-total}")
    private Integer maxTotal;

    @Value("${qsf.store.redis.jedis.pool.max-idle}")
    private Integer maxIdle;

    @Value("${qsf.store.redis.jedis.pool.min-idle}")
    private Integer minIdle;

    @Value("${qsf.store.redis.jedis.pool.max-wait}")
    private Long maxWait;

    @Value("${qsf.store.redis.cluster.nodes:}")
    private String clusterNodes;

}