package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * A combination of {@link MeshPart} and {@link Material}, used to represent a {@link Node}'s graphical
 * properties
 * @author badlogic
 */
public class NodePart {
	public MeshPart meshPart;
	public Material material;
	public ArrayMap<Node, Matrix4> bones;
	
	public NodePart() {}
	
	public NodePart(final MeshPart meshPart, final Material material) {
		this.meshPart = meshPart;
		this.material = material;
	}
}
