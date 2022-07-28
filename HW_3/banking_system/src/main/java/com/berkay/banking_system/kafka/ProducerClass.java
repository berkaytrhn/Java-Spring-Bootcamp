package com.berkay.banking_system.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProducerClass {
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	public void sendLogMessage(String logMessage) {
		this.kafkaTemplate.send("log", logMessage);
	}
}
