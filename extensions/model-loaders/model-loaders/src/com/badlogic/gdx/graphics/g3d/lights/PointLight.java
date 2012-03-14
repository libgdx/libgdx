package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class PointLight implements Comparable {
	
	final public Vector3 position = new Vector3();
	final public Color color = new Color();
	// or just
	// public float x,y,z;
	// public float r,g,b;

	public float intensity;
	
	
	protected float priority;

	@Override
	public int compareTo(Object light) {
		return (((PointLight) light).priority < this.priority) ? -1 : 1;
	}

}
