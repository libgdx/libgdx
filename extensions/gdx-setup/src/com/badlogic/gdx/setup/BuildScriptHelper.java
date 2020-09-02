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
import java.io.IOException;
import java.util.List;

public class BuildScriptHelper {

	private static int indent = 0;

	public static void addBuildScript(Language language, List<ProjectType> projects, BufferedWriter wr) throws IOException {
		write(wr, "buildscript {");
		write(wr, language.buildScript + "\n");
		//repos
		write(wr, "repositories {");
		write(wr, DependencyBank.mavenLocal);
		write(wr, DependencyBank.mavenCentral);
		write(wr, "maven { url \"" + DependencyBank.gradlePlugins + "\" }");
		write(wr, "maven { url \"" + DependencyBank.libGDXSnapshotsUrl + "\" }");
		write(wr, DependencyBank.jCenter);
		write(wr, DependencyBank.google);
		write(wr, "}");
		//dependencies
		write(wr, "dependencies {");
		if (projects.contains(ProjectType.HTML)) {
			write(wr, "classpath '" + DependencyBank.gwtPluginImport + "'");
		}
		if (projects.contains(ProjectType.ANDROID)) {
			write(wr, "classpath '" + DependencyBank.androidPluginImport + "'");
		}
		if (projects.contains(ProjectType.IOS)) {
			write(wr, "classpath '" + DependencyBank.roboVMPluginImport + "'");
		}
		if (projects.contains(ProjectType.IOSMOE)) {
			write(wr, "classpath '" + DependencyBank.moePluginImport + "'");
		}
		write(wr, language.buildScriptDependencies + "\n");
		write(wr, "}");
		write(wr, "}");
		space(wr);
	}

	public static void addAllProjects(BufferedWriter wr) throws IOException {
		write(wr, "allprojects {");
		write(wr, "apply plugin: \"eclipse\"");
		space(wr);
		write(wr, "version = '1.0'");
		write(wr, "ext {");
		write(wr, "appName = \"%APP_NAME%\"");
		write(wr, "gdxVersion = '" + DependencyBank.libgdxVersion + "'");
		write(wr, "roboVMVersion = '" + DependencyBank.roboVMVersion + "'");
		write(wr, "box2DLightsVersion = '" + DependencyBank.box2DLightsVersion + "'");
		write(wr, "ashleyVersion = '" + DependencyBank.ashleyVersion + "'");
		write(wr, "aiVersion = '" + DependencyBank.aiVersion + "'");
		write(wr, "}");
		space(wr);
		write(wr, "repositories {");
		write(wr, DependencyBank.mavenLocal);
		write(wr, DependencyBank.mavenCentral);
		write(wr, DependencyBank.jCenter);
		write(wr, DependencyBank.google);
		write(wr, "maven { url \"" + DependencyBank.libGDXSnapshotsUrl + "\" }");
		write(wr, "maven { url \"" + DependencyBank.libGDXReleaseUrl + "\" }");
		write(wr, "}");
		write(wr, "}");
	}

	public static void addProject(Language language, ProjectType project, List<Dependency> dependencies, BufferedWriter wr) throws IOException {
		space(wr);
		write(wr, "project(\":" + project.getName() + "\") {");
		for (String plugin : project.getPlugins(language)) {
			write(wr, "apply plugin: \"" + plugin + "\"");
		}
		space(wr);
		addConfigurations(project, wr);
		space(wr);
		addDependencies(language, project, dependencies, wr);
		write(wr, "}");
	}

	private static void addDependencies(Language language, ProjectType project, List<Dependency> dependencyList, BufferedWriter wr) throws IOException {
		write(wr, "dependencies {");
		if (!project.equals(ProjectType.CORE)) {
			write(wr, "implementation project(\":" + ProjectType.CORE.getName() + "\")");
		}
		for (Dependency dep : dependencyList) {
			if (dep.getDependencies(project) == null) continue;
			for (String moduleDependency : dep.getDependencies(project)) {
				if (moduleDependency == null) continue;
				if ((project.equals(ProjectType.ANDROID) || project.equals(ProjectType.IOSMOE)) && moduleDependency.contains("native")) {
					write(wr, "natives \"" + moduleDependency + "\"");
				} else {
					write(wr, "api \"" + moduleDependency + "\"");
				}
			}
		}
		write(wr, language.dependencies);
		write(wr, "}");
	}

	private static void addConfigurations(ProjectType project, BufferedWriter wr) throws IOException {
		if (project.equals(ProjectType.ANDROID) || project.equals(ProjectType.IOSMOE)) {
			write(wr, "configurations { natives }");
		}
	}

	private static void write(BufferedWriter wr, String input) throws IOException {
		int delta = StringUtils.countMatches(input, '{') - StringUtils.countMatches(input, '}');
		indent += delta *= 4;
		indent = clamp(indent);
		if (delta > 0) {
			wr.write(StringUtils.repeat(" ", clamp(indent - 4)) + input + "\n");
		} else if (delta < 0) {
			wr.write(StringUtils.repeat(" ", clamp(indent)) + input + "\n");
		} else {
			wr.write(StringUtils.repeat(" ", indent) + input + "\n");
		}
	}

	private static void space(BufferedWriter wr) throws IOException {
		wr.write("\n");
	}

	private static int clamp(int indent) {
		if (indent < 0) {
			return 0;
		}
		return indent;
	}

	static class StringUtils {

		public static int countMatches(String input, char match) {
			int count = 0;
			for (int i = 0; i < input.length(); i++) {
				if (input.charAt(i) == match) {
					count++;
				}
			}
			return count;
		}

		public static String repeat(String toRepeat, int count) {
			String repeat = "";
			for (int i = 0; i < count; i++) {
				repeat += toRepeat;
			}
			return repeat;
		}
	}

}
