package com.badlogic.gdx.graphics.g3d.materials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ColorAttribute extends NewMaterial.Attribute {
	public final static String DiffuseAlias = "diffuseColor";
	public final static long Diffuse = register(DiffuseAlias);
	public final static String SpecularAlias = "specularColor";
	public final static long Specular = register(SpecularAlias);
	public final static String AmbientAlias = "ambientColor";
	public static final long Ambient = register("ambientColor");
	public final static String EmissiveAlias = "emissiveColor";
	public static final long Emissive = register("emissiveColor");
	
	// Might be useful:...
	protected static long Mask = Ambient | Diffuse | Specular | Emissive;
	
	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}
	
	public final static ColorAttribute createDiffuse(final Color color) {
		return new ColorAttribute(Diffuse, color);
	}
	
	public final static ColorAttribute createSpecular(final Color color) {
		return new ColorAttribute(Specular, color);
	}
	
	public final Color color = new Color();
	
	public ColorAttribute(final long type) {
		this(type, null);
	}
	
	public ColorAttribute(final long type, final Color color) {
		super(type);
		if (!is(type))
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