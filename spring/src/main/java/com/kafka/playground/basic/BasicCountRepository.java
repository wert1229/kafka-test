package com.kafka.playground.basic;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicCountRepository extends JpaRepository<BasicCount, Long> {

}
