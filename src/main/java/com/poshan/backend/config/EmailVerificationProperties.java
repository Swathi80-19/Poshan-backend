package com.poshan.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth.email-verification")
public class EmailVerificationProperties {

    private String fromAddress = "";

    private Integer tokenTtlMinutes = 30;
}
