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

package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;

/** Manages OpenGL state and tries to reduce state changes. Uses a {@link TextureBinder} to reduce texture binds as well. Call
 * {@link #begin()} to setup the context, call {@link #end()} to undo all state changes. Use the setters to change state, use
 * {@link #textureBinder} to bind textures.
 * @author badlogic, Xoppa */
public class RenderContext {
	/** used to bind textures **/
	public final TextureBinder textureBinder;
	private final Graphics graphics;
	private boolean blending;
	private int blendSourceRgbFactor;
	private int blendDestRgbFactor;
	private int blendSourceAlphaFactor;
	private int blendDestAlphaFactor;
	private int depthFunc;
	private float depthRangeNear;
	private float depthRangeFar;
	private boolean depthMask;
	private int cullFace;

	private GL20 gl20 () {
		return graphics.getGL20();
	}

	public RenderContext (TextureBinder textures) {
		this(Gdx.graphics, textures);
	}

	public RenderContext (Graphics graphics, TextureBinder textures) {
		this.graphics = graphics;
		this.textureBinder = textures;
	}

	/** Sets up the render context, must be matched with a call to {@link #end()}. */
	public void begin () {
		gl20().glDisable(GL20.GL_DEPTH_TEST);
		depthFunc = 0;
		gl20().glDepthMask(true);
		depthMask = true;
		gl20().glDisable(GL20.GL_BLEND);
		blending = false;
		gl20().glDisable(GL20.GL_CULL_FACE);
		cullFace = blendSourceRgbFactor = blendDestRgbFactor = blendSourceAlphaFactor = blendDestAlphaFactor = 0;
		textureBinder.begin();
	}

	/** Resets all changed OpenGL states to their defaults. */
	public void end () {
		if (depthFunc != 0) gl20().glDisable(GL20.GL_DEPTH_TEST);
		if (!depthMask) gl20().glDepthMask(true);
		if (blending) gl20().glDisable(GL20.GL_BLEND);
		if (cullFace > 0) gl20().glDisable(GL20.GL_CULL_FACE);
		textureBinder.end();
	}

	public void setDepthMask (final boolean depthMask) {
		if (this.depthMask != depthMask) gl20().glDepthMask(this.depthMask = depthMask);
	}

	public void setDepthTest (final int depthFunction) {
		setDepthTest(depthFunction, 0f, 1f);
	}

	public void setDepthTest (final int depthFunction, final float depthRangeNear, final float depthRangeFar) {
		final boolean wasEnabled = depthFunc != 0;
		final boolean enabled = depthFunction != 0;
		if (depthFunc != depthFunction) {
			depthFunc = depthFunction;
			if (enabled) {
				gl20().glEnable(GL20.GL_DEPTH_TEST);
				gl20().glDepthFunc(depthFunction);
			} else
				gl20().glDisable(GL20.GL_DEPTH_TEST);
		}
		if (enabled) {
			if (!wasEnabled || depthFunc != depthFunction) gl20().glDepthFunc(depthFunc = depthFunction);
			if (!wasEnabled || this.depthRangeNear != depthRangeNear || this.depthRangeFar != depthRangeFar)
				gl20().glDepthRangef(this.depthRangeNear = depthRangeNear, this.depthRangeFar = depthRangeFar);
		}
	}

	public void setBlending (final boolean enabled, final int sFactor, final int dFactor) {
		setBlending(enabled, sFactor, dFactor, sFactor, dFactor);
	}

	public void setBlending (final boolean enabled, final int sRgbFactor, final int dRgbFactor, final int sAlphaFactor,
		final int dAlphaFactor) {
		if (enabled != blending) {
			blending = enabled;
			if (enabled)
				gl20().glEnable(GL20.GL_BLEND);
			else
				gl20().glDisable(GL20.GL_BLEND);
		}
		if (enabled && (blendSourceRgbFactor != sRgbFactor || blendDestRgbFactor != dRgbFactor
			|| blendSourceAlphaFactor != sAlphaFactor || blendDestAlphaFactor != dAlphaFactor)) {
			gl20().glBlendFuncSeparate(sRgbFactor, dRgbFactor, sAlphaFactor, dAlphaFactor);
			blendSourceRgbFactor = sRgbFactor;
			blendDestRgbFactor = dRgbFactor;
			blendSourceAlphaFactor = sAlphaFactor;
			blendDestAlphaFactor = dAlphaFactor;
		}
	}

	public void setCullFace (final int face) {
		if (face != cullFace) {
			cullFace = face;
			if ((face == GL20.GL_FRONT) || (face == GL20.GL_BACK) || (face == GL20.GL_FRONT_AND_BACK)) {
				gl20().glEnable(GL20.GL_CULL_FACE);
				gl20().glCullFace(face);
			} else
				gl20().glDisable(GL20.GL_CULL_FACE);
		}
	}
}
