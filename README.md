# RocketMQ-Spring

[![Build Status](https://travis-ci.org/apache/rocketmq-spring.svg?branch=master)](https://travis-ci.org/apache/rocketmq-spring)
[![Coverage Status](https://coveralls.io/repos/github/apache/rocketmq-spring/badge.svg?branch=master)](https://coveralls.io/github/apache/rocketmq-spring?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.rocketmq/rocketmq-spring-all/badge.svg)](https://search.maven.org/search?q=g:org.apache.rocketmq%20AND%20a:rocketmq-spring-all)
[![GitHub release](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/apache/rocketmq-spring/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/apache/rocketmq-spring.svg)](http://isitmaintained.com/project/apache/rocketmq-spring "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/apache/rocketmq-spring.svg)](http://isitmaintained.com/project/apache/rocketmq-spring "Percentage of issues still open")

This project aims to help developers quickly integrate [RocketMQ](http://rocketmq.apache.org/) with [Spring Boot](http://projects.spring.io/spring-boot/).

## Features

- [x] Send messages synchronously
- [x] Send messages asynchronously
- [x] Send messages in one-way mode
- [x] Send ordered messages
- [x] Send batched messages
- [x] Send transactional messages
- [x] Send scheduled messages with delay level
- [x] Consume messages with concurrent mode (broadcasting/clustering)
- [x] Consume ordered messages
- [x] Filter messages using the tag or sql92 expression
- [x] Support message tracing
- [x] Support authentication and authorization
- [x] Support request-reply message exchange pattern
- [x] Consume messages with push/pull mode

## Prerequisites

- JDK 1.8 and above
- [Maven](http://maven.apache.org/) 3.0 and above
- Spring Boot 2.0 and above

## Usage

Add a dependency using maven:

```xml
<!--add dependency in pom.xml-->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## Samples

Please see the [rocketmq-spring-boot-samples](rocketmq-spring-boot-samples).

## User Guide

Please see the [wiki](https://github.com/apache/rocketmq-spring/wiki) page.

## Contributing

We are always very happy to have contributions, whether for trivial cleanups or big new features. Please see the RocketMQ main website to read the [details](http://rocketmq.apache.org/docs/how-to-contribute/).

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) Apache Software Foundation 
