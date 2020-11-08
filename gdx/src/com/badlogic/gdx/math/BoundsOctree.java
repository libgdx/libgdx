package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.ObjectSet;

public class BoundsOctree extends Octree<BoundingBox> {

    public BoundsOctree (Vector3 minimum, Vector3 maximum) {
        super(minimum, maximum);
    }

    private static final BoundingBoxHandler<BoundingBox> BOUNDING_BOX_HANDLER = new BoundingBoxHandler<BoundingBox>() {
        @Override public boolean intersects (BoundingBox aabb, BoundingBox geometry) {
            return aabb.intersects(geometry);
        }
    };

    public void add (BoundingBox aabb) {
        super.add(aabb, BOUNDING_BOX_HANDLER);
    }

    public ObjectSet<BoundingBox> query (BoundingBox aabb, ObjectSet<BoundingBox> result) {
        return super.query(aabb, result, BOUNDING_BOX_HANDLER);
    }

    public ObjectSet<BoundingBox> query (final Frustum frustum, ObjectSet<BoundingBox> result) {
        return super.query(frustum, result, new FrustrumHandler<BoundingBox>() {
            @Override public boolean intersects (Frustum frustum, BoundingBox geometry) {
                return frustum.boundsInFrustum(geometry);
            }
        });
    }

    public RayCastResult<BoundingBox> rayCast (Ray ray) {
        return super.rayCast(ray, new RayCastHandler<BoundingBox>() {
            final Vector3 tmp = new Vector3();
            @Override public Vector3 intersects (Ray ray, BoundingBox geometry) {
                if (!Intersector.intersectRayBounds(ray, geometry, tmp)) {
                    return NOT_INTERSECTING;
                }
                return tmp;
            }
        });
    }
}
  
