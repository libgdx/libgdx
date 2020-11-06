package com.badlogic.gdx.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectSet;
import org.junit.Test;

public class OctreeTest {

    public static final float EPSILON = MathUtils.FLOAT_ROUNDING_ERROR;

    @Test
    public void testInsert() {
        Vector3 min = new Vector3(-5f,-5f,-5f);
        Vector3 max = new Vector3(5f,5f,5f);

        Octree octree = new Octree(min, max).setMaxDepth(2).setMaxItemsPerNode(1);
        assertTrue(octree.root.isLeaf());

        BoundingBox box = new BoundingBox(new Vector3(0,0,0), new Vector3(1,1,1));
        octree.insert(box);

        octree.insert(new BoundingBox(new Vector3(2,2,2), new Vector3(3,3,3)));
        assertFalse(octree.root.isLeaf());

        ObjectSet<BoundingBox> result = new ObjectSet<BoundingBox>();
        octree.getAll(result);
        assertEquals(2, result.size);

        BoundingBox resultBox = (BoundingBox) result.iterator().next();
        assertEquals(box, resultBox);

        // Refill result geometries
        octree.remove(box);
        result.clear();
        octree.getAll(result);
        assertEquals(1, result.size);

        assertTrue(result.iterator().hasNext());
    }

}
