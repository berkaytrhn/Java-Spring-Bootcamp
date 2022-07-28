package com.berkay.banking_system.currencyconversion;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public interface ICurrencyConverter {
	public double convertCurrency(double amount, String currencyTypeOne, String currencyTypeTwo) throws IOException, InterruptedException, ParseException;
}
