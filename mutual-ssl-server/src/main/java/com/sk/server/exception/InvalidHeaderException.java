package com.sk.server.exception;

public class InvalidHeaderException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidHeaderException() {
		super();
	}
	
	public InvalidHeaderException(String message) {
		super(message);
	}
}
