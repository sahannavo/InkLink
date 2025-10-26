package com.project.inklink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
//public class InkLinkApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(InkLinkApplication.class, args);
//    }
//
//}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InkLinkApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(InkLinkApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace(); // This will print the full stack trace
            System.exit(1);
        }
    }
}
