package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;

public class Renderable {
	public Mesh mesh;
	public int meshPartOffset;
	public int meshPartSize;
	public int primitiveType;
	public NewMaterial material;
}