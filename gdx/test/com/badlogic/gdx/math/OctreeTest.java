
package com.badlogic.gdx.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.ObjectSet;
import org.junit.Test;

public class OctreeTest {

	@Test
	public void testInsert () {
		int maxDepth = 2;
		int maxItemsPerNode = 1;

		Vector3 min = new Vector3(-5f, -5f, -5f);
		Vector3 max = new Vector3(5f, 5f, 5f);

		Octree<BoundingBox> octree = new Octree<>(min, max, maxDepth, maxItemsPerNode, new Octree.Collider<BoundingBox>() {
			@Override
			public boolean intersects (BoundingBox nodeBounds, BoundingBox geometry) {
				return nodeBounds.intersects(geometry);
			}

			@Override
			public boolean intersects (Frustum frustum, BoundingBox geometry) {
				return false;
			}

			final Vector3 tmp = new Vector3();

			@Override
			public float intersects (Ray ray, BoundingBox geometry) {
				if (!Intersector.intersectRayBounds(ray, geometry, tmp)) {
					return tmp.dst2(ray.origin);
				}
				return Float.POSITIVE_INFINITY;
			}
		});

		assertTrue(octree.root.isLeaf());

		BoundingBox box1 = new BoundingBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1));
		octree.add(box1);

		BoundingBox box2 = new BoundingBox(new Vector3(2, 2, 2), new Vector3(3, 3, 3));
		octree.add(box2);
		assertFalse(octree.root.isLeaf());

		ObjectSet<BoundingBox> result = new ObjectSet<BoundingBox>();
		octree.getAll(result);
		assertTrue(result.contains(box1));
		assertTrue(result.contains(box2));
		assertEquals(2, result.size);

		octree.remove(box2);
		result.clear();
		// Refill result geometries
		octree.getAll(result);
		assertEquals(1, result.size);
		assertTrue(result.contains(box1));
	}

}
