package com.sk.server.model;

public class RestResponse {
	
	private String message;
	private int status;
	
	public RestResponse() {
		super();
	}
	
	public RestResponse(String message, int status) {
		super();
		this.message = message;
		this.status = status;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
