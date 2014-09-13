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

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureAttribute extends Attribute {
	public final static String DiffuseAlias = "diffuseTexture";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularTexture";
	public final static long Specular = register(SpecularAlias);
	public final static String BumpAlias = "bumpTexture";
	public final static long Bump = register(BumpAlias);
	public final static String NormalAlias = "normalTexture";
	public final static long Normal = register(NormalAlias);

	protected static long Mask = Diffuse | Specular | Bump | Normal;

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

	public final TextureDescriptor<Texture> textureDescription;
	public float uvOffsetX = 0;
	public float uvOffsetY = 0;
	public float uvScaleX = 1;
	public float uvScaleY = 1;

	public TextureAttribute (final long type) {
		super(type);
		if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
		textureDescription = new TextureDescriptor<Texture>();
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription) {
		this(type);
		this.textureDescription.set(textureDescription);
	}
	
	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription, float uvOffsetX,
		float uvOffsetY, float uvScaleX, float uvScaleY) {
		this(type, textureDescription);
		this.uvOffsetX = uvOffsetX;
		this.uvOffsetY = uvOffsetY;
		this.uvScaleX = uvScaleX;
		this.uvScaleY = uvScaleY;
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
		this(copyFrom.type, copyFrom.textureDescription, copyFrom.uvOffsetX, copyFrom.uvOffsetY, copyFrom.uvScaleX, copyFrom.uvScaleY);
	}

	public void set (final TextureRegion region) {
		textureDescription.texture = region.getTexture();
		uvOffsetX = region.getU();
		uvOffsetY = region.getV();
		uvScaleX = region.getU2() - uvOffsetX;
		uvScaleY = region.getV2() - uvOffsetY;
	}

	@Override
	public Attribute copy () {
		return new TextureAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = (int)type;
		result = 991 * result + textureDescription.hashCode();
		result = 991 * result + Float.floatToRawIntBits(uvOffsetX);
		result = 991 * result + Float.floatToRawIntBits(uvOffsetY);
		result = 991 * result + Float.floatToRawIntBits(uvScaleX);
		result = 991 * result + Float.floatToRawIntBits(uvScaleY);
		return result;
	}
}
