package com.sk.server.service;

import javax.servlet.http.HttpServletRequest;

import com.sk.server.exception.InvalidHeaderException;
import com.sk.server.model.RestResponse;

public interface RestService {

	RestResponse obtainGetResponse(HttpServletRequest reqeust);
	
	RestResponse obtainPostResponse(HttpServletRequest reqeust, String body) throws InvalidHeaderException;
	
}
