package com.berkay.movies_web_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.berkay.movies_web_service.properties.ApiProperties;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApplicationConfiguration {

}
