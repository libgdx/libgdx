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

import com.badlogic.gdx.graphics.g3d.Attribute;

public class IntAttribute extends Attribute {
	public static final String CullFaceAlias = "cullface";
	public static final long CullFace = register(CullFaceAlias);

	public static IntAttribute createCullFace (int value) {
		return new IntAttribute(CullFace, value);
	}

	public int value;

	public IntAttribute (long type) {
		super(type);
	}

	public IntAttribute (long type, int value) {
		super(type);
		this.value = value;
	}

	@Override
	public Attribute copy () {
		return new IntAttribute(type, value);
	}

	@Override
	public int hashCode () {
		int result = super.hashCode();
		result = 983 * result + value;
		return result; 
	}
	
	@Override
	public int compareTo (Attribute o) {
		if (type != o.type) return (int)(type - o.type);
		return value - ((IntAttribute)o).value;
	}
}
