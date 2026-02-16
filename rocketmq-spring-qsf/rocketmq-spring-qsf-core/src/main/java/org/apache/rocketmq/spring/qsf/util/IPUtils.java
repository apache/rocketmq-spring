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

package org.apache.rocketmq.spring.qsf.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @desc
 **/

@Slf4j
public class IPUtils {
    private  static String staticIp;

    private  static String staticHost;

    static {
        InetAddress localHostAddress = getLocalHostAddress();

        // ip init
        staticIp = localHostAddress.getHostAddress();
        log.info("<qsf> local ip:{}", staticIp);

        // host init
        staticHost = localHostAddress.getHostName();
        log.info("<qsf> local host:{}", staticHost);
    }

    public static String getLocalHostName() {
        return staticHost;
    }

    public static String getLocalIp() {
        return staticIp;
    }

    public static InetAddress getLocalHostAddress() {
        try {
            InetAddress candidateAddr = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddrs = networkInterface.getInetAddresses();
                while (inetAddrs.hasMoreElements()) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (inetAddr.isLoopbackAddress()) {
                        continue;
                    }

                    if (inetAddr.isSiteLocalAddress()) {
                        return inetAddr;
                    }

                    if (candidateAddr == null) {
                        candidateAddr = inetAddr;
                    }
                }
            }

            return candidateAddr == null ? InetAddress.getLocalHost() : candidateAddr;
        } catch (Throwable e) {
            log.warn("<qsf> getLocalHostAddress fail", e);
        }
        return null;
    }
}