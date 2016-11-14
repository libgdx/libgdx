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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class TextureAttribute extends Attribute {
	public final static String DiffuseAlias = "diffuseTexture";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularTexture";
	public final static long Specular = register(SpecularAlias);
	public final static String BumpAlias = "bumpTexture";
	public final static long Bump = register(BumpAlias);
	public final static String NormalAlias = "normalTexture";
	public final static long Normal = register(NormalAlias);
	public final static String AmbientAlias = "ambientTexture";
	public final static long Ambient = register(AmbientAlias);
	public final static String EmissiveAlias = "emissiveTexture";
	public final static long Emissive = register(EmissiveAlias);
	public final static String ReflectionAlias = "reflectionTexture";
	public final static long Reflection = register(ReflectionAlias);
	
	protected static long Mask = Diffuse | Specular | Bump | Normal | Ambient | Emissive | Reflection;

	public final static boolean is (final long mask) {
		return (mask & Mask) != 0;
	}

	public static TextureAttribute createDiffuse (final Texture texture) {
		return new TextureAttribute(Diffuse, texture);
	}

	public static TextureAttribute createDiffuse (final TextureRegion region) {
		return new TextureAttribute(Diffuse, region);
	}

	public static TextureAttribute createSpecular (final Texture texture) {
		return new TextureAttribute(Specular, texture);
	}

	public static TextureAttribute createSpecular (final TextureRegion region) {
		return new TextureAttribute(Specular, region);
	}

	public static TextureAttribute createNormal (final Texture texture) {
		return new TextureAttribute(Normal, texture);
	}

	public static TextureAttribute createNormal (final TextureRegion region) {
		return new TextureAttribute(Normal, region);
	}

	public static TextureAttribute createBump (final Texture texture) {
		return new TextureAttribute(Bump, texture);
	}
	
	public static TextureAttribute createBump (final TextureRegion region) {
		return new TextureAttribute(Bump, region);
	}
	
	public static TextureAttribute createAmbient (final Texture texture) {
		return new TextureAttribute(Ambient, texture);
	}
	
	public static TextureAttribute createAmbient (final TextureRegion region) {
		return new TextureAttribute(Ambient, region);
	}
	
	public static TextureAttribute createEmissive (final Texture texture) {
		return new TextureAttribute(Emissive, texture);
	}
	
	public static TextureAttribute createEmissive (final TextureRegion region) {
		return new TextureAttribute(Emissive, region);
	}
	
	public static TextureAttribute createReflection (final Texture texture) {
		return new TextureAttribute(Reflection, texture);
	}
	
	public static TextureAttribute createReflection (final TextureRegion region) {
		return new TextureAttribute(Reflection, region);
	}

	public final TextureDescriptor<Texture> textureDescription;
	public float offsetU = 0;
	public float offsetV = 0;
	public float scaleU = 1;
	public float scaleV = 1;
	/** The index of the texture coordinate vertex attribute to use for this TextureAttribute. Whether this value is used, depends
	 * on the shader and {@link Attribute#type} value. For basic (model specific) types (e.g. {@link #Diffuse}, {@link #Normal},
	 * etc.), this value is usually ignored and the first texture coordinate vertex attribute is used. */
	public int uvIndex = 0;

	public TextureAttribute (final long type) {
		super(type);
		if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
		textureDescription = new TextureDescriptor<Texture>();
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription) {
		this(type);
		this.textureDescription.set(textureDescription);
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription, float offsetU,
		float offsetV, float scaleU, float scaleV, int uvIndex) {
		this(type, textureDescription);
		this.offsetU = offsetU;
		this.offsetV = offsetV;
		this.scaleU = scaleU;
		this.scaleV = scaleV;
		this.uvIndex = uvIndex;
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription, float offsetU,
		float offsetV, float scaleU, float scaleV) {
		this(type, textureDescription, offsetU, offsetV, scaleU, scaleV, 0);
	}

	public TextureAttribute (final long type, final Texture texture) {
		this(type);
		textureDescription.texture = texture;
	}

	public TextureAttribute (final long type, final TextureRegion region) {
		this(type);
		set(region);
	}

	public TextureAttribute (final TextureAttribute copyFrom) {
		this(copyFrom.type, copyFrom.textureDescription, copyFrom.offsetU, copyFrom.offsetV, copyFrom.scaleU, copyFrom.scaleV,
			copyFrom.uvIndex);
	}

	public void set (final TextureRegion region) {
		textureDescription.texture = region.getTexture();
		offsetU = region.getU();
		offsetV = region.getV();
		scaleU = region.getU2() - offsetU;
		scaleV = region.getV2() - offsetV;
	}

	@Override
	public Attribute copy () {
		return new TextureAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		result = 991 * result + textureDescription.hashCode();
		result = 991 * result + NumberUtils.floatToRawIntBits(offsetU);
		result = 991 * result + NumberUtils.floatToRawIntBits(offsetV);
		result = 991 * result + NumberUtils.floatToRawIntBits(scaleU);
		result = 991 * result + NumberUtils.floatToRawIntBits(scaleV);
		result = 991 * result + uvIndex;
		return result;
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		TextureAttribute other = (TextureAttribute)o;
		final int c = textureDescription.compareTo(other.textureDescription);
		if (c != 0) return c;
		if (uvIndex != other.uvIndex) return uvIndex - other.uvIndex;
		if (!MathUtils.isEqual(scaleU, other.scaleU)) return scaleU > other.scaleU ? 1 : -1;
		if (!MathUtils.isEqual(scaleV, other.scaleV)) return scaleV > other.scaleV ? 1 : -1;
		if (!MathUtils.isEqual(offsetU, other.offsetU)) return offsetU > other.offsetU ? 1 : -1;
		if (!MathUtils.isEqual(offsetV, other.offsetV)) return offsetV > other.offsetV ? 1 : -1;
		return 0;
	}
}
