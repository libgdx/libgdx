package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class SpotLight {

	final public Vector3 position = new Vector3();
	final public Vector3 direction = new Vector3();
	final public Color color = new Color();
	//or just
	// public float r,g,b;

	public float cutOffAngle;
		
	public float intesity;
}
