package com.berkay.banking_system.currencyconversion;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Qualifier("currency_api_converter")
public class CollectApiCurrencyConverter implements ICurrencyConverter {


	@Value("${api.currency.url}")
	private String apiURL;
	// string formatters predefined as follows, to(%s), from(%s), amount(%f)
	
	@Value("${api.currency.key}")
	private String key;
	
	private HashMap<String, String> currencyMap;
	private JSONParser jsonParser;
	
	
	public CollectApiCurrencyConverter(@Value("${currencies}") String currencies) {
		this.currencyMap=currencyMapper(currencies.split(","));
		this.jsonParser=new JSONParser();
	}
	
	
	public static HashMap<String, String> currencyMapper(String[] types) {
		@SuppressWarnings("serial")
		HashMap<String, String> map = new HashMap<>() {
			{
				put(types[0], "USD");
				put(types[1], "TRY");
				put(types[2], "XAU");
			}
		};
		return map;
		
	}
	
	@Override
	public double convertCurrency(double amount, String currencyTypeOne, String currencyTypeTwo) throws IOException, InterruptedException, ParseException {
		// to,from,amount
		String url = String.format(this.apiURL, this.currencyMap.get(currencyTypeTwo), this.currencyMap.get(currencyTypeOne), amount);
		
		HttpClient client = HttpClient.newHttpClient();
			
		HttpRequest request= HttpRequest.newBuilder()
				.GET()
				.header("Content-Type", "application/json")
				.header("apikey", this.key)
				.uri(URI.create(url))
				.build();
		
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		JSONObject json = (JSONObject) this.jsonParser.parse(response.body());
		return Double.parseDouble(json.get("result").toString());
	}

}
