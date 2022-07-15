package com.berkay.movies_web_service.utils;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public interface IRequestHandler {
	public JSONObject baseRequest(String customPart) throws IOException, InterruptedException, ParseException;
	public Object getRequest(String customPart) throws IOException, InterruptedException, ParseException;
	public JSONObject postRequest(String customPart) throws IOException, InterruptedException, ParseException;
}
