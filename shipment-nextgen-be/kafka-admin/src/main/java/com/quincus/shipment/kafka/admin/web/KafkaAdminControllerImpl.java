package com.quincus.shipment.kafka.admin.web;

import com.quincus.shipment.kafka.admin.KafkaAdminController;
import com.quincus.shipment.kafka.admin.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kafka/topics")
@AllArgsConstructor
public class KafkaAdminControllerImpl implements KafkaAdminController {

    private final AdminService adminService;

    @Override
    public String create(final String name) {
        adminService.createTopic(name);
        return "Topic " + name + " Created";
    }

    @Override
    public List<String> listAllTopic() {
        return adminService.getTopics();
    }

    @Override
    public String deleteTopic(final String name) {
        adminService.deleteTopic(name);
        return "Topic " + name + " Deleted!";
    }
    
}
