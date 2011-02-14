package com.badlogic.gdx.graphics.tmp;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public class Frustum {
	protected static final Vector3[] clipSpacePlanePoints = { new Vector3(-1, -1, 1), new Vector3(1, -1, 1), new Vector3(1, 1, 1), new Vector3(-1, 1, 1), // near clip
		   												   new Vector3(-1, -1, -1), new Vector3(1, -1, -1), new Vector3(1, 1, -1), new Vector3(-1, 1, -1) }; // far clip
	
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
}
