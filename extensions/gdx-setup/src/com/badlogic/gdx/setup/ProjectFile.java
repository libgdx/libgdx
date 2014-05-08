/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
	/** the name of the template resource, relative to resourceLoc **/
	public String resourceName;
	/** the name of the output file, including directories, relative to the project dir **/
	public String outputName;
	/** whether to replace values in this file **/
	public boolean isTemplate;
	/** If the resource is from resource directory, or working dir **/
	public String resourceLoc = "/com/badlogic/gdx/setup/resources/";
	
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