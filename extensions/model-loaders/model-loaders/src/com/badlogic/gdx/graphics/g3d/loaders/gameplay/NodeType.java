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

public enum NodeType {
	Node(1),
	Joint(2);
	
	private final int id;
	
	private NodeType(int id) {
		this.id = id;
	}
	
	public static NodeType forId(int id) {
		for(NodeType type: NodeType.values()) {
			if(type.id == id) return type;
		}
		throw new GdxRuntimeException("Unknown NodeType id " + id);
	}
}