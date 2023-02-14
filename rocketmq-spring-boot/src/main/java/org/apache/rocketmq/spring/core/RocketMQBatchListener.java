package org.apache.rocketmq.spring.core;

import java.util.List;

public interface RocketMQBatchListener<T> {
  void onMessages(List<T> message);
}
