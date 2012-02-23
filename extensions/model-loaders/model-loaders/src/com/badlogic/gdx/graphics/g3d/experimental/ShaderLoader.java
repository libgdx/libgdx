package com.badlogic.gdx.graphics.g3d.experimental;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class ShaderLoader {

	static final public ShaderProgram createShader(String vertexName,
			String fragmentName) {
		String vertexShader = FileUtils.getContent("com/badlogic/gdx/graphics/g3d/experimental/" + vertexName
				+ ".vertex");
		String fragmentShader = FileUtils.getContent("com/badlogic/gdx/graphics/g3d/experimental/" + fragmentName
				+ ".fragment");
		ShaderProgram.pedantic = false;		
		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println("error" + shader.getLog());
			Gdx.app.exit();
		}
		return shader;
	}
}
