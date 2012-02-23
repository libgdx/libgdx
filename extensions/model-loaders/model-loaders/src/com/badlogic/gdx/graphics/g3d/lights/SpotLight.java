package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class SpotLight {

	public Vector3 position = new Vector3();
	public Color color = new Color();
	//or just
	// public float r,g,b;

	public float cutOffAngle;
		
	public float intensity;
}
