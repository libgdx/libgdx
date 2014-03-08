package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ModelInstanceParticle extends Particle {
	public ModelInstance instance;
	//Color
	//Pointer to instance color attribute for fast access
	//This should be an array though, because an instance can
	//be composed by many parts each one with or without the color diffuse attribute
	public Color color;
	public float alphaStart, alphaDiff;
	
	//Scale
	public float scale = 1, scaleStart, scaleDiff;
	
	//Rotation
	public Quaternion rotation = new Quaternion();
		
	//Velocities applied to center of mass
	/** temporary  field used by velocity influencer to accumulate the rotation */
	public static Quaternion ROTATION_ACCUMULATOR = new Quaternion();
	public VelocityData[] velocityData;
	public Vector3 velocity;
}
