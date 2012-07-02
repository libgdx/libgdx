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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectConfiguration {
	public String projectName = "my-gdx-game";
	public String mainClassName = "MyGdxGame";
	public String packageName = "com.me.mygdxgame";
	public String destinationPath = "";

	public final Libraries libs = new Libraries();
	private final Map<String, LibraryDef> librariesDefs = new HashMap<String, LibraryDef>();
	private final Map<String, Boolean> librariesUsages = new HashMap<String, Boolean>();
	private final Map<String, String> librariesPaths = new HashMap<String, String>();

	public boolean isDesktopIncluded = true;
	public boolean isAndroidIncluded = true;
	public boolean isHtmlIncluded = true;
	public String commonSuffix = "";
	public String desktopSuffix = "-desktop";
	public String androidSuffix = "-android";
	public String htmlSuffix = "-html";
	public String androidMinSdkVersion = "5";
	public String androidTargetSdkVersion = "15";
	public String androidMaxSdkVersion = "";

	// -------------------------------------------------------------------------

	public class Libraries {
		public void add(String name, LibraryDef def) {
			librariesDefs.put(name, def);
			if (!librariesUsages.containsKey(name)) librariesUsages.put(name, Boolean.FALSE);
			if (!librariesPaths.containsKey(name)) librariesPaths.put(name, null);
		}

		public void setUsage(String name, boolean used) {librariesUsages.put(name, used);}
		public void setPath(String name, String path) {librariesPaths.put(name, path);}

		public List<String> getNames() {return new ArrayList<String>(librariesDefs.keySet());}
		public LibraryDef getDef(String name) {return librariesDefs.get(name);}
		public boolean isUsed(String name) {return librariesUsages.get(name);}
		public String getPath(String name) {return librariesPaths.get(name);}
	}
}