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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.math.Vector2;

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
	
	// FIXME add more types!
	// FIXME add filter settings? MipMap needs to be obeyed during loading :/

	protected static long Mask = Diffuse | Specular | Bump | Normal | Ambient | Emissive | Reflection;

	public final static boolean is (final long mask) {
		return (mask & Mask) != 0;
	}

	/** uv translation (default: 0,0) */
	public Vector2 uvTranslation = new Vector2(0.0f, 0.0f);
	/** uv scaling (default: 1,1) */
	public Vector2 uvScaling = new Vector2(1.0f, 1.0f);

	public static TextureAttribute createDiffuse (final Texture texture) {
		return new TextureAttribute(Diffuse, texture);
	}

	public static TextureAttribute createSpecular (final Texture texture) {
		return new TextureAttribute(Specular, texture);
	}

	public static TextureAttribute createNormal (final Texture texture) {
		return new TextureAttribute(Normal, texture);
	}
	
	public static TextureAttribute createBump (final Texture texture) {
		return new TextureAttribute(Bump, texture);
	}
	
	public static TextureAttribute createAmbient (final Texture texture) {
		return new TextureAttribute(Ambient, texture);
	}	
	
	public static TextureAttribute createEmissive (final Texture texture) {
		return new TextureAttribute(Emissive, texture);
	}
	
	public static TextureAttribute createReflection (final Texture texture) {
		return new TextureAttribute(Reflection, texture);
	}

	public final TextureDescriptor<Texture> textureDescription;

	public TextureAttribute (final long type) {
		super(type);
		if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
		textureDescription = new TextureDescriptor<Texture>();
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription) {
		this(type);
		this.textureDescription.set(textureDescription);
	}

	public <T extends Texture> TextureAttribute (final long type, final TextureDescriptor<T> textureDescription, final Vector2 uvTranslation, final Vector2 uvScaling) {
		this(type, textureDescription);
		if (uvTranslation != null) this.uvTranslation.set(uvTranslation);
		if (uvScaling != null) this.uvScaling.set(uvScaling);
	}

	public TextureAttribute (final long type, final Texture texture) {
		this(type);
		textureDescription.texture = texture;
	}

	public TextureAttribute (final TextureAttribute copyFrom) {
		this(copyFrom.type, copyFrom.textureDescription, copyFrom.uvTranslation, copyFrom.uvScaling);
	}

	@Override
	public Attribute copy () {
		return new TextureAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = (int)type;
		result = 991 * result + textureDescription.hashCode();
		result = 991 * result + uvTranslation.hashCode();
		result = 991 * result + uvScaling.hashCode();
		return result;
	}
}
