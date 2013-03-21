package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class JsonMaterial {
	public enum MaterialType {
		Lambert,
		Phong
	}
	
	public String id;
	
	public MaterialType type;
	
	public Color ambient;
	public Color diffuse;
	public Color specular;
	public Color emissive;
	
	public float shininess;
	
	public Array<JsonTexture> diffuseTextures;
}
