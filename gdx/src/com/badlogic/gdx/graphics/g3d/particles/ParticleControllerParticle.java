package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.math.Quaternion;


/** @author Inferno */
public class ParticleControllerParticle extends Particle{
	public ParticleController controller;

	//Scale
	public float scale, scaleStart, scaleDiff;
	
	//Rotation
	public Quaternion rotation = new Quaternion();
	
	public void reset(){
		scale = 1;
		rotation.idt();
	}
}
