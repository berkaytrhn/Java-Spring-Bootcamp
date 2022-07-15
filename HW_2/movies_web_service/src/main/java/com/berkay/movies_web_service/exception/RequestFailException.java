package com.berkay.movies_web_service.exception;

public class RequestFailException extends Exception {

	private static final long serialVersionUID = 1L;

	public RequestFailException(String message) {
		// TODO Auto-generated constructor stub
		super(message);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Request Failed!!");
	}
}
