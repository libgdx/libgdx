package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class PointLight extends BaseLight {
	public final Vector3 position = new Vector3();
	public float intensity;
	
	public PointLight set(final PointLight copyFrom) {
		return set(copyFrom.color, copyFrom.position, copyFrom.intensity);
	}
	
	public PointLight set(final Color color, final Vector3 position, final float intensity) {
		if (color != null)
			this.color.set(color);
		if (position != null)
			this.position.set(position);
		this.intensity = intensity;
		return this;
	}
	
	public PointLight set(final float r, final float g, final float b, final Vector3 position, final float intensity) {
		this.color.set(r,g,b,1f);
		if (position != null)
			this.position.set(position);
		this.intensity = intensity;
		return this;
	}
	
	public PointLight set(final Color color, final float x, final float y, final float z, final float intensity) {
		if (color != null)
			this.color.set(color);
		this.position.set(x, y, z);
		this.intensity = intensity;
		return this;
	}
	
	public PointLight set(final float r, final float g, final float b, final float x, final float y, final float z, final float intensity) {
		this.color.set(r,g,b,1f);
		this.position.set(x, y, z);
		this.intensity = intensity;
		return this;
	}
	
	@Override
	public boolean equals (Object obj) {
		return (obj instanceof PointLight) ? equals((PointLight)obj) : false;
	}
	
	public boolean equals (PointLight other) {
		return (other != null && (other == this || (color.equals(other.color) && position.equals(other.position) && intensity == other.intensity)));
	}
}
