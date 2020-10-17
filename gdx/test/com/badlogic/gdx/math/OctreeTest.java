package com.badlogic.gdx.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectSet;
import org.junit.Test;

public class OctreeTest {

    public static final float EPSILON = MathUtils.FLOAT_ROUNDING_ERROR;

    @Test
    public void testInsert() {
        Vector3 min = new Vector3(-5f,-5f,-5f);
        Vector3 max = new Vector3(5f,5f,5f);

        Octree octree = new Octree(min, max);

        BoundingBox box = new BoundingBox(new Vector3(0,0,0), new Vector3(1,1,1));
        octree.insert(box);

        ObjectSet<Object> result = new ObjectSet<Object>();
        octree.getAll(result);

        BoundingBox resultBox = (BoundingBox) result.iterator().next();
        assertEquals(box, resultBox);

        // Refill result geometries
        octree.remove(box);
        result.clear();
        octree.getAll(result);

        assertFalse(result.iterator().hasNext());
    }

}
