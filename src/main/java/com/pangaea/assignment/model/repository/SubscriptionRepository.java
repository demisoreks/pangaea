package com.pangaea.assignment.model.repository;

import java.util.List;

import com.pangaea.assignment.model.entity.Subscription;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByTopic(String topic);
    
}
