package com.kafka.playground.resend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendCheckOrigin {

    @Id
    private String id;

    @Column(columnDefinition = "bit(1) default 0")
    private boolean isCheck;

    public void mark() {
        this.isCheck = true;
    }
}
