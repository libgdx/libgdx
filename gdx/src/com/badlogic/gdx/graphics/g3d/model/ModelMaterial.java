package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class ModelMaterial {
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
	
	public Array<ModelTexture> diffuseTextures;
}
