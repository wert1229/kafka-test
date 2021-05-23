package com.kafka.playground.resend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResendFailRepository extends JpaRepository<ResendCheckFinalFail, String> {

}
