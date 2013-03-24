package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;

/**
 * Manages OpenGL state and tries to reduce state changes. Uses a {@link TextureBinder} to 
 * reduce texture binds as well. Call {@link #begin()} to setup the context, call {@link #end()}
 * to undo all state changes. Use the setters to change state, use {@link #textureBinder} to bind textures.
 * @author badlogic
 *
 */
public class RenderContext {
	/** used to bind textures **/
	public final TextureBinder textureBinder;
	private boolean blending;
	private int blendSFactor;
	private int blendDFactor;
	private boolean depthTest;
	private int depthFunc;
	
	public RenderContext(TextureBinder textures) {
		this.textureBinder = textures;
	}
	
	/**
	 * Sets up the render context, must be matched with a call to {@link #end()}. Assumes
	 * that the OpenGL states are in their defaults.
	 */
	public final void begin() {
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		depthTest = false;
		Gdx.gl.glDisable(GL10.GL_BLEND);
		blending = false;
		blendSFactor = blendDFactor = depthFunc = 0;
		textureBinder.begin();
	}
	
	/**
	 * Resest all changed OpenGL states to their defaults.
	 */
	public final void end() {
		if(depthTest) Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		if(blending) Gdx.gl.glDisable(GL10.GL_BLEND);
		textureBinder.end();
	}
	
	public final void setDepthTest(final boolean enabled, final int depthFunction) {
		if (enabled != depthTest) {
			depthTest = enabled;
			if (enabled)
				Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
			else
				Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		}
		if (enabled && depthFunc != depthFunction) {
			Gdx.gl.glDepthFunc(depthFunction);
			depthFunc = depthFunction;
		}
	}
	
	public final void setBlending(final boolean enabled, final int sFactor, final int dFactor) {
		if (enabled != blending) {
			blending = enabled;
			if (enabled)
				Gdx.gl.glEnable(GL10.GL_BLEND);
			else
				Gdx.gl.glDisable(GL10.GL_BLEND);
		}
		if (enabled && (blendSFactor != sFactor || blendDFactor != dFactor)) {
			Gdx.gl.glBlendFunc(sFactor, dFactor);
			blendSFactor = sFactor;
			blendDFactor = dFactor;
		}
	}
}
