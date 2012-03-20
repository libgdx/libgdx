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

public class Material {
	final public String name;
	final public MaterialAttribute[] attributes;
	public ShaderProgram shader;

	public Material (String name, MaterialAttribute... attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public void bind () {
		for (int i = 0; i < attributes.length; i++) {
			attributes[i].bind();
		}
	}

	public void bind (ShaderProgram program) {
		for (int i = 0; i < attributes.length; i++) {
			attributes[i].bind(program);
		}
	}

	public Material copy () {
		MaterialAttribute[] attributes = new MaterialAttribute[this.attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = this.attributes[i].copy();
		}
		return new Material(name, attributes);
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(attributes);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Material other = (Material)obj;
		if (other.attributes.length != attributes.length) return false;
		for (int i = 0; i < attributes.length; i++) {
			if (!attributes[i].equals(other.attributes[i])) return false;
		}
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	public boolean shaderEquals (Material other) {
		if (this == other) return true;

		int len = this.attributes.length;
		if (len != other.attributes.length) return false;

		for (int i = 0; i < len; i++) {
			final String str = this.attributes[i].name;
			if (str == null) return false;

			boolean matchFound = false;
			for (int j = 0; j < len; j++) {
				if (str.equals(other.attributes[j].name)) {
					matchFound = true;
					break;
				}
			}
			if (!matchFound) return false;
		}

		return true;
	}

}
