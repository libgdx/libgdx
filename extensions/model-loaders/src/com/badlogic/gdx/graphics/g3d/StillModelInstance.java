package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public interface StillModelInstance {
	public Matrix4 getTransform();
	public Vector3 getSortCenter();
	public Material[] getMaterials();
}
