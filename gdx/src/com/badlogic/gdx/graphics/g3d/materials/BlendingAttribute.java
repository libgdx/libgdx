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

package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;

public class BlendingAttribute extends MaterialAttribute {

	/** if BlendingAttribute name is translucent then default tranparency mechanism is used in shader */
	public static final String translucent = "translucent";

	public int blendSrcFunc;
	public int blendDstFunc;

	protected BlendingAttribute () {
	}

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

	@Override
	public void set (MaterialAttribute attr) {
		BlendingAttribute blendAttr = (BlendingAttribute)attr;
		name = blendAttr.name;
		blendDstFunc = blendAttr.blendDstFunc;
		blendSrcFunc = blendAttr.blendSrcFunc;
	}

	private final static Pool<BlendingAttribute> pool = new Pool<BlendingAttribute>() {
		@Override
		protected BlendingAttribute newObject () {
			return new BlendingAttribute();
		}
	};

	@Override
	public MaterialAttribute pooledCopy () {
		BlendingAttribute attr = pool.obtain();
		attr.set(this);
		return attr;
	}

	@Override
	public void free () {
		if (isPooled) pool.free(this);
	}
}
