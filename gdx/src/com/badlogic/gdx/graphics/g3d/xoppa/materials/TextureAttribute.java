package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial.Attribute;
import com.badlogic.gdx.graphics.g3d.xoppa.utils.TextureDescription;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureAttribute extends NewMaterial.Attribute {
	public final static String DiffuseAlias = "diffuseTexture";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularTexture";
	public final static long Specular = register(SpecularAlias);
	
	// Might be useful:...
	protected static long Mask = Diffuse | Specular;
	
	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}
	
	public final TextureDescription textureDescription;
	
	public TextureAttribute(final long type, final TextureDescription textureDescription) {
		super(type);
		if (!is(type))
			throw new GdxRuntimeException("Invalid type specified");
		this.textureDescription = textureDescription;
	}
	
	public TextureAttribute(final long type) {
		this(type, null);
	}
	
	public TextureAttribute(final TextureAttribute copyFrom) {
		this(copyFrom.getType(), copyFrom.textureDescription);
	}
	
	@Override
	public Attribute copy () {
		return new TextureAttribute(this);
	}

	@Override
	protected boolean equals (Attribute other) {
		return ((TextureAttribute)other).textureDescription.equals(textureDescription);
	}
}
