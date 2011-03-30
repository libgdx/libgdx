package com.badlogic.gdx.graphics.g3d.model.keyframe;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.ObjectMap;

public class SubMesh {
	public String name;
	public Material material;
	public Mesh mesh;
	public final ObjectMap<String, KeyframedAnimation> animations = new ObjectMap<String, KeyframedAnimation>();
}
