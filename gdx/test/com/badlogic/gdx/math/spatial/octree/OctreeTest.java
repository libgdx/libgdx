package com.badlogic.gdx.math.spatial.octree;

import static org.junit.Assert.assertEquals;

import com.badlogic.gdx.math.Vector3;
import java.util.Set;
import org.junit.Test;

public class OctreeTest {

    @Test
    public void testInit() {
        Octree octree = new Octree();
        OctreeNode root = octree.getRoot();
        isEqual(0f, 0, 0, root.getMin());
        isEqual(0f, 0, 0, root.getMax());
    }

    @Test
    public void testResize() {
        Octree octree = new Octree();
        octree.insert(new Object(), new Vector3(2, 0, 0));
        octree.insert(new Object(), new Vector3(-1, -1, -1));
        octree.insert(new Object(), new Vector3(0, 1, 0));

        OctreeNode root = octree.getRoot();
        isEqual(-1f, -1f, -1f, root.getMin());
        isEqual(2f, 1, 0, root.getMax());

        Set<Object> geometries = octree.getAll();
        assertEquals(3, geometries.size());
    }

    private void isEqual(float x, float y, float z, Vector3 v) {
        assertEquals(x, v.x, 0f);
        assertEquals(y, v.y, 0f);
        assertEquals(z, v.z, 0f);
    }


}
