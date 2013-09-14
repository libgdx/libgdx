package com.badlogic.gdx.graphics.g3d.model.data;

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
	public Color reflection;
	
	public float shininess;
	public float opacity = 1.f;
	
	public Array<ModelTexture> textures;
}
