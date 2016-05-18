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

import com.badlogic.gdx.setup.DependencyBank.ProjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dependency {

	private HashMap<ProjectType, String[]> subDependencyMap = new HashMap<ProjectType, String[]>();
	private String[] gwtInherits;
	private String name;

	public Dependency (String name, String[] gwtInherits, String[]... subDependencies) {
		this.name = name;
		this.gwtInherits = gwtInherits;
		for (ProjectType type : ProjectType.values()) {
			subDependencyMap.put(type, subDependencies[type.ordinal()]);
		}
	}

	public String[] getDependencies (ProjectType type) {
		return subDependencyMap.get(type);
	}

	public List<String> getIncompatibilities (ProjectType type) {
		List<String> incompat = new ArrayList<String>();
		String[] subArray = subDependencyMap.get(type);
		if (subArray == null) {
			incompat.add("Dependency " + name + " is not compatible with sub module " + type.getName().toUpperCase());
		}
		return incompat;
	}

	public String[] getGwtInherits () {
		return gwtInherits;
	}
	
	public String getName () {
		return name;
	}

	@Override
	public boolean equals (Object obj) {
		if (obj instanceof Dependency) {
			if (((Dependency)obj).getName().equals(getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode () {
		return name.hashCode();
	}
}
