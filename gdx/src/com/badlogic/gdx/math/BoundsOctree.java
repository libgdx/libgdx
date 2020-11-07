package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;

public class BoundsOctree extends Octree<BoundingBox> {

    public BoundsOctree(Vector3 minimum, Vector3 maximum) {
        super(minimum, maximum, new Collider<BoundingBox>() {
            @Override public boolean contains(BoundingBox nodeBounds, BoundingBox geometry) {
                return nodeBounds.intersects(geometry);
            }
        });
    }
}
  
