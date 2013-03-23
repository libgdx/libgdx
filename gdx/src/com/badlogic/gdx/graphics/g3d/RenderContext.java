package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.utils.ExclusiveTextures;

public class RenderContext {
	public final ExclusiveTextures textures;
	
	private boolean blending;
	private int blendSFactor;
	private int blendDFactor;
	private boolean depthTest;
	private int depthFunc;
	
	public RenderContext(ExclusiveTextures textures) {
		this.textures = textures;
	}
	
	public final void begin() {
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		depthTest = false;
		Gdx.gl.glDisable(GL10.GL_BLEND);
		blending = false;
		blendSFactor = blendDFactor = depthFunc = 0;
	}
	
	public final void end() {
		
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
