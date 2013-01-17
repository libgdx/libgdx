package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ColorAttribute extends NewMaterial.Attribute {
	public final static String DiffuseAlias = "diffuseColor";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularColor";
	public final static long Specular = register(SpecularAlias);
	
	// Might be useful:...
	protected static long Mask = Diffuse | Specular;
	
	public final static ColorAttribute createDiffuse(final Color color) {
		return new ColorAttribute(Diffuse, color);
	}
	
	public final Color color = new Color();
	
	public ColorAttribute(final long type) {
		this(type, null);
	}
	
	public ColorAttribute(final long type, final Color color) {
		super(type);
		if ((Mask & type) != type)
			throw new GdxRuntimeException("Invalid type specified");
		if (color != null)
			this.color.set(color);
	}
	
	public ColorAttribute(final ColorAttribute copyFrom) {
		this(copyFrom.type, copyFrom.color);
	}

	@Override
	public Attribute copy () {
		return new ColorAttribute(this);
	}

	@Override
	protected boolean equals (Attribute other) {
		return ((ColorAttribute)other).color.equals(color);
	}
}