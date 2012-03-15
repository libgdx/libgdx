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

public class ColorAttribute extends MaterialAttribute {

	public enum ColorType {
		DIFFUSE, SPECULAR, EMISSIVE
	};

	static final private String[] shaderFlag = {"diffuseCol", "specularCol", "emissiveCol"};
	
	static final private String[] colorNames = {"diffuseCol", "specularCol", "emissiveCol"};	

	public final Color color = new Color();
	public final ColorType colorType;
	private final int type;

	public ColorAttribute (Color color, String name, ColorType colorType) {
		super(name);
		this.color.set(color);
		this.colorType = colorType;
		this.type = colorType.ordinal();
	}

	@Override
	public void bind () {
		if (Gdx.gl10 == null) throw new RuntimeException("Can't call ColorAttribute.bind() in a GL20 context");

		// todo how about emissive or specular?
		if (colorType == ColorType.DIFFUSE) Gdx.gl10.glColor4f(color.r, color.g, color.b, 1f);
	}

	@Override
	public void bind (ShaderProgram program) {
		program.setUniformf(colorNames[type], color.r, color.g, color.b);
	}

	@Override
	public MaterialAttribute copy () {
		return new ColorAttribute(color, name, colorType);
	}

	@Override
	public String getShaderFlag () {
		return shaderFlag[type];
	}
}
