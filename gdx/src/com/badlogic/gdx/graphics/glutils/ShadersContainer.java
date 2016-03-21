
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.utils.Array;

public class ShadersContainer {

// private final ObjectMap<Application, Array<ShaderProgram>> shaders = new ObjectMap<Application, Array<ShaderProgram>>();
	Application app = null;
	Array<ShaderProgram> shaders_list;

	public Array<ShaderProgram> get (Application app) {
		checkConsistency(app);
		return shaders_list;
	}

	public void remove (Application app) {
		checkConsistency(app);
		shaders_list.clear();
		this.app = null;
		log("remove " + app);
	}

	private void checkConsistency (Application app) {
		if (app != this.app) {
			l("app=" + app);
			l("this.app=" + this.app);
			throw new Error("Shaders structure is corrupted");
		}
	}

	private void log (String string) {
// l("ShadersContainer[" + this.shaders.size + "] :: " + string);
// Array<Application> keys = this.shaders.keys().toArray();
// for (int i = 0; i < this.shaders.size; i++) {
// Application key = keys.get(i);
// Array<ShaderProgram> vallue = shaders.get(key);
//
// }
		l("    " + string + " :-> " + shaders_list);
	}

	private void l (String string) {
		System.out.println("# " + string);
	}

	public void put (Application app, Array<ShaderProgram> managedResources) {
		this.app = app;
		if (this.shaders_list != null) {
			throw new Error("Shaders structure is corrupted");
		}
		this.shaders_list = managedResources;
// shaders.put(app, managedResources);
// new Error("#").printStackTrace();
		log("put " + app);
	}

	public int size () {
		return shaders_list.size;
	}

}
