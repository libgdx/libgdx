package com.badlogic.gdx.graphics.g3d.newparticles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Triangle{
	float x1, y1, z1,
			x2, y2, z2,
			x3, y3, z3;
	public Triangle(	float x1, float y1, float z1, 
							float x2, float y2, float z2, 
							float x3, float y3, float z3){
		this.x1 = x1; this.y1 = y1; this.z1 = z1;
		this.x2 = x2; this.y2 = y2; this.z2 = z2;
		this.x3 = x3; this.y3 = y3; this.z3 = z3;
	}
	
	public static Vector3 pick(float x1, float y1, float z1, 
		float x2, float y2, float z2, 
		float x3, float y3, float z3, Vector3 vector){
		float a = MathUtils.random(), b = MathUtils.random();
		return vector.set( 	x1 + a*(x2 - x1) + b*(x3 - x1),
									y1 + a*(y2 - y1) + b*(y3 - y1),
									z1 + a*(z2 - z1) + b*(z3 - z1));
	}
	
	public Vector3 pick(Vector3 vector){
		float a = MathUtils.random(), b = MathUtils.random();
		return vector.set( 	x1 + a*(x2 - x1) + b*(x3 - x1),
									y1 + a*(y2 - y1) + b*(y3 - y1),
									z1 + a*(z2 - z1) + b*(z3 - z1));
	}
	
}