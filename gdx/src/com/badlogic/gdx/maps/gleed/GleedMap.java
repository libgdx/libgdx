package com.badlogic.gdx.maps.gleed;

import com.badlogic.gdx.maps.Map;

public class GleedMap extends Map {
	
	private String m_name;
		
	public GleedMap() {
		this("");
	}
	
	public GleedMap(String name) {
		super();
		m_name = name;
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String name) {
		m_name = name;
	}
}
