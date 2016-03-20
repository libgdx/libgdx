
package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class ShadersContainer {

	private final ObjectMap<Application, Array<ShaderProgram>> shaders = new ObjectMap<Application, Array<ShaderProgram>>();

	public Array<ShaderProgram> get (Application app) {
		return shaders.get(app);
	}

	public void remove (Application app) {
		shaders.remove(app);
		log("remove " + app);
	}

	private void log (String string) {
		l("ShadersContainer[" + this.shaders.size + "] :: " + string);
		Array<Application> keys = this.shaders.keys().toArray();
		for (int i = 0; i < this.shaders.size; i++) {
			Application key = keys.get(i);
			Array<ShaderProgram> vallue = shaders.get(key);
			l("    " + key + " :-> " + vallue);
		}
	}

	private void l (String string) {
		System.out.println("# " + string);
	}

	public void put (Application app, Array<ShaderProgram> managedResources) {
		shaders.put(app, managedResources);
		log("put " + app);
	}

	public Keys<Application> keys () {
		return shaders.keys();
	}

}
