package com.berkay.movies_web_service.controller;

import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.berkay.movies_web_service.exception.RequestFailException;
import com.berkay.movies_web_service.model.Movie;
import com.berkay.movies_web_service.utils.FileHandler;
import com.berkay.movies_web_service.utils.IRequestHandler;
import com.berkay.movies_web_service.utils.MovieUtils;

@RestController
public class MovieController {

	private IRequestHandler requestHandler;
	

	@Value("${storage.file}")
	private String path;
	
	@Autowired
	public MovieController(IRequestHandler requestHandler) {
		super();
		this.requestHandler = requestHandler;
	}

	@GetMapping("/movies/search")
	public ResponseEntity<Object> searchMovie(@RequestParam(name= "name", required = true) String name) throws RequestFailException, IOException, InterruptedException, ParseException {
		String customPart=String.format("imdbSearchByName?query=%s", name);
		
		Object res=this.requestHandler.getRequest(customPart);
		
		if (res==null) return new ResponseEntity<Object>("Wrong Request!!", HttpStatus.BAD_REQUEST); 
		else return new ResponseEntity<Object>(res, HttpStatus.OK);
	}
	
	@PostMapping("/movies/saveToList/{id}")
	public ResponseEntity<String> saveToList(@PathVariable(name="id")String id) throws IOException, InterruptedException, ParseException {		//List<Movie>
		// check existing file for possible data
		FileHandler.checkForFile(this.path);
		
		if(MovieUtils.checkForMovie(this.path, id)) {
			return new ResponseEntity<String>("Movie Exists!!", HttpStatus.ACCEPTED);// response entity don
		}
		
		
		// request
		System.out.println("Not found, adding...");
		String customPart = String.format("imdbSearchById?movieId=%s", id);
		
		JSONObject res=this.requestHandler.postRequest(customPart);
		if (res==null) return new ResponseEntity<String>("Wrong Request!!", HttpStatus.BAD_REQUEST); 
		
		
		String convertedString=MovieUtils.jsonToMovieToCSV(res);
		
		//file writing
		FileHandler.write(this.path, convertedString);
		
		
		return new ResponseEntity<String>("Successful!!", HttpStatus.OK);
	}
	
	
	@PostMapping("/movies/detail/{id}")
	public ResponseEntity<Object> detail(@PathVariable("id") String id) throws IOException, InterruptedException, ParseException{
		//checkForFile
		FileHandler.checkForFile(this.path);
		
		Movie movie=MovieUtils.findMovieByID(this.path, id);
		if (movie!= null) return new ResponseEntity<Object>(movie, HttpStatus.OK); ;
		
		// else send request
		System.out.println("Did not find locally, retrieving from remote...");
		String customPart = String.format("imdbSearchById?movieId=%s", id);
		
		JSONObject res=this.requestHandler.postRequest(customPart);
		if (res==null) return new ResponseEntity<Object>("Wrong Request!!", HttpStatus.BAD_REQUEST); 

		
		String convertedString=MovieUtils.jsonToMovieToCSV(res);
		
		Movie newMovie=MovieUtils.buildMovie(convertedString.split(","));
		
		return new ResponseEntity<Object>(
				newMovie, 
				HttpStatus.OK
		);
	}
	
}
