package com.badlogic.gdx.graphics.g3d.materials;

import java.util.Arrays;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Material {
	public String name;
	public MaterialAttribute[] attributes;
	
	public Material(String name, MaterialAttribute ... attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public void bind() {
		for(int i = 0; i < attributes.length; i++) {
			attributes[i].bind();
		}
	}
	
	public void bind(ShaderProgram program) {
		for(int i = 0; i < attributes.length; i++) {
			attributes[i].bind(program);
		}
	}
	
	public Material copy() {
		MaterialAttribute[] attributes = new MaterialAttribute[this.attributes.length];
		for(int i = 0; i < attributes.length; i++) {
			attributes[i] = this.attributes[i].copy();
		}
		return new Material(name, attributes);
	}
	
	@Override public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(attributes);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Material other = (Material)obj;
		if(other.attributes.length != attributes.length) return false;
		for(int i = 0; i < attributes.length; i++) {
			if(!attributes[i].equals(other.attributes[i])) return false;
		}
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}
}
