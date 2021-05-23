package com.kafka.playground.resend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendCheckRemote {

    @Id
    private String id;

    @Column(columnDefinition = "bit(1) default 0")
    private boolean isCheck;
}
