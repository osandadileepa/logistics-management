package com.quincus.shipment.kafka.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AdminService {

    public static final String ERROR_IN_LISTING_TOPICS = "Error in listing topics: ";
    public static final String ERROR_IN_CREATING_A_TOPIC = "Error in creating a topic: ";
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private AdminClient getAdminClient() {
        return AdminClient.create(kafkaTemplate.getProducerFactory().getConfigurationProperties());
    }

    public void deleteTopic(String topic) {
        DeleteTopicsResult deleteTopicsResult = getAdminClient()
                .deleteTopics(Collections.singleton(topic));
        boolean isDone = false;
        while (!isDone) {
            isDone = deleteTopicsResult.all().isDone();
        }
    }

    public List<String> getTopics() {
        List<String> topicList = new ArrayList<>();
        try {
            ListTopicsOptions options = new ListTopicsOptions();
            options.listInternal(true);
            Collection<TopicListing> list = getAdminClient().listTopics(options).listings().get();
            list.forEach(e -> topicList.add(e.name()));
            return topicList;
        } catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error(ERROR_IN_LISTING_TOPICS + e.getMessage(), e);
        }
        return topicList;
    }

    public void createTopic(String topicName) {
        try {
            Admin admin = getAdminClient();
            int partitions = 1;
            short replicationFactor = 1;
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            admin.createTopics(Collections.singleton(newTopic));
        } catch (Exception e) {
            log.error(ERROR_IN_CREATING_A_TOPIC + e.getMessage(), e);
        }
    }
}
