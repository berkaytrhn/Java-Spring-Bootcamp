package com.berkay.banking_system.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaAdminConfiguration {

		@Value("${kafka.address}")
		private String kafkaAddress;
	
		@Bean
		public KafkaAdmin kafkaAdmin() {
			Map<String, Object> configs = new HashMap<>();
			configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
			return new KafkaAdmin(configs);
		}
		
		@Bean
		public NewTopic newTopic() {
			return new NewTopic("log", 1, (short) 1);
		}
}
