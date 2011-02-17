package com.badlogic.gdx.graphics.tmp;

import java.util.Arrays;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class Frustum {
	protected static final Vector3[] clipSpacePlanePoints = { new Vector3(-1, -1, -1), new Vector3(1, -1, -1), new Vector3(1, 1, -1), new Vector3(-1, 1, -1), // near clip
		   												   new Vector3(-1, -1, 1), new Vector3(1, -1, 1), new Vector3(1, 1, 1), new Vector3(-1, 1, 1) }; // far clip
	
	/** the six clipping planes, near, far, left, right, top, bottm **/
	public final Plane[] planes = new Plane[6];	
	
	/** eight points making up the near and far clipping "rectangles". order is counter clockwise, starting at bottom left **/
	protected final Vector3[] planePoints = { new Vector3(), new Vector3(), new Vector3(), new Vector3(), 
			new Vector3(), new Vector3(), new Vector3(), new Vector3() 
	};	
		
	public Frustum() {
		for(int i = 0; i < 6; i++) {
			planes[i] = new Plane(new Vector3(), 0);
		}
	}
	
	final Matrix4 invProjectionView = new Matrix4();
	/**
	 * Updates the clipping plane's based on the given combined projection and view
	 * matrix, e.g. from an {@link OrthographicCamera} or {@link PerspectiveCamera}.
	 * @param projectionView the combined projection and view matrices.
	 */
	public void update(Matrix4 projectionView) {
		invProjectionView.set(projectionView);
		invProjectionView.inv();
		
		for(int i = 0; i < 8; i++) {
			Vector3 point = planePoints[i].set(clipSpacePlanePoints[i]);
			point.prj(invProjectionView);			
		}
		
		planes[0].set(planePoints[1], planePoints[0], planePoints[2]);
		planes[1].set(planePoints[4], planePoints[5], planePoints[7]);
		planes[2].set(planePoints[0], planePoints[4], planePoints[3]);
		planes[3].set(planePoints[5], planePoints[1], planePoints[6]);
		planes[4].set(planePoints[2], planePoints[3], planePoints[6]);
		planes[5].set(planePoints[4], planePoints[0], planePoints[1]);
	}	
	
	/**
	 * Returns wheter the point is in the frustum.
	 * 
	 * @param point The point
	 * @return Wheter the point is in the frustum.
	 */
	public boolean pointInFrustum (Vector3 point) {
		for (int i = 0; i < planes.length; i++) {
			PlaneSide result = planes[i].testPoint(point);
			if (result == PlaneSide.Back) return false;
		}

		return true;
	}

	/**
	 * Returns wheter the given sphere is in the frustum.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum
	 */
	public boolean sphereInFrustum (Vector3 center, float radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	/**
	 * Returns wheter the given sphere is in the frustum not checking wheter it is behind the near and far clipping plane.
	 * 
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @return Wheter the sphere is in the frustum
	 */
	public boolean sphereInFrustumWithoutNearFar (Vector3 center, float radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	/**
	 * Returns wheter the given {@link BoundingBox} is in the frustum.
	 * 
	 * @param bounds The bounding box
	 * @return Wheter the bounding box is in the frustum
	 */
	public boolean boundsInFrustum (BoundingBox bounds) {
		Vector3[] corners = bounds.getCorners();
		int len = corners.length;

		for (int i = 0, len2 = planes.length; i < len2; i++) {
			int out = 0;

			for (int j = 0; j < len; j++)
				if (planes[i].testPoint(corners[j]) == PlaneSide.Back) out++;

			if (out == 8) return false;
		}

		return true;
	}
	
//	/**
//	 * Calculates the pick ray for the given window coordinates. Assumes the window coordinate system has it's y downwards. The
//	 * returned Ray is a member of this instance so don't reuse it outside this class.
//	 * 
//	 * @param screen_width The window width in pixels
//	 * @param screen_height The window height in pixels
//	 * @param mouse_x The window x-coordinate
//	 * @param mouse_y The window y-coordinate
//	 * @param pos The camera position
//	 * @param dir The camera direction, having unit length
//	 * @param up The camera up vector, having unit length
//	 * @return the picking ray.
//	 */
//	public Ray calculatePickRay (float screen_width, float screen_height, float mouse_x, float mouse_y, Vector3 pos, Vector3 dir,
//		Vector3 up) {
//		float n_x = mouse_x - screen_width / 2.0f;
//		float n_y = mouse_y - screen_height / 2.0f;
//		n_x /= screen_width / 2.0f;
//		n_y /= screen_height / 2.0f;
//
//		Z.set(dir.tmp().mul(-1)).nor();
//		X.set(up.tmp().crs(Z)).nor();
//		Y.set(Z.tmp().crs(X)).nor();
//		near_center.set(pos.tmp3().sub(Z.tmp2().mul(near)));
//		Vector3 near_point = X.tmp3().mul(near_width).mul(n_x).add(Y.tmp2().mul(near_height).mul(n_y));
//		near_point.add(near_center);
//
//		return ray.set(near_point.tmp(), near_point.sub(pos).nor());
//	}
	
	public static void main(String[] argv) {
		PerspectiveCamera camera = new PerspectiveCamera(45, 2, 2);
//		camera.rotate(90, 0, 1, 0);
		camera.update();
		System.out.println(camera.direction);
		System.out.println(Arrays.toString(camera.frustum.planes));
		
		OrthographicCamera camOrtho = new OrthographicCamera(2, 2);
		camOrtho.near = 1;
//		camOrtho.rotate(90, 1, 0, 0);
		camOrtho.update();
		System.out.println(camOrtho.direction);
		System.out.println(Arrays.toString(camOrtho.frustum.planes));
	}
}
