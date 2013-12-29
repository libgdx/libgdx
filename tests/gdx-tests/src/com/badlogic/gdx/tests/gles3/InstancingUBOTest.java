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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;

/** This test demonstrates the use of geometry instancing from OpenGL ES 3.0. In this specific implementation, an array of
 * positional offsets is stored in an Uniform Buffer Object, which are used in the vertex shader to offset each instance.
 * @author mattijs driel */
public class InstancingUBOTest extends GdxTest {
	ShaderProgramES3 shader;
	VBOGeometry geom;
	UniformBufferObject offsetBuffer;
	int numInstances; // defined by this system's limit

	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void create () {
		if (Gdx.graphics.getGL30() == null) {
			System.out.println("This test requires OpenGL ES 3.0.");
			System.out.println("Make sure needsGL20() is returning true. (ES 2.0 is a subset of ES 3.0.)");
			System.out
				.println("Otherwise, your system does not support it, or it might not be available yet for the current backend.");
			return;
		}

		Gdx.gl20.glClearColor(0, 0, 0, 0);

		// Uniform Blocks (and UBO's as well) may store a limited number of bytes, which sets the limit for instancing for a single
		// pass. This demonstrates how the limit could be obtained.
		{
			IntBuffer ib = BufferUtils.newIntBuffer(16);
			Gdx.gl30.glGetIntegerv(GL30.GL_MAX_UNIFORM_BLOCK_SIZE, ib);
			int limitBytes = ib.get(0);
			System.out.println("Uniform Block limit (bytes): " + limitBytes);
			System.out.println("Uniform Block limit (floats): " + (limitBytes / 4));
			System.out.println("Uniform Block limit (vec4): " + (limitBytes / 16));
			numInstances = (limitBytes / 16);
		}

		String vertexShader = "#version 300 es                                                  \n"
			+ "uniform InstancingOffsets {                                                       \n"
			+ "   vec4 offset[" + numInstances + "];                                                   \n"
			+ "};                                                                                \n"
			+ "layout(location = 0)in vec4 vPos;                                                 \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   gl_Position = vPos;                                                            \n"
			+ "   gl_Position.xyz += offset[gl_InstanceID].xyz;                                  \n"
			+ "}                                                                                 \n";

		String fragmentShader = "#version 300 es                                                \n"
			+ "precision mediump float;                                                          \n"
			+ "out vec4 fragColor;                                                               \n"
			+ "void main()                                                                       \n"
			+ "{                                                                                 \n"
			+ "   fragColor = vec4(gl_FragCoord.z);                                              \n"
			+ "}                                                                                 \n";

		// load the shader
		shader = new ShaderProgramES3(vertexShader, fragmentShader);
		if (!shader.isCompiled()) {
			System.out.println(shader.getErrorLog());
			return;
		}
		shader.getUniformBlock("InstancingOffsets").bindToBindingPoint(3);

		offsetBuffer = new UniformBufferObject(4 * 4 * numInstances, 3);

		// fill the buffer with some offsets
		MathUtils.random.setSeed(0);
		FloatBuffer fb = offsetBuffer.getDataBuffer().asFloatBuffer();
		for (int i = 0; i < numInstances * 4; ++i)
			fb.put(MathUtils.random(-0.9f, 0.9f));

		// just a random geometry with a single attribute (POSITION)
		geom = VBOGeometry.tinyTriangleV();
	}

	@Override
	public void render () {
		if (Gdx.graphics.getGL30() == null || !shader.isCompiled()) return;

		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shader.use();
		offsetBuffer.bind();
		geom.bind();
		geom.drawInstances(numInstances);
	}
}
