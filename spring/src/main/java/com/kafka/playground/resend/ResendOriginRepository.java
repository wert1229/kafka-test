package com.kafka.playground.resend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResendOriginRepository extends JpaRepository<ResendCheckOrigin, String> {

    List<ResendCheckOrigin> findTop1000ByIsCheckIsFalse();
}
