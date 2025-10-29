package com.project.inklink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(EmailProperties props) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(props.getHost());
        mailSender.setPort(props.getPort());
        mailSender.setUsername(props.getUsername());
        mailSender.setPassword(props.getPassword());

        Properties javaMailProps = mailSender.getJavaMailProperties();
        javaMailProps.put("mail.transport.protocol", "smtp");
        javaMailProps.put("mail.smtp.auth", "true");
        javaMailProps.put("mail.smtp.starttls.enable", String.valueOf(props.isTls()));
        javaMailProps.put("mail.debug", "false");

        return mailSender;
    }
}


