package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;


public class ParticleControllerParticle extends Particle{
	public ParticleController controller;
	
	//Position
	public float x, y, z;
	
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
