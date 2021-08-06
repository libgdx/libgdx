package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlaneTest {

    @Test
    public void testIntersectBoundingBoxPlane () {
        // Cube (1x1x1 at origin)
        BoundingBox box = new BoundingBox(
                new Vector3(-0.5f, -0.5f, -0.5f),
                new Vector3(0.5f, 0.5f, 0.5f));
        // Plane XY pointing to +Z
        Plane plane = new Plane(Vector3.Z, 0);

        // Plane and box intersect
        assertEquals(Plane.PlaneSide.OnPlane, plane.testBounds(box));

        // Move box 0.5 units to +Z (test if intersection is inclusive)
        box.mul(new Matrix4().setTranslation(0, 0, 0.5f));
        assertEquals(Plane.PlaneSide.OnPlane, plane.testBounds(box));

        // Move box 1 unit to +Z (at the front of the plane)
        box.mul(new Matrix4().setTranslation(0, 0, 1));
        assertEquals(Plane.PlaneSide.Front, plane.testBounds(box));
        assertEquals(Plane.PlaneSide.Front, plane.testPoint(box.min));
        assertEquals(Plane.PlaneSide.Front, plane.testPoint(box.max));
        assertFalse(Intersector.intersectBoundsPlaneFast(box, plane));

        // Move box 3.1 units to -Z (at the back of the plane)
        box.mul(new Matrix4().setTranslation(0, 0, -3.1f));
        assertEquals(Plane.PlaneSide.Back, plane.testBounds(box));
        assertEquals(Plane.PlaneSide.Back, plane.testPoint(box.min));
        assertEquals(Plane.PlaneSide.Back, plane.testPoint(box.max));
        assertFalse(Intersector.intersectBoundsPlaneFast(box, plane));
    }

}
