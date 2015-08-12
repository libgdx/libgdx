package com.badlogic.gdx.graphics.g3d.shadow.directional;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * @FIXME NOT WORKING, DO NOT USE
 * @author realitix
 * @see "http://gamedev.stackexchange.com/questions/81734/how-to-calculate-directional-light-frustum-from-camera-frustum"
 */
public class FrustumDirectionalAnalyzer implements DirectionalAnalyzer {

	private DirectionalResult result = new DirectionalResult();
	private Vector3 vz = new Vector3();
	private Vector3 vx = new Vector3();
	private Vector3 vy = new Vector3();

	private Vector2 dimz = new Vector2();
	private Vector2 dimx = new Vector2();
	private Vector2 dimy = new Vector2();

	/**
	 * @FIXME NOT WORKING
	 */
	@Override
	public DirectionalResult analyze (Frustum frustum, Vector3 direction) {
		vz.set(direction);
		vx.set(vz.y, vz.z, vz.x);
		vy.set(vz).crs(vx);

		dimx.set(9999999, -9999999);
		dimy.set(9999999, -9999999);
		dimz.set(9999999, -9999999);

		int i = 0;
		float d;

		for(i = 0; i < frustum.planePoints.length; i++) {
			// z
			d = frustum.planePoints[i].dot(vz);
			if( d < dimz.x ) dimz.x = d;
			if( d > dimz.y ) dimz.y = d;

			// x
			d = frustum.planePoints[i].dot(vx);
			if( d < dimx.x ) dimx.x = d;
			if( d > dimx.y ) dimx.y = d;

			// y
			d = frustum.planePoints[i].dot(vy);
			if( d < dimy.x ) dimy.x = d;
			if( d > dimy.y ) dimy.y = d;
		}

		return null;
	}
}
