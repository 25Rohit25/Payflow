package com.stripe.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PayflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayflowApplication.class, args);
    }
}
