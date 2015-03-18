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

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/** @author xoppa Base class specifying only a renderable entity */
public abstract class BaseEntity implements Disposable {
	public Matrix4 transform;
	public ModelInstance modelInstance;
	private Color color = new Color(1f, 1f, 1f, 1f);

	public Color getColor () {
		return color;
	}

	public void setColor (Color color) {
		setColor(color.r, color.g, color.b, color.a);
	}

	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		if (modelInstance != null) {
			for (Material m : modelInstance.materials) {
				ColorAttribute ca = (ColorAttribute)m.get(ColorAttribute.Diffuse);
				if (ca != null) ca.color.set(r, g, b, a);
			}
		}
	}
}
