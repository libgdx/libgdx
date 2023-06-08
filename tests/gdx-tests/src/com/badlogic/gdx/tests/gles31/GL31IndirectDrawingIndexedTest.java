/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

package com.badlogic.gdx.tests.gles31;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

/** see https://www.khronos.org/opengl/wiki/Vertex_Rendering#Indirect_rendering
 * 
 * Example of indirect commands. Note that commands could be defined directly in GPU via a comput shader. Also note that multi
 * draw (glMultiDrawElementsIndirect) requires an extension to GLES 3.1
 * 
 * @author mgsx */
@GdxTestConfig(requireGL31 = true)
public class GL31IndirectDrawingIndexedTest extends GdxTest {
	static String vsCode = "attribute vec4 a_position;\n" + //
		"attribute vec4 a_color;\n" + //
		"uniform mat4 u_projTrans;\n" + //
		"varying vec4 v_color;\n" + //
		"void main(){\n" + //
		"    v_color = a_color;\n" + //
		"    gl_Position =  u_projTrans * a_position;\n" + //
		"}"; //

	static String fsCode = "varying vec4 v_color;\n" + //
		"void main(){\n" + //
		"    gl_FragColor = v_color;\n" + //
		"}"; //

	private int drawCommands;
	private Mesh mesh;

	private ShaderProgram shader;
	private Matrix4 transform = new Matrix4();
	private float time;

	private static final int commandInts = 5;
	private static final int commandStride = commandInts * 4;
	int nbCommands = 2;

	@Override
	public void create () {
		drawCommands = Gdx.gl.glGenBuffer();
		IntBuffer buffer = BufferUtils.newIntBuffer(commandInts * nbCommands);
		buffer.put(new int[] { //
			3, // count
			1, // instanceCount
			0, // firstIndex
			0, // baseVertex
			0 // reservedMustBeZero
		});

		buffer.put(new int[] { //
			3, // count
			1, // instanceCount
			3, // firstIndex
			0, // baseVertex
			0 // reservedMustBeZero
		});
		buffer.flip();

		Gdx.gl.glBindBuffer(GL31.GL_DRAW_INDIRECT_BUFFER, drawCommands);
		Gdx.gl.glBufferData(GL31.GL_DRAW_INDIRECT_BUFFER, nbCommands * commandStride, buffer, GL30.GL_DYNAMIC_DRAW);
		Gdx.gl.glBindBuffer(GL31.GL_DRAW_INDIRECT_BUFFER, 0);

		mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());
		mesh.setVertices(new float[] { //
			0, 0, 0, 1, 1, 1, 1, //
			1, 0, 0, 1, 1, 1, 1, //
			0, 1, 0, 1, 1, 1, 1, //
			1, 1, 0, 1, 1, 1, 1, //
		});
		mesh.setIndices(new short[] { //
			0, 1, 2, //
			2, 1, 3, //
		});

		shader = new ShaderProgram(vsCode, fsCode);
		if (!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
	}

	@Override
	public void dispose () {
		shader.dispose();
		mesh.dispose();
		Gdx.gl.glDeleteBuffer(drawCommands);
	}

	@Override
	public void render () {
		time += Gdx.graphics.getDeltaTime();
		int commandIndex = (int)time % nbCommands;

		ScreenUtils.clear(Color.CLEAR, true);

		shader.bind();
		transform.setToOrtho2D(-1, -1, 3, 3);
		shader.setUniformMatrix("u_projTrans", transform);

		mesh.bind(shader);

		Gdx.gl.glBindBuffer(GL31.GL_DRAW_INDIRECT_BUFFER, drawCommands);
		Gdx.gl31.glDrawElementsIndirect(GL20.GL_TRIANGLES, GL20.GL_UNSIGNED_SHORT, commandStride * commandIndex);
		mesh.unbind(shader);
	}
}
