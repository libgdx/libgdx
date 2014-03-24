package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/** @author Inferno */
public class ModelInstanceParticle extends Particle {
	public ModelInstance instance;
	//Color
	//Pointer to instance color attribute for fast access
	//This should be an array though, because an instance can
	//be composed by many parts each one with or without the color diffuse attribute
	public Color color;
	public BlendingAttribute blending;
	public float alphaStart, alphaDiff;
	
	//Scale
	public float scale, scaleStart, scaleDiff;
	
	//Rotation
	public Quaternion rotation = new Quaternion();
	public void reset () {
		scale = 1;
		rotation.idt();
		instance = null;
	}
}
