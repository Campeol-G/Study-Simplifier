package com.studys.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.studys")
@EnableJpaRepositories(basePackages = "com.studys.db.repository")
@EntityScan(basePackages = "com.studys.db.entity")
public class StudySApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudySApplication.class, args);
    }

}
