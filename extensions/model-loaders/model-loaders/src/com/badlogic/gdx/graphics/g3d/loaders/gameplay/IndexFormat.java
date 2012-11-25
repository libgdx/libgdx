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
