package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CubemapAttribute extends Attribute {
	public final static String EnvironmentMapAlias = "environmentMapTexture";
	public final static long EnvironmentMap = register(EnvironmentMapAlias);
	
	// FIXME add more types!
	// FIXME add scaling + offset?
	// FIXME add filter settings? MipMap needs to be obeyed during loading :/
	
	protected static long Mask = EnvironmentMap;
	
	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}
	
	public final TextureDescriptor<Cubemap> textureDescription;
	
	public CubemapAttribute(final long type) {
		super(type);
		if (!is(type))
			throw new GdxRuntimeException("Invalid type specified");
		textureDescription = new TextureDescriptor<Cubemap>();
	}
	
	public <T extends Cubemap> CubemapAttribute(final long type, final TextureDescriptor<T> textureDescription) {
		this(type);
		this.textureDescription.set(textureDescription);
	}
	
	public CubemapAttribute(final long type, final Cubemap texture) {
		this(type);
		textureDescription.texture = texture;
	}
	
	public CubemapAttribute(final CubemapAttribute copyFrom) {
		this(copyFrom.type, copyFrom.textureDescription);
	}
	
	@Override
	public Attribute copy () {
		return new CubemapAttribute(this);
	}

	@Override
	protected boolean equals (Attribute other) {
		return ((CubemapAttribute)other).textureDescription.equals(textureDescription);
	}
}
