package com.dbapp.data.generater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
描述:
数据生成器
 *
@author lenghl
@create 2018-11-07 16:21
 */
public class DataGenerater {
    private static final Logger logger = LogManager.getLogger(DataGenerater.class);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        logger.info("dataGenerater start to run");

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer(props);
        for(int i = 200; i < 40000; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", "name_" + i);
            map.put("user", "user_" + i);
            map.put("timestamp", "timestamp_" + i);
            producer.send(new ProducerRecord<String, String>("topicA", Integer.toString(i), objectMapper.writeValueAsString(map)));
            Thread.sleep(10);
        }
        producer.close();
    }
}
