package com.badlogic.gdx.graphics.g3d.newparticles;

import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Vector3;

public class PointParticle extends Particle {
	public float 	x, y, z,
						r = 1, g = 1, b = 1, a = 1,
						alphaStart, alphaDiff,
						scale = 1, 
						scaleStart, scaleDiff,
						cosRotation = 1, sinRotation = 0,
						u = 0, v = 0;
	
	public static float ROTATION_ACCUMULATOR = 0;
	public VelocityData[] velocityData;
	public Vector3 velocity;
}
