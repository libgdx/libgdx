package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g3d.materials.Material.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BlendingAttribute extends Material.Attribute {
	public final static String Alias = "blended";
	public final static long Type = register(Alias);
	
	public final static boolean is(final long mask) {
		return (mask & Type) == mask;
	}
 
	public int sourceFunction;
	public int destFunction;
	public float opacity = 1.f;

	public BlendingAttribute() { 
		this(null); 
	}

	public BlendingAttribute(final int sourceFunc, final int destFunc, final float opacity) {
		super(Type);
		sourceFunction = sourceFunc;
		destFunction = destFunc;
		this.opacity = opacity; 
	}
	
	public BlendingAttribute(final int sourceFunc, final int destFunc) {
		this(sourceFunc, destFunc, 1.f);
	}
	
	public BlendingAttribute(final BlendingAttribute copyFrom) {
		this(copyFrom == null ? GL10.GL_SRC_ALPHA : copyFrom.sourceFunction,
			copyFrom == null ? GL10.GL_ONE_MINUS_SRC_ALPHA : copyFrom.destFunction,
			copyFrom == null ? 1.f : copyFrom.opacity);
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