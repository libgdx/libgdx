package com.badlogic.gdx.graphics.g3d.xoppa.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class Light {
	public final Color color = new Color();
	public final Vector3 position = new Vector3();
	public float power = 1f;
	
	public Light(final Color color, final Vector3 position, final float power) {
		this(color.r, color.g, color.b, color.a, position.x, position.y, position.z, power);
	}
	
	public Light(final float red, final float green, final float blue, final float alpha, 
					final float x, final float y, final float z, final float power) {
		color.set(red, green, blue, alpha);
		position.set(x, y, z);
		this.power = power;
	}
}
