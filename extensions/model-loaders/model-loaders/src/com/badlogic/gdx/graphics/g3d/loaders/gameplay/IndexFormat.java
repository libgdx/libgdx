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

public enum IndexFormat {
	Index8(0x1401),
	Index16(0x1403),
	Index32(0x1405);
	
	private final int id;
	
	private IndexFormat(int id) {
		this.id = id;
	}
	
	public static IndexFormat forId(int id) {
		for(IndexFormat type: IndexFormat.values()) {
			if(type.id == id) return type;
		}
		throw new GdxRuntimeException("Unknown IndexFormat id " + id);
	}
}