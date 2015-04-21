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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ColorAttribute extends Attribute {
	public final static String DiffuseAlias = "diffuseColor";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularColor";
	public final static long Specular = register(SpecularAlias);
	public final static String AmbientAlias = "ambientColor";
	public static final long Ambient = register(AmbientAlias);
	public final static String EmissiveAlias = "emissiveColor";
	public static final long Emissive = register(EmissiveAlias);
	public final static String ReflectionAlias = "reflectionColor";
	public static final long Reflection = register(ReflectionAlias);
	public final static String AmbientLightAlias = "ambientLightColor";
	public static final long AmbientLight = register(AmbientLightAlias);
	public final static String FogAlias = "fogColor";
	public static final long Fog = register(FogAlias);

	protected static long Mask = Ambient | Diffuse | Specular | Emissive | Reflection | AmbientLight | Fog;

	public final static boolean is (final long mask) {
		return (mask & Mask) != 0;
	}

	public final static ColorAttribute createAmbient (final Color color) {
		return new ColorAttribute(Ambient, color);
	}

	public final static ColorAttribute createAmbient (float r, float g, float b, float a) {
		return new ColorAttribute(Ambient, r, g, b, a);
	}

	public final static ColorAttribute createDiffuse (final Color color) {
		return new ColorAttribute(Diffuse, color);
	}

	public final static ColorAttribute createDiffuse (float r, float g, float b, float a) {
		return new ColorAttribute(Diffuse, r, g, b, a);
	}

	public final static ColorAttribute createSpecular (final Color color) {
		return new ColorAttribute(Specular, color);
	}

	public final static ColorAttribute createSpecular (float r, float g, float b, float a) {
		return new ColorAttribute(Specular, r, g, b, a);
	}

	public final static ColorAttribute createReflection (final Color color) {
		return new ColorAttribute(Reflection, color);
	}

	public final static ColorAttribute createReflection (float r, float g, float b, float a) {
		return new ColorAttribute(Reflection, r, g, b, a);
	}

	public final Color color = new Color();

	public ColorAttribute (final long type) {
		super(type);
		if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
	}

	public ColorAttribute (final long type, final Color color) {
		this(type);
		if (color != null) this.color.set(color);
	}

	public ColorAttribute (final long type, float r, float g, float b, float a) {
		this(type);
		this.color.set(r, g, b, a);
	}

	public ColorAttribute (final ColorAttribute copyFrom) {
		this(copyFrom.type, copyFrom.color);
	}

	@Override
	public Attribute copy () {
		return new ColorAttribute(this);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		result = 953 * result + color.toIntBits();
		return result; 
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return (int)(type - o.type);
		return ((ColorAttribute)o).color.toIntBits() - color.toIntBits();
	}
}
