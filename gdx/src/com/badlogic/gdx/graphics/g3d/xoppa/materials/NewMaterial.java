package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public interface NewMaterial {
	/** Returns a bitwise mask indicating the properties of this material */
	long getMask();
	
	public static interface BlendingMaterial extends NewMaterial {
		public final static long Mask = 1 << 0;
		public final static String Flag = "blendingFlag";
		
		int getBlendSourceFunction();
		int getBlendDestFunction();
	}
	
	public static interface DiffuseColorMaterial extends NewMaterial {
		public final static long Mask = 1 << 1;
		public final static String Flag = "diffuseColorFlag";
		public final static String Uniform = "diffuseColor";
		
		Color getDiffuseColor();
	}
	
	public static interface SpecularColorMaterial extends NewMaterial {
		public final static long Mask = 1 << 2;
		public final static String Flag = "specularColorFlag";
		public final static String Uniform = "specularColor";
		
		Color getSpecularColor();
	}
	
	public static interface EmmisiveColorMaterial extends NewMaterial {
		public final static long Mask = 1 << 3;
		public final static String Flag = "emmisiveColorFlag";
		public final static String Uniform = "emmisiveColor";
		
		Color getEmmisiveColor();
	}
	
	public static interface DiffuseTextureMaterial extends NewMaterial {
		public final static long Mask = 1 << 4;
		public final static String Flag = "diffuseTextureFlag";
		public final static String Uniform = "diffuseTexture";
		
		Texture getDiffuseTexture();
	}
	
	// Etc...
}
