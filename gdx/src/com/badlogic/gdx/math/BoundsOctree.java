package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class BoundsOctree extends Octree<BoundingBox> {

    public BoundsOctree (Vector3 minimum, Vector3 maximum) {
        super(minimum, maximum, new Collider<BoundingBox>() {
            @Override public boolean intersects(BoundingBox aabb, BoundingBox geometry) {
                return aabb.contains(geometry);
            }

            final Vector3 tmp = new Vector3();
            @Override public float intersects(Ray ray, BoundingBox geometry) {
                if (!Intersector.intersectRayBounds(ray, geometry, tmp)) {
                    return tmp.dst2(ray.origin);
                }
                return Float.POSITIVE_INFINITY;
            }
        });
    }

}
  
