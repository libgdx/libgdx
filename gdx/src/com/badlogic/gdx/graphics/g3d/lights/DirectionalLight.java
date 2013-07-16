package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class DirectionalLight extends BaseLight {
	public final Vector3 direction = new Vector3();
	
	public DirectionalLight set(final DirectionalLight copyFrom) {
		return set(copyFrom.color, copyFrom.direction);
	}
	
	public DirectionalLight set(final Color color, final Vector3 direction) {
		if (color != null)
			this.color.set(color);
		if (direction != null)
			this.direction.set(direction).nor();
		return this;
	}
	
	public DirectionalLight set(final float r, final float g, final float b, final Vector3 direction) {
		this.color.set(r,g,b,1f);
		if (direction != null)
			this.direction.set(direction).nor();
		return this;
	}
	
	public DirectionalLight set(final Color color, final float dirX, final float dirY, final float dirZ) {
		if (color != null)
			this.color.set(color);
		this.direction.set(dirX, dirY, dirZ).nor();
		return this;
	}
	
	public DirectionalLight set(final float r, final float g, final float b, final float dirX, final float dirY, final float dirZ) {
		this.color.set(r,g,b,1f);
		this.direction.set(dirX, dirY, dirZ).nor();
		return this;
	}
	
	@Override
	public boolean equals (Object arg0) {
		return (arg0 instanceof DirectionalLight) ? equals((DirectionalLight)arg0) : false;
	}
	
	public boolean equals (final DirectionalLight other) {
		return (other != null) && ((other == this) || ((color.equals(other.color) && direction.equals(other.direction))));
	}
}
