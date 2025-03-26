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

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.qsf.util.QSFStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @desc QSFStateStoreBeans such as jedis bean
 */
@Configuration
@Slf4j
public class QSFStateStoreBeans {
    private final static String RESULT_OK = "OK";
    @Autowired
    private QSFStateStoreRedisConfigProperties redisConfigProperties;

    @Bean("qsfJedisClient")
    public QSFJedisClient qsfJedisClient() {
        log.info("<qsf> init qsfJedisClient config:{}", redisConfigProperties);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(redisConfigProperties.getMaxIdle());
        config.setMaxTotal(redisConfigProperties.getMaxTotal());
        config.setMaxWaitMillis(redisConfigProperties.getMaxWait());
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);

        QSFJedisClient qsfJedisClient;
        int timeout = redisConfigProperties.getTimeout();
        if (QSFStringUtils.isNotTrimEmpty(redisConfigProperties.getClusterNodes())) {
            Set<HostAndPort> nodes = new HashSet<>();
            for (String clusterNode : redisConfigProperties.getClusterNodes().split(",")) {
                String[] ipAndPort = clusterNode.trim().split(":");
                int port = Integer.parseInt(ipAndPort[1].trim());
                nodes.add(new HostAndPort(ipAndPort[0].trim(), port));
            }

            JedisCluster jedisCluster;
            if (QSFStringUtils.isNotTrimEmpty(redisConfigProperties.getPassword())) {
                jedisCluster = new JedisCluster(nodes, timeout, timeout, redisConfigProperties.getMaxAttempts(), redisConfigProperties.getPassword().trim(), "QSFStateStore", config);
            } else {
                jedisCluster = new JedisCluster(nodes, timeout, timeout, redisConfigProperties.getMaxAttempts(), config);
            }

            qsfJedisClient = new QSFJedisClusterClient();
            ((QSFJedisClusterClient)qsfJedisClient).setJedisCluster(jedisCluster);
        } else {
            JedisPool jedisPool;
            if (QSFStringUtils.isNotTrimEmpty(redisConfigProperties.getPassword())) {
                jedisPool = new JedisPool(config, redisConfigProperties.getHost(), redisConfigProperties.getPort(), timeout, redisConfigProperties.getPassword().trim());
            } else {
                jedisPool = new JedisPool(config, redisConfigProperties.getHost(), redisConfigProperties.getPort(), timeout);
            }

            qsfJedisClient = new QSFJedisPoolClient();
            ((QSFJedisPoolClient)qsfJedisClient).setJedisPool(jedisPool);
        }

        log.info("<qsf> init qsfJedisClient done:{}", qsfJedisClient);

        return qsfJedisClient;
    }
}