package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g3d.particles.values.VelocityDatas.VelocityData;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class Particle {
	public static float ROTATION_ACCUMULATOR = 0;
	public static Quaternion ROTATION_3D_ACCUMULATOR = new Quaternion();
	public int life, currentLife;
	public float lifePercent;
	public float cameraDistance;
	public VelocityData[] velocityData;
	public Vector3 velocity;
}
