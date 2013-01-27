package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.xoppa.materials.NewMaterial;

public class Renderable {
	public Mesh mesh;
	public int meshPartOffset;
	public int meshPartSize;
	public int primitiveType;
	public NewMaterial material;
}