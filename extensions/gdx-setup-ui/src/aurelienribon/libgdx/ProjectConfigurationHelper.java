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
package aurelienribon.libgdx;

import java.io.File;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectConfigurationHelper {
	public static boolean isValid(ProjectConfiguration cfg) {
		if (cfg.projectName.trim().equals("")) return false;
		if (cfg.packageName.trim().equals("")) return false;
		if (cfg.packageName.endsWith(".")) return false;
		if (cfg.mainClassName.trim().equals("")) return false;

		for (String libraryName : cfg.libs.getNames()) {
			if (!isLibraryValid(cfg, libraryName)) return false;
		}

		return true;
	}

	public static boolean isLibraryValid(ProjectConfiguration cfg, String libraryName) {
		if (!cfg.libs.isUsed(libraryName)) return true;
		String path = cfg.libs.getPath(libraryName);
		if (path == null) return false;
		if (!path.endsWith(".zip")) return false;
		if (!new File(path).isFile()) return false;
		return true;
	}

	public static String getErrorMessage(ProjectConfiguration cfg) {
		if (cfg.projectName.trim().equals("")) return "Project name is not set.";
		if (cfg.packageName.trim().equals("")) return "Package name is not set.";
		if (cfg.packageName.endsWith(".")) return "Package name ends with a dot.";
		if (cfg.mainClassName.trim().equals("")) return "Main class name is not set.";

		for (String libraryName : cfg.libs.getNames()) {
			if (!isLibraryValid(cfg, libraryName))
				return "At least one selected library has a missing or invalid archive.";
		}

		return "No error found";
	}
}