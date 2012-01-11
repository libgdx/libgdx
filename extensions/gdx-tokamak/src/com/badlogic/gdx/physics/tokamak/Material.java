package com.badlogic.gdx.physics.tokamak;

/**
 * Helper class for {@link Simulator#getMaterial(Material)} and {@link Simulator#setMaterial(Material)}
 * @author mzechner
 *
 */
public class Material {
	public int index;
	public float friction;
	public float restitution;
	
	public Material() {
	}
	
	public Material(int index, float friction, float restitution) {
		this.friction = friction;
		this.restitution = restitution;
	}
}
