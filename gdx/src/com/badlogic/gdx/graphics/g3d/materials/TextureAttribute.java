package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureAttribute extends NewMaterial.Attribute {
	public final static String DiffuseAlias = "diffuseTexture";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularTexture";
	public final static long Specular = register(SpecularAlias);
	
	// FIXME add more types!
	// FIXME add scaling + offset?
	// FIXME add filter settings? MipMap needs to be obeyed during loading :/
	
	protected static long Mask = Diffuse | Specular;
	
	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}
	
	public final TextureDescriptor textureDescription;
	
	public TextureAttribute(final long type, final TextureDescriptor textureDescription) {
		super(type);
		if (!is(type))
			throw new GdxRuntimeException("Invalid type specified");
		this.textureDescription = textureDescription; // FIXME Add TextureDescriptor#copy or #addRef ?
	}
	
	public TextureAttribute(final long type) {
		this(type, null);
	}
	
	public TextureAttribute(final TextureAttribute copyFrom) {
		this(copyFrom.type, copyFrom.textureDescription);
	}
	
	@Override
	public Attribute copy () {
		return new TextureAttribute(this);
	}

	@Override
	protected boolean equals (Attribute other) {
		return ((TextureAttribute)other).textureDescription.equals(textureDescription);
	}

	@Override
	public void dispose () {
		// FIXME dispose texture if needed
	}
}
