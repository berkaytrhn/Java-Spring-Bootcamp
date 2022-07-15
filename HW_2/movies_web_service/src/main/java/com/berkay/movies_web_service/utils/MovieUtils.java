package com.berkay.movies_web_service.utils;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.berkay.movies_web_service.model.Movie;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MovieUtils {
	

	public static Movie buildMovie(String[] fields) {
		// this.imdbID, this.Year, this.Title, this.Type, this.Poster
		return Movie.builder()
				.imdbID(fields[0])
				.Year(fields[1])
				.Title(fields[2])
				.Type(fields[3])
				.Poster(fields[4])
				.build();
	}
	
	public static String toCSV(Movie movie) {
		return String.format("%s,%s,%s,%s,%s", 
				movie.getImdbID(), 
				movie.getYear(), 
				movie.getTitle(), 
				movie.getType(), 
				movie.getPoster())
				+System.lineSeparator();
	}
	
	
	public static String jsonToMovieToCSV(JSONObject json) throws JsonMappingException, JsonProcessingException {
		String[] fields=new String[] {
				json.get("imdbID").toString(),
				json.get("Year").toString(),
				json.get("Title").toString(), 
				json.get("Type").toString(),
				json.get("Poster").toString()
		};		
		return toCSV(buildMovie(fields));
	}
	
	

	public static boolean checkForMovie(String path, String id) throws IOException {
		
		String data=FileHandler.read(path);
		
		for (String line:data.split("\\r?\\n")) {
			String movieID = line.split(",")[0];
			if (movieID.equals(id)){
				return true;
			}
		}
		return false;
	}
	
	public static Movie findMovieByID(String path, String id) throws IOException {
		String data=FileHandler.read(path);
				
		for (String line:data.split("\\r?\\n")) {
			String movieID = line.split(",")[0];
			if (movieID.equals(id)){
				return MovieUtils.buildMovie(line.split(","));
			}
		}
		return null;
	}

}
