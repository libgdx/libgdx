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

package com.badlogic.gdx.tests.gles3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;

/** This test demonstrates the use of Uniform Buffer Objects from OpenGL ES 3.0
 * @author mattijs driel */
public class UniformBufferObjectTest extends GdxTest {
	ShaderProgramES3 shader;
	VBOGeometry geom;
	UniformBufferObject offsetBuffer;
	UniformBufferObject colorBuffer;
	float progress = 0;

	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void create () {
		if (Gdx.graphics.getGL30() == null) {
			System.out.println("This test requires OpenGL ES 3.0.");
			System.out.println("Make sure needsGL20() is returning true. (ES 2.0 is a subset of ES 3.0.)");
			System.out.println("Otherwise, your system does not support it, or it might not be available yet for the current backend.");
			return;
		}

		Gdx.gl20.glClearColor(0, 0, 0, 0);

		String vertexShader = "#version 300 es                                                  \n"
			+ "uniform Offsets {                                                   \n"
			+ "   vec2 offset;                                                                   \n"
			+ "   float rotate;                                                                  \n"
			+ "};                                                                                \n"
			+ "layout(location = 0)in vec4 vPos;                                                 \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   gl_Position = vPos;                                                            \n"
			+ "   gl_Position.x = vPos.x * cos(rotate) - vPos.y * sin(rotate);                   \n"
			+ "   gl_Position.y = vPos.x * sin(rotate) + vPos.y * cos(rotate);                   \n"
			+ "   gl_Position.xy += offset;                                                      \n"
			+ "}                                                                                 \n";

		// using highp instead of mediump so it's easier to store correct floats in UBO's. 
		String fragmentShader = "#version 300 es                                                \n"
			+ "precision highp float;                                                            \n"
			+ "uniform Colors {                                                                  \n"
			+ "   vec4 colorA;                                                                   \n"
			+ "   vec4 colorB;                                                                   \n"
			+ "};                                                                                \n"
			+ "out vec4 fragColor;                                                               \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   fragColor = colorA + colorB;                                                   \n"
			+ "}                                                                                 \n";

		// load the shader
		shader = new ShaderProgramES3(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println(shader.getErrorLog());
			return;
		}
		// set known binding points for the shader (that can be used used by UBO's)
		shader.registerUniformBlock("Colors").setBinding(5);
		shader.registerUniformBlock("Offsets").setBinding(3);

		// create UBO's (note the matching binding points as the block bindings)
		colorBuffer = new UniformBufferObject(8 * 4, 5);
		offsetBuffer = new UniformBufferObject(3 * 4, 3);

		// fill one buffer with some data
		FloatBuffer fb = colorBuffer.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(new float[] {1, 0, 0, 1, 0, 1, 0, 1});

		// just a random geometry with a single attribute (POSITION)
		geom = VBOGeometry.triangleV();
	}

	@Override
	public void render () {
		if (Gdx.graphics.getGL30() == null || !shader.isCompiled()) return;

		// update only the offset
		progress += 0.01f;
		FloatBuffer fb = offsetBuffer.getDataBuffer().asFloatBuffer();
		fb.position(0);
		fb.put(new float[] {MathUtils.cos(progress) * 0.2f, 0.3f, progress});

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shader.use();
		offsetBuffer.bind();
		colorBuffer.bind();
		geom.bind();
		geom.draw();
	}
}
