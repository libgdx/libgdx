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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {
	private static final String NL = System.lineSeparator();
	DependencyBank bank;
	List<ProjectType> modules = new ArrayList<ProjectType>();
	List<Dependency> dependencies = new ArrayList<Dependency>();
	File settingsFile;
	File buildFile;

	public ProjectBuilder (DependencyBank bank) {
		this.bank = bank;
	}

	public List<String> buildProject (List<ProjectType> projects, List<Dependency> dependencies) {
		List<String> incompatibilities = new ArrayList<String>();
		for (Dependency dep : dependencies) {
			for (ProjectType type : projects) {
				dep.getDependencies(type);
				incompatibilities.addAll(dep.getIncompatibilities(type));
			}
		}
		this.modules = projects;
		this.dependencies = dependencies;
		return incompatibilities;
	}

	public boolean build (Language language, String appName) throws IOException {
		settingsFile = File.createTempFile("libgdx-setup-settings", ".gradle");
		buildFile = File.createTempFile("libgdx-setup-build", ".gradle");
		if (!settingsFile.exists()) {
			settingsFile.createNewFile();
		}
		if (!buildFile.exists()) {
			buildFile.createNewFile();
		}
		settingsFile.setWritable(true);
		buildFile.setWritable(true);
		try (FileWriter settingsWriter = new FileWriter(settingsFile.getAbsoluteFile());
			BufferedWriter settingsBw = new BufferedWriter(settingsWriter);
			FileWriter buildWriter = new FileWriter(buildFile.getAbsoluteFile());
			BufferedWriter buildBw = new BufferedWriter(buildWriter)) {

			StringBuilder settingsContent = new StringBuilder();
			settingsContent.append("rootProject.name = '").append(appName).append("'").append(NL);

			settingsContent.append("include ");
			for (ProjectType module : modules) {
				settingsContent.append("'").append(module.getName()).append("'");
				if (modules.indexOf(module) != modules.size() - 1) {
					settingsContent.append(", ");
				}
			}
			settingsContent.append(NL);

			settingsBw.write(settingsContent.toString());

			BuildScriptHelper.addBuildScript(language, modules, buildBw);
			BuildScriptHelper.addAllProjects(buildBw);
			for (ProjectType module : modules) {
				BuildScriptHelper.addProject(language, module, dependencies, buildBw);
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void cleanUp () {
		settingsFile.deleteOnExit();
		buildFile.deleteOnExit();
	}

}
