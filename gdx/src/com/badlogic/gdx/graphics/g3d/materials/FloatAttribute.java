package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.g3d.materials.NewMaterial.Attribute;

public class FloatAttribute extends NewMaterial.Attribute {
	public static final String ShininessAlias = "shininess";
	public static final long Shininess = register(ShininessAlias);
	
	public float value;
	
	public FloatAttribute(long type) {
		super(type);
	}
	
	public FloatAttribute(long type, float value) {
		super(type);
		this.value = value;
	}

	@Override
	public Attribute copy () {
		return new FloatAttribute(type, value);
	}

	@Override
	protected boolean equals (Attribute other) {
		// FIXME use epsilon?
		return ((FloatAttribute)other).value == value;
	}
}
