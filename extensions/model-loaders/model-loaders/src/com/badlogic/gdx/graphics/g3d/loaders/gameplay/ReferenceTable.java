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
