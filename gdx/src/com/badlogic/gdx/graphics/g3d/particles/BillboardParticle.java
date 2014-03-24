package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.math.Vector3;


/** @author Inferno */
public class BillboardParticle extends Particle{
	public float 	x,y,z,
						u, v, u2, v2,
						//Size in world units
						halfWidth, halfHeight,
						
						//Color
						r, g, b, a,
						
						//Rotation
						cosRotation, sinRotation,
						
						//Scale
						scale,
						
						//Start and Diff
						scaleStart, scaleDiff,
						alphaStart, alphaDiff;
	public void reset () {
		x = y = z = 0;
		u=0; v=0; u2 =1; v2 = 1;
		halfWidth = halfHeight = 0.5f;
		r= g= b= a= 1;
		cosRotation =1; sinRotation =0;
		scale =1;
	}
}
