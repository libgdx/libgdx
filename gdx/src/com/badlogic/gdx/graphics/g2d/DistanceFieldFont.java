/*
 * Copyright (c) 2015, Florian Falkner
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

/** Renders bitmap fonts using distance field textures, see the <a
 * href="https://github.com/libgdx/libgdx/wiki/Distance-field-fonts">Distance Field Fonts wiki article</a> for usage. Initialize
 * the SpriteBatch with the {@link #createDistanceFieldShader()} shader.
 * <p>
 * Attention: The batch is flushed before and after each string is rendered.
 * @author Florian Falkner */
public class DistanceFieldFont extends BitmapFont {
	private float distanceFieldSmoothing;

	public DistanceFieldFont (BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer) {
		super(data, pageRegions, integer);
	}

	public DistanceFieldFont (BitmapFontData data, TextureRegion region, boolean integer) {
		super(data, region, integer);
	}

	public DistanceFieldFont (FileHandle fontFile, boolean flip) {
		super(fontFile, flip);
	}

	public DistanceFieldFont (FileHandle fontFile, FileHandle imageFile, boolean flip, boolean integer) {
		super(fontFile, imageFile, flip, integer);
	}

	public DistanceFieldFont (FileHandle fontFile, FileHandle imageFile, boolean flip) {
		super(fontFile, imageFile, flip);
	}

	public DistanceFieldFont (FileHandle fontFile, TextureRegion region, boolean flip) {
		super(fontFile, region, flip);
	}

	public DistanceFieldFont (FileHandle fontFile, TextureRegion region) {
		super(fontFile, region);
	}

	public DistanceFieldFont (FileHandle fontFile) {
		super(fontFile);
	}

	protected void load (BitmapFontData data) {
		super.load(data);

		// Distance field font rendering requires font texture to be filtered linear.
		final Array<TextureRegion> regions = getRegions();
		for (TextureRegion region : regions)
			region.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	@Override
	public BitmapFontCache newFontCache () {
		return new DistanceFieldFontCache(this, integer);
	}

	/** @return The distance field smoothing factor for this font. */
	public float getDistanceFieldSmoothing () {
		return distanceFieldSmoothing;
	}

	/** @param distanceFieldSmoothing Set the distance field smoothing factor for this font. SpriteBatch needs to have this shader
	 *           set for rendering distance field fonts. */
	public void setDistanceFieldSmoothing (float distanceFieldSmoothing) {
		this.distanceFieldSmoothing = distanceFieldSmoothing;
	}

	/** Returns a new instance of the distance field shader, see https://github.com/libgdx/libgdx/wiki/Distance-field-fonts if the
	 * u_smoothing uniform > 0.0. Otherwise the same code as the default SpriteBatch shader is used. */
	static public ShaderProgram createDistanceFieldShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main() {\n" //
			+ "	v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "	v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";

		String fragmentShader = "#ifdef GL_ES\n" //
			+ "	precision mediump float;\n" //
			+ "	precision mediump int;\n" //
			+ "#endif\n" //
			+ "\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "uniform float u_smoothing;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main() {\n" //
			+ "	if (u_smoothing > 0.0) {\n" //
			+ "		float smoothing = 0.25 / u_smoothing;\n" //
			+ "		float distance = texture2D(u_texture, v_texCoords).a;\n" //
			+ "		float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n" //
			+ "		gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n" //
			+ "	} else {\n" //
			+ "		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
			+ "	}\n" //
			+ "}\n";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false)
			throw new IllegalArgumentException("Error compiling distance field shader: " + shader.getLog());
		return shader;
	}

	/** Provides a font cache that uses distance field shader for rendering fonts. Attention: breaks batching because uniform is
	 * needed for smoothing factor, so a flush is performed before and after every font rendering.
	 * @author Florian Falkner */
	static private class DistanceFieldFontCache extends BitmapFontCache {
		public DistanceFieldFontCache (DistanceFieldFont font) {
			super(font, font.usesIntegerPositions());
		}

		public DistanceFieldFontCache (DistanceFieldFont font, boolean integer) {
			super(font, integer);
		}

		private float getSmoothingFactor () {
			final DistanceFieldFont font = (DistanceFieldFont)super.getFont();
			return font.getDistanceFieldSmoothing() * font.getScaleX();
		}

		private void setSmoothingUniform (Batch spriteBatch, float smoothing) {
			spriteBatch.flush();
			spriteBatch.getShader().setUniformf("u_smoothing", smoothing);
		}

		@Override
		public void draw (Batch spriteBatch) {
			setSmoothingUniform(spriteBatch, getSmoothingFactor());
			super.draw(spriteBatch);
			setSmoothingUniform(spriteBatch, 0);
		}

		@Override
		public void draw (Batch spriteBatch, int start, int end) {
			setSmoothingUniform(spriteBatch, getSmoothingFactor());
			super.draw(spriteBatch, start, end);
			setSmoothingUniform(spriteBatch, 0);
		}
	}
}
