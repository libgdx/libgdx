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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;

public class TextureAttribute extends MaterialAttribute {

	public final static int MAX_TEXTURE_UNITS = 16;
	static final public String diffuseTexture = "diffuseTexture";
	static final public String lightmapTexture = "lightmapTexture";
	static final public String specularTexture = "specularTexture";

	public Texture texture;
	public int unit;
	public int minFilter;
	public int magFilter;
	public int uWrap;
	public int vWrap;

	protected TextureAttribute () {
	}

	public TextureAttribute (Texture texture, int unit, String name, TextureFilter minFilter, TextureFilter magFilter,
		TextureWrap uWrap, TextureWrap vWrap) {
		this(texture, unit, name, minFilter.getGLEnum(), magFilter.getGLEnum(), uWrap.getGLEnum(), vWrap.getGLEnum());
	}

	public TextureAttribute (Texture texture, int unit, String name, int minFilter, int magFilter, int uWrap, int vWrap) {
		super(name);
		this.texture = texture;
		if (unit > MAX_TEXTURE_UNITS) throw new RuntimeException(MAX_TEXTURE_UNITS + " is max texture units supported");
		this.unit = unit;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
	}

	public TextureAttribute (Texture texture, int unit, String name) {
		this(texture, unit, name, texture.getMinFilter(), texture.getMagFilter(), texture.getUWrap(), texture.getVWrap());
	}

	@Override
	public void bind () {
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap);
	}

	@Override
	public void bind (ShaderProgram program) {
		texture.bind(unit);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, uWrap);
		Gdx.gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, vWrap);
		program.setUniformi(name, unit);
	}

	@Override
	public MaterialAttribute copy () {
		return new TextureAttribute(texture, unit, name, minFilter, magFilter, uWrap, vWrap);
	}

	@Override
	public void set (MaterialAttribute attr) {
		TextureAttribute texAttr = (TextureAttribute)attr;
		name = texAttr.name;
		texture = texAttr.texture;
		unit = texAttr.unit;
		magFilter = texAttr.magFilter;
		minFilter = texAttr.minFilter;
		uWrap = texAttr.uWrap;
		vWrap = texAttr.vWrap;
	}

	/** this method check if the texture portion of texture attribute is equal, name isn't used */
	public boolean texturePortionEquals (TextureAttribute other) {
		if (other == null) return false;
		if (this == other) return true;

		return (texture == other.texture) && (unit == other.unit) && (minFilter == other.minFilter)
			&& (magFilter == other.magFilter) && (uWrap == other.uWrap) && (vWrap == other.vWrap);

	}

	private final static Pool<TextureAttribute> pool = new Pool<TextureAttribute>() {
		@Override
		protected TextureAttribute newObject () {
			return new TextureAttribute();
		}
	};

	@Override
	public MaterialAttribute pooledCopy () {
		TextureAttribute attr = pool.obtain();
		attr.set(this);
		return attr;
	}

	@Override
	public void free () {
		if (isPooled) pool.free(this);
	}
}
