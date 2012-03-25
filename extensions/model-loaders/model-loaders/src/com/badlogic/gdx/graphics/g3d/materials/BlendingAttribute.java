
package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class BlendingAttribute extends MaterialAttribute {

	/** if BlendingAttribute name is translucent then default tranparency mechanism is used in shader */
	public static final String translucent = "translucent";

	public int blendSrcFunc;
	public int blendDstFunc;

	/** Utility constuctor for basic transparency blendSrcFunc = GL10.GL_SRC_ALPHA blendDstFunc = GL10.GL_ONE_MINUS_SRC_ALPHA
	 * @param name */
	public BlendingAttribute (String name) {
		this(name, GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	public BlendingAttribute (String name, int srcFunc, int dstFunc) {
		super(name);
		blendSrcFunc = srcFunc;
		blendDstFunc = dstFunc;
	}

	@Override
	public void bind () {
		Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
	}

	@Override
	public void bind (ShaderProgram program) {
		Gdx.gl.glBlendFunc(blendSrcFunc, blendDstFunc);
	}

	@Override
	public MaterialAttribute copy () {
		return new BlendingAttribute(this.name, this.blendSrcFunc, this.blendDstFunc);
	}
}
