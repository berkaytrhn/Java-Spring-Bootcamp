package com.berkay.movies_web_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix="api")
public class ApiProperties {
	
	private String url;
	
	private String key;

}
