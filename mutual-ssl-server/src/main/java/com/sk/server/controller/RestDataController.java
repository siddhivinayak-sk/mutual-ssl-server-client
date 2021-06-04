package com.sk.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sk.server.exception.InvalidHeaderException;
import com.sk.server.service.RestService;

@RestController
@RequestMapping("/test")
public class RestDataController {

	private static final Logger LOGGER = LogManager.getLogger(RestDataController.class);
	
	@Autowired
	private RestService restService;
	
	@GetMapping
	public ResponseEntity<?> get(HttpServletRequest request) {
		LOGGER.info("RestDataController:get");
		return new ResponseEntity<>(restService.obtainGetResponse(request), HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<?> post(HttpServletRequest request, @RequestBody String body) throws InvalidHeaderException {
		LOGGER.info("RestDataController:get");
		return new ResponseEntity<>(restService.obtainPostResponse(request, body), HttpStatus.OK);
	}
	
	
}