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
package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Defines the types of primitives in a mesh
 * see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay-encoder/gameplay-bundle.txt#L73">gameplay-bundle.txt</a>
 * @author mzechner
 *
 */
public enum PrimitiveType {
	Triangles(4),
	TraingleStrip(5),
	Lines(1),
	LineStrip(3),
	Points(0);
	
	private final int id;
	
	private PrimitiveType(int id) {
		this.id = id;
	}
	
	public static PrimitiveType forId(int id) {
		for(PrimitiveType type: PrimitiveType.values()) {
			if(type.id == id) return type;
		}
		throw new GdxRuntimeException("Unknown PrimitiveType id " + id);
	}
}