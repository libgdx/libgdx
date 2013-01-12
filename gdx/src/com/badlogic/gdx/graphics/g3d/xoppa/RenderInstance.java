package com.badlogic.gdx.graphics.g3d.xoppa;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class RenderInstance {
	public Mesh mesh;
	public int meshPartOffset;
	public int meshPartSize;
	public Material material;
	public int primitiveType;
	public Matrix4 transform;
	public float distance;
	public Shader shader;
}