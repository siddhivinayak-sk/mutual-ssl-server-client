package com.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.http.configs.HttpProperties;
import com.http.service.HttpCallService;
/**
 * Application class
 * @author Sandeep Kumar
 *
 */
@SpringBootApplication(scanBasePackages = "com.http.*")
@EnableConfigurationProperties(HttpProperties.class)
public class Application implements CommandLineRunner  {

	private static final Logger LOGGER = LogManager.getLogger(Application.class);	
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Autowired
	private HttpProperties httpProperties;
	
	@Autowired
	private HttpCallService httpCallService;
	
	@Override
	public void run(String...args) throws Exception {
		httpProperties.getCalls().stream().forEach(call -> {
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info("Call Started - {}", call.getHttpUrl());
			httpCallService.makeCall(call);
			LOGGER.info("Call End - {}", call.getHttpUrl());
			LOGGER.info("");
			LOGGER.info("");
			LOGGER.info("");
			});
	}
}
