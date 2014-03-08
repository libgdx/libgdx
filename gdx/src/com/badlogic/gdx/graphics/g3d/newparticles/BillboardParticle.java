package com.badlogic.gdx.graphics.g3d.newparticles;

import com.badlogic.gdx.graphics.g3d.newparticles.influencers.VelocityInfluencer;
import com.badlogic.gdx.graphics.g3d.newparticles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Vector3;

public class BillboardParticle extends Particle{
	public float 	x,y,z,
						u,v, u2,v2,
						halfWidth, halfHeight,
						
						//Color
						r = 1, g = 1, b = 1, a = 1,
						
						//Rotation
						cosRotation = 1, sinRotation = 0,
						
						//Scale
						scale = 1,
						
						//Start and Diff
						scaleStart, scaleDiff,
						alphaStart, alphaDiff;
	
	//Velocities applied to center of mass
	/** temporary  field used by velocity influencer to accumulate the rotation */
	public static float ROTATION_ACCUMULATOR = 0;
	public VelocityData[] velocityData;
	public Vector3 velocity;
}
