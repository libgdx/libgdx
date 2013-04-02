package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** Basic general light class which can represent an ambient, directional, point and spot light.
 * Extend this class to add additional data.
 * @author Xoppa */
public class Light {
	public final static int NONE = 			0x00; // Inactive
	public final static int AMBIENT = 		0x01; // Active
	public final static int POINT = 			0x03; // Active | Position
	public final static int DIRECTIONAL = 	0x05; // Active | Direction
	public final static int SPOT = 			0x07; // Active | Position | Direction
	
	/** The type of light */
	public int type = NONE;
	/** The (diffuse) color of the light */
	public final Color color = new Color();
	/** The position of the light, only applicable for point and spot lights. */ 
	public final Vector3 position = new Vector3(0,0,0);
	/** The direction of the light, only applicable for directional and spot lights. */
	public final Vector3 direction = new Vector3(0,-1,0);
	/** The cut off angle of the light, only applicable for spot lights. */ 
	public float angle = 90f;
	/** The power of the light, only applicable for point and spot lights. */
	public float power = 1f;
	
	public Light() { }
	
	/** Create a new light which is a copy of the specified light. */
	public Light(final Light other) {
		set(other);
	}
	
	/** Create a new ambient light */
	public Light(final Color color) {
		this(color.r, color.g, color.b, color.a);
	}
	
	/** Create a new ambient light */
	public Light(float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
		type = AMBIENT;
	}
	
	/** Create a new directional light */
	public Light(final Color color, final Vector3 direction) {
		this(color.r, color.g, color.b, color.a, direction.x, direction.y, direction.z);
	}
	
	/** Create a new directional light */
	public Light(float r, float g, float b, float a, float x, float y, float z) {
		this.color.set(r, g, b, a);
		this.direction.set(x, y, z);
		type = DIRECTIONAL;
	}
	
	/** Create a new point light */
	public Light(final Color color, final Vector3 position, final float power) {
		this(color.r, color.g, color.b, color.a, position.x, position.y, position.z, power);
	}
	
	/** Create a new point light */
	public Light(final float red, final float green, final float blue, final float alpha, 
					final float x, final float y, final float z, final float power) {
		color.set(red, green, blue, alpha);
		this.position.set(x, y, z);
		this.power = power;
		type = POINT;
	}
	
	/** Create a new spot light */
	public Light(final Color color, final Vector3 position, final Vector3 direction, final float angle, final float power) {
		this(color.r, color.g, color.b, color.a, position.x, position.y, position.z, direction.x, direction.y, direction.z, angle, power);
	}
	
	/** Create a new spot light */
	public Light(float red, float green, float blue, float alpha, float x, float y, float z, float dirX, float dirY, float dirZ, float angle, float power) {
		this.color.set(red, green, blue, alpha);
		this.position.set(x, y, z);
		this.direction.set(dirX, dirY, dirZ);
		this.angle = angle;
		this.power = power;
		type = SPOT;
	}
	
	/** Set the values of this light to the values of the given light. */
	public void set(final Light other) {
		if (other == null)
			type = NONE;
		else {
			type = other.type;
			color.set(other.color);
			position.set(other.position);
			direction.set(other.direction);
			angle = other.angle;
			power = other.power;
		}
	}
	
	public boolean equals(Light other) {
		if (other == null) return false;
		if (other == this) return true;
		if (type != other.type)
			return false;
		if (!color.equals(other.color))
			return false;
		if ((type == POINT || type == SPOT) && (!position.equals(other.position) || power != other.power))
			return false;
		if ((type == DIRECTIONAL || type == SPOT) && !direction.equals(other.direction))
			return false;
		if (type == SPOT && angle != other.angle)
			return false;
		return true;
	}
	
	@Override
	public boolean equals (Object arg0) {
		if (arg0 == null) return false;
		if (arg0 == this) return true;
		if (!(arg0 instanceof Light)) return false;
		return equals((Light)arg0);
	}
}
