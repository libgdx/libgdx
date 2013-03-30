package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.materials.Material;

/**
 * A combination of {@link MeshPart} and {@link Material}, used to represent a {@link Node}'s graphical
 * properties
 * @author badlogic
 *
 * FIXME the name for this is horrible...
 */
public class MeshPartMaterial {
	public MeshPart meshPart;
	public Material material;
	
	public MeshPartMaterial() {}
	
	public MeshPartMaterial(final MeshPart meshPart, final Material material) {
		this.meshPart = meshPart;
		this.material = material;
	}
}
