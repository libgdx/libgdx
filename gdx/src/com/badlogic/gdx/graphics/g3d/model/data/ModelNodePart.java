package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntArray;

public class ModelNodePart {
	public String materialId;
	public String meshPartId;
	public ArrayMap<String, Matrix4> bones;
	public int uvMapping[][];
}
