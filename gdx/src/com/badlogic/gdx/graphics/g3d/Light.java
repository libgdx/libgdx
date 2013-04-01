package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** Basic general light class which can represent an ambient, directional, point and spot light.
 * Extend this class to add additional data.
 * @author Xoppa */
public class Light {
	/** The (diffuse) color of the light */
	public final Color color = new Color();
	/** The position of the light or null of not applicable. */ 
	public Vector3 position;
	/** The direction of the light or null of not applicable. */
	public Vector3 direction;
	/** The cut off angle of the light, only applicable if both direction and position aren't null. */ 
	public float angle = 0f;
	/** The power of the light */
	public float power = 1f;
	
	/** Create a new ambient light */
	public Light(final Color color) {
		this.color.set(color);
	}
	
	/** Create a new ambient light */
	public Light(float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
	}
	
	/** Create a new directional light */
	public Light(final Color color, final Vector3 direction) {
		this.color.set(color);
		this.direction = new Vector3(direction);
	}
	
	/** Create a new directional light */
	public Light(float r, float g, float b, float a, float x, float y, float z) {
		this.color.set(r, g, b, a);
		this.direction = new Vector3(x, y, z);
	}
	
	/** Create a new point light */
	public Light(final Color color, final Vector3 position, final float power) {
		this(color.r, color.g, color.b, color.a, position.x, position.y, position.z, power);
	}
	
	/** Create a new point light */
	public Light(final float red, final float green, final float blue, final float alpha, 
					final float x, final float y, final float z, final float power) {
		color.set(red, green, blue, alpha);
		this.position = new Vector3(x, y, z);
		this.power = power;
	}
	
	/** Create a new spot light */
	public Light(final Color color, final Vector3 position, final Vector3 direction, final float angle, final float power) {
		this.color.set(color);
		this.position = new Vector3(position);
		this.direction = new Vector3(direction);
		this.angle = angle;
		this.power = power;
	}
	
	public boolean equals(Light other) {
		if (other == null) return false;
		if (other == this) return true;
		return color.equals(other.color) && position.equals(other.position) && power == other.power;
	}
	
	@Override
	public boolean equals (Object arg0) {
		if (arg0 == null) return false;
		if (arg0 == this) return true;
		if (!(arg0 instanceof Light)) return false;
		return equals((Light)arg0);
	}
	
	public boolean isAmbientLight() {
		return position == null && direction == null;
	}
	
	public boolean isPointLight() {
		return position != null && direction == null;
	}
	
	public boolean isDirectionalLight() {
		return position == null && direction != null;
	}
	
	public boolean isSpotLight() {
		return position != null && direction != null;
	}
}
