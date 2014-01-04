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

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.BufferUtils;

/** This test demonstrates rendering to multiple draw buffers (render targets) through the use of a frame buffer object (FBO). This
 * functionality is the key component of most deferred shading approaches.
 * <p>
 * The test first draws blue and red to 2 textures using a shader with 2 fragment outputs. This is done once, after which the
 * textures are used to fill in a quad and triangle shape, to show that the operation was successful.
 * @author mattijs driel */
public class MultipleDrawBuffersTest extends AbstractES3test {
	ShaderProgramES3 drawTexProgram;
	ShaderProgramES3 resultProgram;
	GenericTexture tex0;
	GenericTexture tex1;
	VBOGeometry quad;
	VBOGeometry tri;

	private final String vertexShader = "#version 300 es                                    \n"
		+ "layout(location = 0)in vec4 vPos;                                                 \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   gl_Position = vPos;                                                            \n"
		+ "}                                                                                 \n";

	private final String drawTexturesShader = "#version 300 es                              \n"
		+ "precision highp float;                                                            \n"
		+ "layout(location = 0)out vec4 redOutput;                                           \n"
		+ "layout(location = 1)out vec4 blueOutput;                                          \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   redOutput = vec4(1,0,0,1);                                                     \n"
		+ "   blueOutput = vec4(0,0,1,1);                                                    \n"
		+ "}                                                                                 \n";
	
	private final String showResultShader = "#version 300 es                                \n"
		+ "precision mediump float;                                                          \n"
		+ "uniform sampler2D mytexture;                                                      \n"
		+ "out vec4 fragColor;                                                               \n"
		+ "void main()                                                                       \n"
		+ "{                                                                                 \n"
		+ "   fragColor = texture2D(mytexture, gl_FragCoord.xy);                             \n"
		+ "}                                                                                 \n";
	
	@Override
	public boolean createLocal () {
		// create empty draw target textures
		TextureFormatES3 format = new TextureFormatES3();
		format.width = format.height = 256;
		
		tex0 = new GenericTexture(format);
		tex1 = new GenericTexture(format);

		// load the shaders
		drawTexProgram = new ShaderProgramES3(vertexShader, drawTexturesShader);
		if (!drawTexProgram.isCompiled()) {
			System.out.println(drawTexProgram.getErrorLog());
			return false;
		}

		resultProgram = new ShaderProgramES3(vertexShader, showResultShader);
		if (!resultProgram.isCompiled()) {
			System.out.println(resultProgram.getErrorLog());
			return false;
		}
		resultProgram.registerTextureSampler("mytexture").setBinding(0);

		tri = VBOGeometry.triangle(Usage.Position);
		quad = VBOGeometry.quad(Usage.Position);

		Gdx.gl30.glClearColor(0, 0, 0, 0);
		
		return writeColorToTextures();
	}

	boolean writeColorToTextures () {

		// generate and bind FBO
		FrameBufferObject fbo = new FrameBufferObject(GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1);

		// bind textures to current FBO
		fbo.bind();
		tex0.setFBOBinding(GL30.GL_COLOR_ATTACHMENT0);
		tex1.setFBOBinding(GL30.GL_COLOR_ATTACHMENT1);

		VBOGeometry geom = VBOGeometry.fsQuad(Usage.Position);

		// render to texture
		drawTexProgram.use();
		geom.bind();
		geom.draw();

		geom.dispose();

		// bind back to normal buffer
		fbo.unbind();
		fbo.dispose();
		
		return true;
	}

	@Override
	public void renderLocal () {
		Gdx.gl30.glClear(GL30.GL_COLOR_BUFFER_BIT);

		// render results
		resultProgram.use();

		tex0.bind();
		tri.bind();
		tri.draw();

		tex1.bind();
		quad.bind();
		quad.draw();
	}
}
