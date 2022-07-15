package com.berkay.movies_web_service.utils;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.berkay.movies_web_service.exception.RequestFailException;
import com.berkay.movies_web_service.properties.ApiProperties;

@Component
public class RequestHandler implements IRequestHandler{
	
	private JSONParser jsonParser;
	private ApiProperties apiProperties;
	
	@Autowired
	public RequestHandler(ApiProperties apiProperties) {
		// TODO Auto-generated constructor stub
		this.jsonParser=new JSONParser();
		this.apiProperties=apiProperties;
	}
	
	public JSONObject baseRequest(String customPart) throws IOException, InterruptedException, ParseException {
		String url = String.format("%s%s", this.apiProperties.getUrl(), customPart);
		
		HttpClient client = HttpClient.newHttpClient();
		
		HttpRequest request= HttpRequest.newBuilder()
				.GET()
				.header("Content-Type", "application/json")
				.header("Authorization", this.apiProperties.getKey())
				.uri(URI.create(url))
				.build();
		
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		JSONObject json = (JSONObject) this.jsonParser.parse(response.body());
		
		
		
		boolean success = (boolean) json.get("success");
		
		try {
			if (!success) throw new RequestFailException("Request Failed!!");
		}catch(RequestFailException exception) {
			System.out.println(exception);
			return null;
		}
		
		return json;
	}
	
	public JSONObject postRequest(String customPart) throws IOException, InterruptedException, ParseException {
		JSONObject json= baseRequest(customPart);
		if (json==null) return json; else return (JSONObject) json.get("result");
	}
	
	public Object getRequest(String customPart) throws IOException, InterruptedException, ParseException {
		JSONObject json= baseRequest(customPart);
		if(json==null) return json;else return (Object) json.get("result");
	}
	
	

}
