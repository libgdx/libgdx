package com.badlogic.gdx.setup;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes all the files required to generate a
 * new project for an Application. Files are found
 * on the classpath of the gdx-setup project, see
 * package com.badlogic.gdx.setup.resources.
 * @author badlogic
 *
 */
public class Project {
	/** list of files, relative to project directory **/
	public List<ProjectFile> files = new ArrayList<ProjectFile>();
}
