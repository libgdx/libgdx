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

package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;

public class BlendingAttribute extends Attribute {
	public final static String Alias = "blended";
	public final static long Type = register(Alias);

	public final static boolean is (final long mask) {
		return (mask & Type) == mask;
	}

	/** Whether this material should be considered blended (default: true). This is used for sorting (back to front instead of
	 * front to back). */
	public boolean blended;
	/** Specifies how the (incoming) red, green, blue source blending factors are computed (default: GL_SRC_ALPHA) */
	public int sourceFunction;
	/** Specifies how the (existing) red, green, blue destination blending factors are computed (default:
	 * GL_ONE_MINUS_SRC_ALPHA) */
	public int destFunction;
	/** Specifies how the (incoming) alpha source blending factor is computed (default: GL_SRC_ALPHA) */
	public int sourceFunctionAlpha;
	/** Specifies how the (existing) alpha destination blending factor is computed (default: GL_ONE_MINUS_SRC_ALPHA) */
	public int destFunctionAlpha;
	/** the blend equation for rgb (default: GL_FUNC_ADD) */
	public int equationRGB;
	/** the blend equation for alpha (default: GL_FUNC_ADD) */
	public int equationAlpha;
	/** The opacity used as source alpha value, ranging from 0 (fully transparent) to 1 (fully opaque), (default: 1). */
	public float opacity = 1.f;

	public BlendingAttribute () {
		this(null);
	}

	public BlendingAttribute (final boolean blended, final int sourceFuncRGB, final int destFuncRGB, final int sourceFuncAlpha,
		final int destFuncAlpha, final int equationRGB, final int equationAlpha, final float opacity) {
		super(Type);
		this.blended = blended;
		this.sourceFunction = sourceFuncRGB;
		this.destFunction = destFuncRGB;
		this.sourceFunctionAlpha = sourceFuncAlpha;
		this.destFunctionAlpha = destFuncAlpha;
		this.equationRGB = equationRGB;
		this.equationAlpha = equationAlpha;
		this.opacity = opacity;
	}

	public BlendingAttribute (final boolean blended, final int sourceFunc, final int destFunc, final float opacity) {
		this(blended, sourceFunc, destFunc, sourceFunc, destFunc, GL20.GL_FUNC_ADD, GL20.GL_FUNC_ADD, opacity);
	}

	public BlendingAttribute (final int sourceFunc, final int destFunc, final float opacity) {
		this(true, sourceFunc, destFunc, opacity);
	}

	public BlendingAttribute (final int sourceFunc, final int destFunc) {
		this(sourceFunc, destFunc, 1.f);
	}

	public BlendingAttribute (final boolean blended, final float opacity) {
		this(blended, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, opacity);
	}

	public BlendingAttribute (final float opacity) {
		this(true, opacity);
	}

	public BlendingAttribute (final BlendingAttribute copyFrom) {
		this(copyFrom == null || copyFrom.blended, copyFrom == null ? GL20.GL_SRC_ALPHA : copyFrom.sourceFunction,
			copyFrom == null ? GL20.GL_ONE_MINUS_SRC_ALPHA : copyFrom.destFunction,
			copyFrom == null ? GL20.GL_SRC_ALPHA : copyFrom.sourceFunctionAlpha,
			copyFrom == null ? GL20.GL_ONE_MINUS_SRC_ALPHA : copyFrom.destFunctionAlpha,
			copyFrom == null ? GL20.GL_FUNC_ADD : copyFrom.equationRGB, copyFrom == null ? GL20.GL_FUNC_ADD : copyFrom.equationAlpha,
			copyFrom == null ? 1.f : copyFrom.opacity);
	}

	@Override
	public BlendingAttribute copy () {
		return new BlendingAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		result = 947 * result + (blended ? 1 : 0);
		result = 947 * result + sourceFunction;
		result = 947 * result + destFunction;
		result = 947 * result + sourceFunctionAlpha;
		result = 947 * result + destFunctionAlpha;
		result = 947 * result + equationRGB;
		result = 947 * result + equationAlpha;
		result = 947 * result + NumberUtils.floatToRawIntBits(opacity);
		return result;
	}

	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return (int)(type - o.type);
		BlendingAttribute other = (BlendingAttribute)o;
		if (blended != other.blended) return blended ? 1 : -1;
		if (sourceFunction != other.sourceFunction) return sourceFunction - other.sourceFunction;
		if (destFunction != other.destFunction) return destFunction - other.destFunction;
		if (sourceFunctionAlpha != other.sourceFunctionAlpha) return sourceFunctionAlpha - other.sourceFunctionAlpha;
		if (destFunctionAlpha != other.destFunctionAlpha) return destFunctionAlpha - other.destFunctionAlpha;
		if (equationRGB != other.equationRGB) return equationRGB - other.equationRGB;
		if (equationAlpha != other.equationAlpha) return equationAlpha - other.equationAlpha;
		return (MathUtils.isEqual(opacity, other.opacity)) ? 0 : (opacity < other.opacity ? 1 : -1);
	}
}
