package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial.Attribute;

public class BlendingAttribute extends NewMaterial.Attribute {
	public final static String Alias = "blended";
	public final static long Type = register(Alias);
 
	public int sourceFunction;
	public int destFunction;

	public BlendingAttribute() { 
		super(Type); 
	}
	
	public BlendingAttribute(final int sourceFunc, final int destFunc) {
		this();
		sourceFunction = sourceFunc;
		destFunction = destFunc;
	}
	
	public BlendingAttribute(final BlendingAttribute copyFrom) {
		this(copyFrom == null ? 0 : copyFrom.sourceFunction, copyFrom == null ? 0 : copyFrom.destFunction);
	}
	
	@Override
	public BlendingAttribute copy () {
		return new BlendingAttribute(this);
	}
	
	@Override
	protected boolean equals (final Attribute other) {
		return ((BlendingAttribute)other).sourceFunction == sourceFunction && 
			((BlendingAttribute)other).destFunction == destFunction; 
	}
}