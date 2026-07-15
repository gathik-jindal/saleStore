package com.salestop.orders;

import com.salestop.common.events.EventTopics;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
class KafkaErrorConfig {
  @Bean
  ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ConsumerFactory<Object, Object> consumerFactory,
      KafkaTemplate<Object, Object> template) {
    var factory = new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, consumerFactory);
    var recoverer =
        new DeadLetterPublishingRecoverer(
            template,
            (record, ex) -> new TopicPartition(EventTopics.ORDER_INTENTS_DLQ, record.partition()));
    factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000, 4)));
    return factory;
  }
}
