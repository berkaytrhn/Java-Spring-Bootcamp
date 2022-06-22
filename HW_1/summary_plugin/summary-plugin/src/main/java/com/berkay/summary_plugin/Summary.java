package com.berkay.summary_plugin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


import org.apache.maven.model.Plugin;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name= "summarize", defaultPhase = LifecyclePhase.COMPILE)
public class Summary extends AbstractMojo{

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject mavenProject;
	
	@Parameter(defaultValue = "${project.build.directory}", required=true)
	private String outputFile;
	
	private final String FILE_START="----------Summary----------\n"; 
	private final String LINE_START="\n";
	

	public void execute() throws MojoExecutionException, MojoFailureException{			
		
		List<Developer> developers = mavenProject.getDevelopers();
		List<Dependency> dependencies = mavenProject.getDependencies();
		List<Plugin> plugins = mavenProject.getBuildPlugins();
		
		String groupID = mavenProject.getGroupId();
		String artifactID = mavenProject.getArtifactId();
		String version = mavenProject.getVersion();
		String releaseDate = mavenProject.getProperties().getProperty("release.date");
		
		
		// configuring developer information part
		int counter=1;
		String developerInfo=String.format("%s%s", LINE_START, "\n---Developers---\n");
		for(Developer developer: developers){
			developerInfo+=String.format("\t->Developer %d Name: %s\n", counter++, developer.getName());
		}
		
		
		// configuring project info
		String projectInfo = String.format("%s%s", LINE_START, "\n---Project Info---\n");
		projectInfo+=String.format("\t->%s.%s.%s\n", groupID, artifactID, version);
		
		//configuration dependencies
		String dependenciesInfo = String.format("%s%s", LINE_START, "\n---Dependencies---\n");
		for(Dependency dependency: dependencies) {
			dependenciesInfo+=String.format("\t->Dependency: %s.%s\n", dependency.getGroupId(), dependency.getArtifactId());
		}
		
		//configuration plugin info
		String pluginInfo=String.format("%s%s", LINE_START, "\n---Plugins---\n");
		for(Plugin plugin: plugins) {
			pluginInfo+=String.format("\t->Plugin: %s\n", plugin.getArtifactId());
		}
		
		//release date info
		String releaseString = String.format("\n->%s%s\n", "Release Date: ", releaseDate);
		
		String data=String.format("%s%s%s%s%s%s", FILE_START, projectInfo, developerInfo, releaseString, dependenciesInfo, pluginInfo);
		
		
		// log print
		getLog().info("Hello World!");
		
		outputFile+="\\summary.txt";
		
		// file writing part
		try {
			MyFileWriter.writeFile(outputFile, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
