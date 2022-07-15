package com.berkay.movies_web_service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movie {
	private String Title;
	private String Year;
	private String imdbID;
	private String Type;
	private String Poster;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("%s -> %s,%s movie in %s, %s", this.imdbID, this.Title, this.Type, this.Year, this.Poster);
	}
	
}
