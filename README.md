# RocketMQ-Spring [![Build Status](https://travis-ci.org/apache/rocketmq-spring.svg?branch=master)](https://travis-ci.org/apache/rocketmq-spring) [![Coverage Status](https://coveralls.io/repos/github/apache/rocketmq-spring/badge.svg?branch=master)](https://coveralls.io/github/apache/rocketmq-spring?branch=master)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.rocketmq/rocketmq-spring-all/badge.svg)](https://search.maven.org/search?q=g:org.apache.rocketmq%20AND%20a:rocketmq-spring-all)
[![GitHub release](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/apache/rocketmq-spring/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

This project aims to help developers quickly integrate [RocketMQ](http://rocketmq.apache.org/) with [Spring Boot](http://projects.spring.io/spring-boot/). 

## Features

- [x] Send messages synchronously
- [x] Send messages asynchronously
- [x] Send messages in one-way mode
- [x] Send messages orderly
- [x] Send messages in batch
- [x] Send transactional messages
- [x] Send scheduled messages with delay level
- [x] Consume messages with concurrently mode (broadcasting/clustering)
- [x] Consume messages with orderly mode
- [x] Support message filter with tag and sql
- [x] Support message tracing capability
- [x] Authentication and authorisation
- [ ] Request-reply mode

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

Please see the [rocketmq-spring-boot-samples](rocketmq-spring-boot-samples)

## User Guide

Please see the [wiki](https://github.com/apache/rocketmq-spring/wiki) page


## Contributing

We are always very happy to have contributions, whether for trivial cleanups or big new features. Please see the RocketMQ main website to read [details](http://rocketmq.apache.org/docs/how-to-contribute/)

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) Copyright (C) Apache Software Foundation 