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

package com.badlogic.gdx.tests.gles32;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

@GdxTestConfig(requireGL32 = true)
public class GL32MultipleRenderTargetsBlendingTest extends GdxTest {
	static final String shapeRendererVS = "" + "attribute vec4 a_position;\n" + //
		"attribute vec4 a_color;\n" + //
		"uniform mat4 u_projModelView;\n" + //
		"varying vec4 v_color;\n" + //
		"void main() {\n" + //
		"    v_color = a_color;\n" + //
		"    gl_Position = u_projModelView * a_position;\n" + //
		"}";

	static final String shapeRendererFS = "" + //
		"#if __VERSION__ < 300\n" + //
		"#extension GL_ARB_explicit_attrib_location : enable\n" + //
		"#endif\n" + //
		"\n" + //
		"layout(location = 0) out vec4 out_FragColor0;\n" + //
		"layout(location = 1) out vec4 out_FragColor1;\n" + //
		"layout(location = 2) out vec4 out_FragColor2;\n" + //
		"layout(location = 3) out vec4 out_FragColor3;\n" + //
		"layout(location = 4) out vec4 out_FragColor4;\n" + //
		"layout(location = 5) out vec4 out_FragColor5;\n" + //
		"layout(location = 6) out vec4 out_FragColor6;\n" + //
		"layout(location = 7) out vec4 out_FragColor7;\n" + //
		"in vec4 v_color;\n" + //
		"void main() {\n" + //
		"    out_FragColor0 = v_color;\n" + //
		"    out_FragColor1 = v_color;\n" + //
		"    out_FragColor2 = v_color;\n" + //
		"    out_FragColor3 = v_color;\n" + //
		"    out_FragColor4 = v_color;\n" + //
		"    out_FragColor5 = v_color;\n" + //
		"    out_FragColor6 = v_color;\n" + //
		"    out_FragColor7 = v_color;\n" + //
		"}";

	static final String spriteBatchVS = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
		+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
		+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
		+ "uniform mat4 u_projTrans;\n" //
		+ "varying vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "\n" //
		+ "void main()\n" //
		+ "{\n" //
		+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
		+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
		+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
		+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
		+ "}\n";

	static final String spriteBatchFS = "#ifdef GL_ES\n" //
		+ "#define LOWP lowp\n" //
		+ "precision mediump float;\n" //
		+ "#else\n" //
		+ "#define LOWP \n" //
		+ "#endif\n" //
		+ "varying LOWP vec4 v_color;\n" //
		+ "varying vec2 v_texCoords;\n" //
		+ "uniform sampler2D u_texture;\n" //
		+ "void main()\n"//
		+ "{\n" //
		+ "  float a = texture2D(u_texture, v_texCoords).a;\n" //
		+ "  gl_FragColor = v_color * vec4(a, a, a, 1.0);\n" //
		+ "}";

	private FrameBuffer fbo;
	private ShapeRenderer shapes;
	private SpriteBatch batch;
	private ShaderProgram mrtShader;
	private ShaderProgram alphaShader;

	@Override
	public void create () {
		FrameBufferBuilder builder = new FrameBufferBuilder(64, 64);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		builder.addColorTextureAttachment(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE);
		fbo = builder.build();

		String prefix;
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			prefix = "#version 150\n";
		} else {
			prefix = "#version 300 es\n";
		}

		String oldPrepend = ShaderProgram.prependFragmentCode;
		ShaderProgram.prependFragmentCode = null;
		mrtShader = new ShaderProgram(shapeRendererVS, prefix + shapeRendererFS);
		if (!mrtShader.isCompiled()) throw new GdxRuntimeException(mrtShader.getLog());
		ShaderProgram.prependFragmentCode = oldPrepend;

		alphaShader = new ShaderProgram(spriteBatchVS, spriteBatchFS);
		if (!alphaShader.isCompiled()) throw new GdxRuntimeException(alphaShader.getLog());

		shapes = new ShapeRenderer(6, mrtShader);

