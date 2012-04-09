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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;

public class ColorAttribute extends MaterialAttribute {

	static final public String diffuse = "diffuseColor";
	static final public String specular = "specularColor";
	static final public String emissive = "emissiveColor";

	public final Color color = new Color();

	protected ColorAttribute () {
	}

	public ColorAttribute (Color color, String name) {
		super(name);
		this.color.set(color);
	}

	@Override
	public void bind () {
		if (Gdx.gl10 == null) throw new RuntimeException("Can't call ColorAttribute.bind() in a GL20 context");

		// todo how about emissive or specular?
		// TODO replace string equals with something more performant
		if (diffuse.equals(diffuse)) Gdx.gl10.glColor4f(color.r, color.g, color.b, color.a);
	}

	@Override
	public void bind (ShaderProgram program) {
		program.setUniformf(name, color.r, color.g, color.b, color.a);
	}

	@Override
	public MaterialAttribute copy () {
		return new ColorAttribute(color, name);
	}

	@Override
	public void set (MaterialAttribute attr) {
		ColorAttribute colAttr = (ColorAttribute)attr;
		name = colAttr.name;
		final Color c = colAttr.color;
		color.r = c.r;
		color.g = c.g;
		color.b = c.b;
		color.a = c.a;
	}

	private final static Pool<ColorAttribute> pool = new Pool<ColorAttribute>() {
		@Override
		protected ColorAttribute newObject () {
			return new ColorAttribute();
		}
	};

	@Override
	public MaterialAttribute pooledCopy () {
		ColorAttribute attr = pool.obtain();
		attr.set(this);
		return attr;
	}

	@Override
	public void free () {
		if (isPooled) pool.free(this);
	}
}
