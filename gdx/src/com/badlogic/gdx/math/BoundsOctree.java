package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class BoundsOctree extends Octree<BoundingBox> {

    public BoundsOctree(Vector3 minimum, Vector3 maximum) {
        super(minimum, maximum, new Collider<BoundingBox>() {
            @Override public boolean intersects(BoundingBox aabb, BoundingBox geometry) {
                return aabb.intersects(geometry);
            }

            @Override public boolean intersects(Frustum frustum, BoundingBox geometry) {
                return frustum.boundsInFrustum(geometry);
            }

            Vector3 intersection = new Vector3();
            @Override public float intersects(Ray ray, BoundingBox geometry) {
                Intersector.intersectRayBounds(ray, geometry, intersection);
                return intersection.dst2(ray.origin);
            }
        });
    }
}
  
