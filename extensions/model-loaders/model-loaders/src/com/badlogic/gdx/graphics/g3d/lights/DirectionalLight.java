package com.badlogic.gdx.graphics.g3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class DirectionalLight {
	
	public Vector3 direction = new Vector3();
	public Color color = new Color();
	//or just 
	// public float r,g,b;
	
	public float intensity;
}
