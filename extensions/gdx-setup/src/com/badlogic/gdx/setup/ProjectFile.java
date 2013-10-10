package com.badlogic.gdx.setup;

/**
 * A file in a {@link Project}, the resourceName specifies the location
 * of the template file, the outputName specifies the final name of the
 * file relative to its project, the isTemplate field specifies if 
 * values need to be replaced in this file or not.
 * @author badlogic
 *
 */
public class ProjectFile {
	/** the name of the template resource, relative to com.badlogic.gdx.setup.resources **/
	public String resourceName;
	/** the name of the output file, including directories, relative to the project dir **/
	public String outputName;
	/** whehter to replace values in this file **/
	public boolean isTemplate;
	
	public ProjectFile(String name) {
		this.resourceName = name;
		this.outputName = name;
		this.isTemplate = true;
	}
	
	public ProjectFile(String name, boolean isTemplate) {
		this.resourceName = name;
		this.outputName = name;
		this.isTemplate = isTemplate;
	}
	
	public ProjectFile(String resourceName, String outputName, boolean isTemplate) {
		this.resourceName = resourceName;
		this.outputName = outputName;
		this.isTemplate = isTemplate;		
	}
}
