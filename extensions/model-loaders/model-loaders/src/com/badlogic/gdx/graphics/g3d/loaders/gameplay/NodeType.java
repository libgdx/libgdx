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
