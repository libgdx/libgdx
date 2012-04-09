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

import java.util.Arrays;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class Material {
	public String name;
	public Array<MaterialAttribute> attributes;
	/** This flag is true if material contain blendingAttribute */
	public boolean needBlending;

	public ShaderProgram shader;

	public Material () {
		attributes = new Array<MaterialAttribute>(2);
	}

	public Material (String name, Array<MaterialAttribute> attributes) {
		this.name = name;
		this.attributes = attributes;

		// this way we foresee if blending is needed with this material and rendering can deferred more easily
		boolean blendingNeeded = false;
		for (int i = 0; i < this.attributes.size; i++) {
			if (this.attributes.get(i) instanceof BlendingAttribute) blendingNeeded = true;
		}
		this.needBlending = blendingNeeded;
	}

	public Material (String name, MaterialAttribute... attributes) {
		this.name = name;
		this.attributes = new Array<MaterialAttribute>(attributes);

		// this way we foresee if blending is needed with this material and rendering can deferred more easily
		boolean blendingNeeded = false;
		for (int i = 0; i < this.attributes.size; i++) {
			if (this.attributes.get(i) instanceof BlendingAttribute) blendingNeeded = true;
		}
		this.needBlending = blendingNeeded;

	}

	public void bind () {
		for (int i = 0; i < attributes.size; i++) {
			attributes.get(i).bind();
		}
	}

	public void bind (ShaderProgram program) {
		for (int i = 0; i < attributes.size; i++) {
			attributes.get(i).bind(program);
		}
	}

	public Material copy () {
		Array<MaterialAttribute> attributes = new Array<MaterialAttribute>(this.attributes.size);
		for (int i = 0; i < attributes.size; i++) {
			attributes.add(this.attributes.get(i).copy());
		}
		final Material copy = new Material(name, attributes);
		copy.shader = this.shader;
		return copy;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + attributes.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Material other = (Material)obj;
		if (other.attributes.size != attributes.size) return false;
		for (int i = 0; i < attributes.size; i++) {
			if (!attributes.get(i).equals(other.attributes.get(i))) return false;
		}
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	public boolean shaderEquals (Material other) {
		if (this == other) return true;

		int len = this.attributes.size;
		if (len != other.attributes.size) return false;

		for (int i = 0; i < len; i++) {
			final String str = this.attributes.get(i).name;
			if (str == null) return false;

			boolean matchFound = false;
			for (int j = 0; j < len; j++) {
				if (str.equals(other.attributes.get(j).name)) {
					matchFound = true;
					break;
				}
			}
			if (!matchFound) return false;
		}

		return true;
	}

	public void setPooled (Material material) {
		name = material.name;
		shader = material.shader;
		attributes.clear();
		for (MaterialAttribute attr : material.attributes) {
			if (attr instanceof BlendingAttribute) needBlending = true;
			attributes.add(attr.pooledCopy());
		}
	}
}
