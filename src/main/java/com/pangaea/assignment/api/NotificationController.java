package com.pangaea.assignment.api;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pangaea.assignment.data.SubscribeObject;
import com.pangaea.assignment.model.entity.Subscription;
import com.pangaea.assignment.model.repository.SubscriptionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class NotificationController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping("/subscribe/{topic}")
    public ResponseEntity<ObjectNode> subscribe(@PathVariable String topic, @RequestBody SubscribeObject body) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        
        try {
            // Store url/topic matching

            response.put("url", body.getUrl());
            response.put("topic", topic);

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            response.put("error", "Something went wrong!");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/publish/{topic}")
    public ResponseEntity<ObjectNode> publish(@PathVariable String topic, @RequestBody ObjectNode message) {
        // Get all subscribers
        List<Subscription> subscriptions = subscriptionRepository.findByTopic(topic);

        String result;

        ObjectMapper objectMapper = new ObjectMapper();

        int failures = 0;
        for (Subscription subscription : subscriptions) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                ObjectNode body = objectMapper.createObjectNode();
                body.put("topic", topic);
                body.putPOJO("data", message);

                HttpEntity<String> request =  new HttpEntity<String>(body.toString(), headers);

                ResponseEntity<Object> subscriberResponse = restTemplate.exchange(subscription.getUrl(), HttpMethod.POST, request, Object.class);

                if (subscriberResponse.getStatusCode() != HttpStatus.OK) {
                    failures ++;
                }
            } catch (Exception e) {
                failures ++;
            }
        }

        if (failures == 0) {
            result = "Sent to all.";
        } else {
            result = "Sent to some, others failed.";
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("result", result);

        return ResponseEntity.ok().body(response);
    }
    
}
