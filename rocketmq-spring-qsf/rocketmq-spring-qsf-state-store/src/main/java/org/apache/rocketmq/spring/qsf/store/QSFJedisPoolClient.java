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

import org.apache.rocketmq.spring.qsf.util.InvokeUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

/**
 * @desc
 */
public class QSFJedisPoolClient implements QSFJedisClient {
    private JedisPool jedisPool;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    private Jedis getJedis() {
        return jedisPool.getResource();
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = getJedis()) {
            return (String) InvokeUtils.invokeWithRetry(jedis, "get",
                    new Class[]{String.class},
                    new Object[]{key});
//            return jedis.get(key);
        }
    }

    @Override
    public String set(String key, String value, SetParams params) {
        try (Jedis jedis = getJedis()) {
            return (String) InvokeUtils.invokeWithRetry(jedis, "set",
                    new Class[]{String.class, String.class, SetParams.class},
                    new Object[]{key, value, params});
//            return jedis.set(key, value, params);
        }
    }

    @Override
    public Boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return (Boolean) InvokeUtils.invokeWithRetry(jedis, "exists",
                    new Class[]{String.class},
                    new Object[]{key});
//            return jedis.exists(key);
        }
    }

    @Override
    public Long del(String key) {
        try (Jedis jedis = getJedis()) {
            return (Long) InvokeUtils.invokeWithRetry(jedis, "del",
                    new Class[]{String.class},
                    new Object[]{key});
//            return jedis.del(key);
        }
    }

}