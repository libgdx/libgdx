package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.g3d.materials.NewMaterial;

/**
 * A combination of {@link MeshPart} and {@link NewMaterial}, used to represent a {@link Node}'s graphical
 * properties
 * @author badlogic
 *
 * FIXME the name for this is horrible...
 */
public class MeshPartMaterial {
	public MeshPart meshPart;
	public NewMaterial material;
	
	public MeshPartMaterial() {}
	
	public MeshPartMaterial(final MeshPart meshPart, final NewMaterial material) {
		this.meshPart = meshPart;
		this.material = material;
	}
}
