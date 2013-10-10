package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class TextureAttribute extends Attribute {
	public final static String DiffuseAlias = "diffuseTexture";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularTexture";
	public final static long Specular = register(SpecularAlias);
	public final static String BumpAlias = "bumpTexture";
	public final static long Bump = register(BumpAlias);
	public final static String NormalAlias = "normalTexture";
	public final static long Normal = register(NormalAlias);
	
	// FIXME add more types!
	// FIXME add scaling + offset?
	// FIXME add filter settings? MipMap needs to be obeyed during loading :/
	
	protected static long Mask = Diffuse | Specular | Bump | Normal;
	
	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}
	
	public static TextureAttribute createDiffuse(final Texture texture) {
		return new TextureAttribute(Diffuse, texture);
	}
	
	public static TextureAttribute createSpecular(final Texture texture) {
		return new TextureAttribute(Specular, texture);
	}
	
	public final TextureDescriptor<Texture> textureDescription;
	
	public TextureAttribute(final long type) {
		super(type);
		if (!is(type))
			throw new GdxRuntimeException("Invalid type specified");
		textureDescription = new TextureDescriptor<Texture>();
	}
	
	public <T extends Texture> TextureAttribute(final long type, final TextureDescriptor<T> textureDescription) {
		this(type);
		this.textureDescription.set(textureDescription);
	}
	
	public TextureAttribute(final long type, final Texture texture) {
		this(type);
		textureDescription.texture = texture;
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
}
