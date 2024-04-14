
package com.badlogic.gdx.utils;

import com.badlogic.gdx.OpenGLObjects.OpenGLObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.junit.Test;

public class ShaderProgramUpdateTest {

	@Test
	public void TestCreation () {
		// Just to test interface implementation and creation/usage of a ShaderProgram
		OpenGLObject testOGO = new OpenGLObject() {
			@Override
			public int getHandle () {
				return 123;
			}

			@Override
			public int[] getHandles () {
				return new int[] {123, 456, 789};
			}

			@Override
			public int getType () {
				return 10;
			}
		};

		ShaderProgram program = new ShaderProgram(testOGO, "Vertex shader", "Fragment shader");

		program.setAttributes(new ObjectIntMap<String>(), new ObjectIntMap<String>(), new ObjectIntMap<String>());
		program.setUniforms(new ObjectIntMap<String>(), new ObjectIntMap<String>(), new ObjectIntMap<String>());

	}
}
