package com.badlogic.gdx.setup;


import com.badlogic.gdx.setup.DependencyBank.ProjectType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder {

	DependencyBank bank;
	List<ProjectType> modules = new ArrayList<ProjectType>();
	List<Dependency> dependencies = new ArrayList<Dependency>();
	File settingsFile;
	File buildFile;

	public ProjectBuilder(DependencyBank bank) {
		this.bank = bank;
	}

	public List<String> buildProject(List<ProjectType> projects, List<Dependency> dependencies) {
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

	public boolean build() throws IOException {
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
		try {
			FileWriter settingsWriter = new FileWriter(settingsFile.getAbsoluteFile());
			BufferedWriter settingsBw = new BufferedWriter(settingsWriter);
			String settingsContents = "include ";
			for (ProjectType module : modules) {
				settingsContents += "'" + module.getName() + "'";
				if (modules.indexOf(module) != modules.size() - 1) {
					settingsContents += ", ";
				}
			}
			settingsBw.write(settingsContents);
			settingsBw.close();
			settingsWriter.close();

			FileWriter buildWriter = new FileWriter(buildFile.getAbsoluteFile());
			BufferedWriter buildBw = new BufferedWriter(buildWriter);

			BuildScriptHelper.addBuildScript(modules, buildBw);
			BuildScriptHelper.addAllProjects(buildBw);
			for (ProjectType module : modules) {
				BuildScriptHelper.addProject(module, dependencies, buildBw);
			}

			//Add task here for now
			buildBw.write("\n");
			buildBw.write("tasks.eclipse.doLast {\n");
			buildBw.write("    delete \".project\"\n");
			buildBw.write("}");

			buildBw.close();
			buildWriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void cleanUp() {
		settingsFile.deleteOnExit();
		buildFile.deleteOnExit();
	}

}
