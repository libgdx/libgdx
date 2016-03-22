
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.utils.Array;

public class ShadersContainer {

	Application app = null;
	final Array<ShaderProgram> shaders_list = new Array<ShaderProgram>();

	public void clearAllShaderPrograms (Application app) {
		if (this.app == null) {
			err(app);
		} else if (this.app != app) {
			err(app);
		}

		shaders_list.clear();
		this.app = null;
		log("clearAllShaderPrograms " + app);
	}

	private void log (String string) {
		l("    " + string + " :-> " + shaders_list);
	}

	private void l (String string) {
		System.out.println("# " + string);
	}

	public int size () {
		return shaders_list.size;
	}

	public void addManagedShader (Application app, ShaderProgram shaderProgram) {
		if (this.app == null) {
			this.app = app;
		} else if (this.app != app) {
			err(app);
		}
		this.shaders_list.add(shaderProgram);
		log("addManagedShader " + app);
	}

	private void err (Application app) {
		l("app=" + app);
		l("this.app=" + this.app);
		throw new Error("Shaders structure is corrupted");
	}

	public void dispose () {
		if (this.app != null) {
			this.app = null;
			this.shaders_list.clear();
		}
	}

	public void invalidateAll (Application app) {
		for (int i = 0; i < this.shaders_list.size; i++) {
			shaders_list.get(i).invalidated = true;
			shaders_list.get(i).checkManaged();
		}
	}

}
