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

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Values;

/**
 * A list of references, queryable by id
 * @author mzechner
 *
 */
public class ReferenceTable {
	private final ObjectMap<String, Reference> refs = new ObjectMap<String, Reference>();
	
	public void add(Reference ref) {
		this.refs.put(ref.getId(), ref);
	}
	
	public Reference get(String id) {
		return refs.get(id);
	}
	
	public Values<Reference> getRefs() {
		return refs.values();
	}
}