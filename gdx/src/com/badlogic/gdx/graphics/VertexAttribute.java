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

package com.badlogic.gdx.graphics;

import java.nio.FloatBuffer;


import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexArray;

/** A single vertex attribute defined by its {@link Usage}, its number of components and its shader alias. The Usage is used
 * for uniquely identifying the vertex attribute from among its {@linkplain VertexAttributes} siblings. The number of components 
 * defines how many components the attribute has. The alias defines to which shader attribute this attribute should bind. The alias 
 * is used by a {@link Mesh} when drawing with a {@link ShaderProgram}. The alias can be changed at any time.
 * 
 * @author mzechner */
public final class VertexAttribute {
	/** The attribute {@link Usage}, used for identification. **/
	public final int usage;
	/** the number of components this attribute has **/
	public final int numComponents;
	/** For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned) */
	public final boolean normalized;
	/** the OpenGL type of each component, e.g. {@link GL20#GL_FLOAT} or {@link GL20#GL_UNSIGNED_BYTE}  */
	public final int type;
	/** the offset of this attribute in bytes, don't change this! **/
	public int offset;
	/** the alias for the attribute used in a {@link ShaderProgram} **/
	public String alias;
	/** optional unit/index specifier, used for texture coordinates and bone weights **/
	public int unit;
	private final int usageIndex;

	/** Constructs a new VertexAttribute. The GL data type is automatically selected based on the usage.
	 * 
	 * @param usage The attribute {@link Usage}, used to select the {@link #type} and for identification.
	 * @param numComponents the number of components of this attribute, must be between 1 and 4.
	 * @param alias the alias used in a shader for this attribute. Can be changed after construction. */
	public VertexAttribute (int usage, int numComponents, String alias) {
		this(usage, numComponents, alias, 0);
	}

	/** Constructs a new VertexAttribute. The GL data type is automatically selected based on the usage.
	 * 
	 * @param usage The attribute {@link Usage}, used to select the {@link #type} and for identification.
	 * @param numComponents the number of components of this attribute, must be between 1 and 4.
	 * @param alias the alias used in a shader for this attribute. Can be changed after construction.
	 * @param unit Optional unit/index specifier, used for texture coordinates and bone weights */
	public VertexAttribute (int usage, int numComponents, String alias, int unit) {
		this(usage, numComponents, usage == Usage.ColorPacked ? GL20.GL_UNSIGNED_BYTE : GL20.GL_FLOAT, 
				usage == Usage.ColorPacked, alias, unit);
	}

	/** Constructs a new VertexAttribute.
	 * 
	 * @param usage The attribute {@link Usage}, used for identification.
	 * @param numComponents the number of components of this attribute, must be between 1 and 4.
	 * @param type the OpenGL type of each component, e.g. {@link GL20#GL_FLOAT} or {@link GL20#GL_UNSIGNED_BYTE}. Since {@link Mesh}
	 * stores vertex data in 32bit floats, the total size of this attribute (type size times number of components) must be a 
	 * multiple of four.
	 * @param normalized For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned) 
	 * @param alias The alias used in a shader for this attribute. Can be changed after construction. */
	public VertexAttribute (int usage, int numComponents, int type, boolean normalized, String alias) {
		this(usage, numComponents, type, normalized, alias, 0);
	}
	
	/** Constructs a new VertexAttribute.
	 * 
	 * @param usage The attribute {@link Usage}, used for identification.
	 * @param numComponents the number of components of this attribute, must be between 1 and 4.
	 * @param type the OpenGL type of each component, e.g. {@link GL20#GL_FLOAT} or {@link GL20#GL_UNSIGNED_BYTE}. Since {@link Mesh}
	 * stores vertex data in 32bit floats, the total size of this attribute (type size times number of components) must be a 
	 * multiple of four bytes.
	 * @param normalized For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned) 
	 * @param alias The alias used in a shader for this attribute. Can be changed after construction.
	 * @param unit Optional unit/index specifier, used for texture coordinates and bone weights */
	public VertexAttribute (int usage, int numComponents, int type, boolean normalized, String alias, int unit) {
		this.usage = usage;
		this.numComponents = numComponents;
		this.type = type;
		this.normalized = normalized;
		this.alias = alias;
		this.unit = unit;
		this.usageIndex = Integer.numberOfTrailingZeros(usage);
	}
	
	/** @return A copy of this VertexAttribute with the same parameters. The {@link #offset} is not copied and must
	 * be recalculated, as is typically done by the {@linkplain VertexAttributes} that owns the VertexAttribute. */
	public VertexAttribute copy (){
		return new VertexAttribute(usage, numComponents, type, normalized, alias, unit);
	}

	public static VertexAttribute Position () {
		return new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE);
	}

	public static VertexAttribute TexCoords (int unit) {
		return new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + unit, unit);
	}

	public static VertexAttribute Normal () {
		return new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE);
	}
	
	public static VertexAttribute ColorPacked () {
		return new VertexAttribute(Usage.ColorPacked, 4, GL20.GL_UNSIGNED_BYTE, true, ShaderProgram.COLOR_ATTRIBUTE);
	}

	public static VertexAttribute ColorUnpacked () {
		return new VertexAttribute(Usage.ColorUnpacked, 4, GL20.GL_FLOAT, false, ShaderProgram.COLOR_ATTRIBUTE);
	}

	public static VertexAttribute Tangent () {
		return new VertexAttribute(Usage.Tangent, 3, ShaderProgram.TANGENT_ATTRIBUTE);
	}

	public static VertexAttribute Binormal () {
		return new VertexAttribute(Usage.BiNormal, 3, ShaderProgram.BINORMAL_ATTRIBUTE);
	}

	public static VertexAttribute BoneWeight (int unit) {
		return new VertexAttribute(Usage.BoneWeight, 2, ShaderProgram.BONEWEIGHT_ATTRIBUTE + unit, unit);
	}

	/** Tests to determine if the passed object was created with the same parameters */
	@Override
	public boolean equals (final Object obj) {
		if (!(obj instanceof VertexAttribute)) {
			return false;
		}
		return equals((VertexAttribute)obj);
	}

	public boolean equals (final VertexAttribute other) {
		return other != null && usage == other.usage && numComponents == other.numComponents 
			&& type == other.type && normalized == other.normalized && alias.equals(other.alias)
			&& unit == other.unit;
	}

	/** @return A unique number specifying the usage index (3 MSB) and unit (1 LSB). */
	public int getKey () {
		return (usageIndex << 8) + (unit & 0xFF);
	}
	
	/** @return How many bytes this attribute uses. */
	public int getSizeInBytes () {
		switch (type) {
		case GL20.GL_FLOAT:
		case GL20.GL_FIXED:
			return 4 * numComponents;
		case GL20.GL_UNSIGNED_BYTE:
		case GL20.GL_BYTE:
			return numComponents;
		case GL20.GL_UNSIGNED_SHORT:
		case GL20.GL_SHORT:
			return 2 * numComponents;
		}
		return 0;
	}

	@Override
	public int hashCode () {
		int result = getKey();
		result = 541 * result + numComponents;
		result = 541 * result + alias.hashCode();
		return result;
	}
}
