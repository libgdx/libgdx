package com.badlogic.gdx.setup;


import com.badlogic.gdx.setup.DependencyBank.ProjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Dependency {

    private HashMap<ProjectType, String[]> subDependencyMap = new HashMap<ProjectType, String[]>();
    private String name;

    public Dependency(String name, String[]... subDependencies) {
        this.name = name;
        for (ProjectType type : ProjectType.values()) {
            subDependencyMap.put(type, subDependencies[type.ordinal()]);
        }
    }

    public String[] getDependencies(ProjectType type) {
    	return subDependencyMap.get(type);
    }

	public List<String> getIncompatibilities(ProjectType type) {
		List<String> incompat = new ArrayList<String>();
		String[] subArray = subDependencyMap.get(type);
		if (subArray == null) {
			incompat.add("Dependency " + name + " is not compatible with sub module " + type.getName().toUpperCase());
		}
		return incompat;
	}

    public String getName() {
        return name;
    }

}
