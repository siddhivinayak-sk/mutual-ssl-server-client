package com.sk.server.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sk.server.exception.InvalidHeaderException;
import com.sk.server.model.RestResponse;
import com.sk.server.service.RestService;
import com.sk.server.util.RequestValidationUtil;

@Service
public class RestServiceImpl implements RestService {
	
	private static final Logger LOGGER = LogManager.getLogger(RestServiceImpl.class);
	
	@Value("${header.validation.required:false}")
	private boolean headerValidationRequired;

	@Override
	public RestResponse obtainGetResponse(HttpServletRequest reqeust) {
		LOGGER.info("RestServiceImpl.obtainGetResponse");
		return new RestResponse("Successfull!", 200);
	}

	@Override
	public RestResponse obtainPostResponse(HttpServletRequest reqeust, String body) throws InvalidHeaderException {
		LOGGER.info("RestServiceImpl.obtainPostResponse");
		if(headerValidationRequired) {
			RequestValidationUtil.parseRequest(reqeust, body);
		}
		return new RestResponse("Successfull!", 200);
	}
}