		batch = new SpriteBatch();

		for (int i = 0; i < fbo.getTextureAttachments().size; i++) {
			boolean enabled = Gdx.gl32.glIsEnabledi(GL20.GL_BLEND, i);
			Gdx.app.log("Gdx", "#" + i + " blending: " + enabled);
		}
	}

	@Override
	public void dispose () {
		alphaShader.dispose();
		mrtShader.dispose();
		fbo.dispose();
		shapes.dispose();
		batch.dispose();
	}

	@Override
	public void render () {

		// Configure blending for individual render target
		Gdx.gl32.glEnablei(GL20.GL_BLEND, 0);
		Gdx.gl32.glBlendFuncSeparatei(0, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

		Gdx.gl32.glEnablei(GL20.GL_BLEND, 1);
		Gdx.gl32.glBlendFunci(1, GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		Gdx.gl32.glDisablei(GL20.GL_BLEND, 2);

		Gdx.gl32.glEnablei(GL20.GL_BLEND, 3);
		Gdx.gl32.glBlendFunci(3, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		Gdx.gl32.glBlendEquationi(3, GL20.GL_FUNC_SUBTRACT);

		Gdx.gl32.glDisablei(GL20.GL_BLEND, 4);
		Gdx.gl32.glColorMaski(4, true, false, false, false);

		Gdx.gl32.glEnablei(GL20.GL_BLEND, 5);
		Gdx.gl32.glBlendFunci(5, GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		Gdx.gl32.glBlendEquationSeparatei(5, GL20.GL_FUNC_SUBTRACT, GL20.GL_FUNC_ADD);

		Gdx.gl32.glEnablei(GL20.GL_BLEND, 6);
		Gdx.gl32.glBlendFunci(6, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl32.glBlendEquationSeparatei(6, GL32.GL_FUNC_REVERSE_SUBTRACT, GL20.GL_FUNC_ADD);

		Gdx.gl32.glEnablei(GL20.GL_BLEND, 7);
		Gdx.gl32.glBlendFunci(7, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl32.glBlendEquationSeparatei(7, GL32.GL_MAX, GL20.GL_FUNC_ADD);

		// render into MRT fbo
		fbo.begin();
		ScreenUtils.clear(Color.CLEAR);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 4, 4);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(1, .5f, 0, .5f);
		shapes.rect(0, 0, 3, 3);
		shapes.setColor(0f, .5f, .7f, .5f);
		shapes.rect(1, 1, 3, 3);
		shapes.end();
		fbo.end();

		// reset GL state
		for (int i = 0; i < 8; i++) {
			Gdx.gl32.glDisablei(GL20.GL_BLEND, i);
			Gdx.gl32.glBlendEquationi(i, GL20.GL_FUNC_ADD);
			Gdx.gl32.glBlendFunci(i, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl32.glColorMaski(i, true, true, true, true);
		}

		// Display render targets
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 4, 4);
		batch.disableBlending();
		batch.begin();
		float x = 0;

		batch.draw(fbo.getTextureAttachments().get(0), x, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), x + 1, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), x, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(3), x + 1, 1, 1, 1, 0, 0, 1, 1);

		batch.draw(fbo.getTextureAttachments().get(4), x, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(5), x + 1, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(6), x, 3, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(7), x + 1, 3, 1, 1, 0, 0, 1, 1);

		batch.setShader(alphaShader);
		x = 2;

		batch.draw(fbo.getTextureAttachments().get(0), x, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(1), x + 1, 0, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(2), x, 1, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(3), x + 1, 1, 1, 1, 0, 0, 1, 1);

		batch.draw(fbo.getTextureAttachments().get(4), x, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(5), x + 1, 2, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(6), x, 3, 1, 1, 0, 0, 1, 1);
		batch.draw(fbo.getTextureAttachments().get(7), x + 1, 3, 1, 1, 0, 0, 1, 1);

		batch.setShader(null);
		batch.end();
	}

}
