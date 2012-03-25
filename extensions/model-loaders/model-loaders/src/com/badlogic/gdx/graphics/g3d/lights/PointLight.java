
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

	protected int priority;

	static final int PRIORITY_DISCRETE_STEPS = 256;

	@Override
	public int compareTo (Object other) {
		return this.priority - ((PointLight)other).priority;
	}

}
