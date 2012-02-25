
package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class ShaderLoader {

	static final public ShaderProgram createShader (String vertexName, String fragmentName) {
		String vertexShader = Gdx.files.internal("data/shaders/" + vertexName + ".vertex.glsl").readString();
		String fragmentShader = Gdx.files.internal("data/shaders/" + fragmentName + ".fragment.glsl").readString();
		ShaderProgram.pedantic = false;
		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println("error" + shader.getLog());
			Gdx.app.exit();
		}
		return shader;
	}
}
