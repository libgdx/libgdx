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

import java.util.List;
import java.util.Map;

public class ExternalExtension {

	private String name;
	private String[] gwtInherits;
	private String description;
	private String version;

	private Map<String, List<ExternalExtensionDependency>> dependencies;

	public ExternalExtension (String name, String[] gwtInherits, String description, String version) {
		this.name = name;
		this.gwtInherits = gwtInherits;
		this.description = description;
		this.version = version;
	}

	public void setDependencies (Map<String, List<ExternalExtensionDependency>> dependencies) {
		this.dependencies = dependencies;
	}

	public Dependency generateDependency () {
		Dependency dep = new Dependency(name, gwtInherits, getPlatformDependencies("core"), getPlatformDependencies("desktop"),
			getPlatformDependencies("android"), getPlatformDependencies("ios"), getPlatformDependencies("html"));

		return dep;
	}

	private String[] getPlatformDependencies (String platformName) {
		if (dependencies.get(platformName) == null) {
			return null;
		} else if (dependencies.get(platformName) != null && dependencies.get(platformName).size() == 0) {
			return new String[] {};
		} else {
			String[] arr = new String[dependencies.get(platformName).size()];
			for (int i = 0; i < dependencies.get(platformName).size(); i++) {
				ExternalExtensionDependency dependency = dependencies.get(platformName).get(i);
				if (dependency.external) {
					arr[i] = dependency.text;
				} else {
					String[] split = dependency.text.split(":");
					if (split.length == 3) {
						arr[i] = split[0] + ":" + split[1] + ":" + version + ":" + split[2];
					} else {
						arr[i] = dependency.text + ":" + version;
					}
				}
			}
			return arr;
		}
	}

	public String getName () {
		return name;
	}
}
