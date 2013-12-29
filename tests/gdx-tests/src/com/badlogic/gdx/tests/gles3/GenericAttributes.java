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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.Array;

/** This class describes any number of attributes, of any type.
 * @author mattijs driel */
public final class GenericAttributes {

	private static final int floatSize = 4;

	/** Sizes of some common attribute types. */
	public static final int POSITION = 3, NORMAL = 3, TEXCOORD = 2;

	/** Stride of all attributes, in bytes. */
	public final int stride;
	/** array of individual attributes. */
	public final GenericAttribute[] allAttributes;

	/** Adds generic attributes from the given types. The types describe the size of each attribute in number of floats. Generic
	 * attributes are created with a zero-based index corresponding to their order. When writing a vertex shader, it's recommended
	 * you define attributes with layout locations explicitly defined to match the type order you use when calling this
	 * constructor.
	 * @param types */
	public GenericAttributes (int... types) {
		int localstride = 0;
		allAttributes = new GenericAttribute[types.length];
		for (int i = 0; i < types.length; ++i) {
			allAttributes[i] = new GenericAttribute(i, types[i], localstride);

			localstride += allAttributes[i].size * floatSize;
		}
		stride = localstride;
	}

	public void bindAttributes () {
		for (GenericAttribute a : allAttributes) {
			Gdx.gl20.glEnableVertexAttribArray(a.index);
			Gdx.gl20.glVertexAttribPointer(a.index, a.size, GL30.GL_FLOAT, false, stride, a.offset);
		}
	}

	public static final class GenericAttribute {
		/** Index must match shader location. */
		public int index;
		/** 1, 2, 3 or 4 (floats) */
		public final int size;
		/** Attribute offset in bytes */
		public final int offset;

		public GenericAttribute (int index, int size, int offset) {
			this.index = index;
			this.size = size;
			this.offset = offset;
		}
	}

}
