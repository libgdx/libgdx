package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.math.Vector3;


public class PointParticle extends Particle {
	public float 	x, y, z,
						u, v, u2, v2,
						r, g, b, a,
						alphaStart, alphaDiff,
						scale, 
						scaleStart, scaleDiff,
						cosRotation, sinRotation;
	public void reset () {
		x = y = z = 0;
		u = v = 0;
		u2 = v2 = 1;
		r= g= b= a= 1;
		cosRotation =1; sinRotation =0;
		scale =1;
	}
}
