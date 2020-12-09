/*******************************************************************************
 * Copyright 2020 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

/**
 * A static Octree implementation.
 *
 * Example of usage:
 *
 * <pre>
 * Vector3 min = new Vector3(-10, -10, -10);
 * Vector3 max = new Vector3(10, 10, 10);
 * octree = new Octree<GameObject>(min, max, MAX_DEPTH, MAX_ITEMS_PER_NODE, new Octree.Collider<GameObject>() {
 * 	 @Override
 *   public boolean intersects(BoundingBox nodeBounds, GameObject geometry) {
 *     return nodeBounds.intersects(geometry.box);
 *   }
 *
 *   @Override
 *   public boolean intersects(Frustum frustum, GameObject geometry) {
 *     return frustum.boundsInFrustum(geometry.box);
 *   }
 *
 *   @Override
 *   public float intersects(Ray ray, GameObject geometry) {
 *     if (Intersector.intersectRayBounds(ray, geometry.box, new Vector3())) {
 *       return tmp.dst2(ray.origin);
 *     }
 *     return Float.MAX_VALUE;
 *   }
 * });
 *
 * // Adding game objects to the octree
 * octree.add(gameObject1);
 * octree.add(gameObject2);
 *
 * // Querying the result
 * ObjectSet<GameObject> result = new ObjectSet<>();
 * octree.query(cam.frustum, result);
 *
 * // Rendering the result
 * for (GameObject gameObject : result) {
 *   modelBatch.render(gameObject);
 * }
 * </pre>
 */
public class Octree<T> {

	private final int maxItemsPerNode;

	private final Pool<OctreeNode> nodePool = new Pool<OctreeNode>() {
		@Override
		protected OctreeNode newObject() {
			return new OctreeNode();
		}
	};

	protected OctreeNode root;
	private final Collider<T> collider;

	private static final Vector3 tmp = new Vector3();

	public Octree (Vector3 minimum, Vector3 maximum, int maxDepth, int maxItemsPerNode, Collider<T> collider) {
		super();
		Vector3 realMin = new Vector3(Math.min(minimum.x, maximum.x), Math.min(minimum.y, maximum.y), Math.min(minimum.z, maximum.z));
		Vector3 realMax = new Vector3(Math.max(minimum.x, maximum.x), Math.max(minimum.y, maximum.y), Math.max(minimum.z, maximum.z));

		this.root = nodePool.obtain();
		this.root.bounds.set(realMin, realMax);
		this.root.level = maxDepth;
		this.collider = collider;
		this.maxItemsPerNode = maxItemsPerNode;
	}

	public void add (T object) {
		root.add(object);
	}

	public void remove (T object) {
		root.remove(object);
	}

	public void update (T object) {
		root.remove(object);
		root.add(object);
	}

	/** Method to retrieve all the geometries.
	 * @param resultSet
	 * @return the result set
	 */
	public ObjectSet<T> getAll (ObjectSet<T> resultSet) {
		root.getAll(resultSet);
		return resultSet;
	}

	/** Method to query geometries inside nodes that the aabb intersects.
	 * Can be used as broad phase.
	 * @param aabb - The bounding box to query
	 * @param result - Set to be populated with objects inside the BoundingBoxes
	 */
	public ObjectSet<T> query (BoundingBox aabb, ObjectSet<T> result) {
		root.query(aabb, result);
		return result;
	}

	/** Method to query geometries inside nodes that the frustum intersects.
	 * Can be used as broad phase.
	 * @param frustum - The frustum to query
	 * @param result set populated with objects near from the frustum
	 */
	public ObjectSet<T> query (Frustum frustum, ObjectSet<T> result) {
		root.query(frustum, result);
		return result;
	}

	public T rayCast (Ray ray, RayCastResult<T> result) {
		result.distance = result.maxDistanceSq;
		root.rayCast(ray, result);
		return result.geometry;
	}

	/** Method to get nodes as bounding boxes. Useful for debug purpose.
	 *
	 * @param boxes
	 */
	public ObjectSet<BoundingBox> getNodesBoxes (ObjectSet<BoundingBox> boxes) {
		root.getBoundingBox(boxes);
		return boxes;
	}

	protected class OctreeNode {

		private int level;

		// Represents the boundary of the node
		private final BoundingBox bounds;
		private final Array<OctreeNode> children = new Array<>(8);
		private final Array<T> geometries = new Array<T>(maxItemsPerNode);

		public OctreeNode () {
			bounds = new BoundingBox();
		}

		private void split () {
			float midx = (bounds.max.x + bounds.min.x) * 0.5f;
			float midy = (bounds.max.y + bounds.min.y) * 0.5f;
			float midz = (bounds.max.z + bounds.min.z) * 0.5f;

			int deeperLevel = level - 1;

			children.add(createNode(new Vector3(bounds.min.x, midy, midz), new Vector3(midx, bounds.max.y, bounds.max.z), deeperLevel));
			children.add(createNode(new Vector3(midx, midy, midz), new Vector3(bounds.max.x, bounds.max.y, bounds.max.z), deeperLevel));
			children.add(createNode(new Vector3(midx, midy, bounds.min.z), new Vector3(bounds.max.x, bounds.max.y, midz), deeperLevel));
			children.add(createNode(new Vector3(bounds.min.x, midy, bounds.min.z), new Vector3(midx, bounds.max.y, midz), deeperLevel));
			children.add(createNode(new Vector3(bounds.min.x, bounds.min.y, midz), new Vector3(midx, midy, bounds.max.z), deeperLevel));
			children.add(createNode(new Vector3(midx, bounds.min.y, midz), new Vector3(bounds.max.x, midy, bounds.max.z), deeperLevel));
			children.add(createNode(new Vector3(midx, bounds.min.y, bounds.min.z), new Vector3(bounds.max.x, midy, midz), deeperLevel));
			children.add(createNode(new Vector3(bounds.min.x, bounds.min.y, bounds.min.z), new Vector3(midx, midy, midz), deeperLevel));

			// Move geometries from parent to children
			for (OctreeNode child : children) {
				for (T geometry : this.geometries) {
					child.add(geometry);
				}
			}
			this.geometries.clear();
		}

		private void merge () {
			for (OctreeNode node : children) {
				geometries.addAll(node.geometries);
				freeNode(node);
			}
			children.clear();
		}

		private OctreeNode createNode (Vector3 min, Vector3 max, int level) {
			OctreeNode node = nodePool.obtain();
			node.bounds.set(min, max);
			node.level = level;
			return node;
		}   

		private void freeNode (OctreeNode node) {
			node.geometries.clear();
			node.children.clear();
			nodePool.free(node);
		}

		protected void add (T geometry) {
			if (!collider.intersects(bounds, geometry)) {
				return;
			}

			// If is not leaf, check children
			if (!isLeaf()) {
				for (OctreeNode child : children) {
					child.add(geometry);
				}
			} else {
				geometries.add(geometry);
				if (geometries.size > maxItemsPerNode && level > 0) {
					split();
				}
			}
		}

		protected boolean remove (T object) {
			if (!isLeaf()) {
				boolean removed = false;
				int childrenSum = 0;
				for (OctreeNode node : children) {
					removed |= node.remove(object);
					childrenSum += node.geometries.size;
				}

				if (removed) {
					if (childrenSum <= maxItemsPerNode) {
						merge();
					}
				}

				return removed;
			} else {
				return geometries.removeValue(object, true);
			}
		}

		protected boolean isLeaf () {
			return children.isEmpty();
		}

		protected void query (BoundingBox aabb, ObjectSet<T> result) {
			if (!aabb.intersects(bounds)) {
				return;
			}

			if (!isLeaf()) {
				for (OctreeNode node : children) {
					node.query(aabb, result);
				}
			} else {
				for (T geometry : geometries) {
					// Filter geometries using collider
					if (collider.intersects(bounds, geometry)) {
						result.add(geometry);
					}
				}
			}
		}

		protected void query (Frustum frustum, ObjectSet<T> result) {
			if (!Intersector.intersectFrustumBounds(frustum, bounds)) {
				return;
			}
			if (!isLeaf()) {
				for (OctreeNode node : children) {
					node.query(frustum, result);
				}
			} else {
				for (T geometry : geometries) {
					// Filter geometries using collider
					if (collider.intersects(frustum, geometry)) {
						result.add(geometry);
					}
				}
			}
		}

		protected void rayCast (Ray ray, RayCastResult<T> result) {
			// Check intersection with node
			boolean intersect = Intersector.intersectRayBounds(ray, bounds, tmp);
			if (!intersect) {
				return;
			} else {
				float dst2 = tmp.dst2(ray.origin);
				if (dst2 >= result.maxDistanceSq) {
					return;
				}
			}

			// Check intersection with children
			if (!isLeaf()) {
				for (OctreeNode child : children) {
					child.rayCast(ray, result);
				}
			} else {
				for (T geometry: geometries) {
					// Check intersection with geometries
					float distance = collider.intersects(ray, geometry);
					if (result.geometry == null || distance < result.distance) {
						result.geometry = geometry;
						result.distance = distance;
					}
				}
			}
		}

		/** Get all geometries using Depth-First Search recursion.
		 * @param resultSet
		 */
		protected void getAll (ObjectSet<T> resultSet) {
			if (!isLeaf()) {
				for (OctreeNode child : children) {
					child.getAll(resultSet);
				}
			}
			resultSet.addAll(geometries);
		}

		/** Get bounding boxes using Depth-First Search recursion.
		 * @param bounds
		 */
		protected void getBoundingBox(ObjectSet<BoundingBox> bounds) {
			if (!isLeaf()) {
				for (OctreeNode node : children) {
					node.getBoundingBox(bounds);
				}
			}
			bounds.add(this.bounds);
		}
	}

	/** Interface used by octree to handle geometries' collisions
	 * against BoundingBox, Frustum and Ray.
	 * @param <T>
	 */
	public interface Collider<T> {

		/** Method to calculate intersection between aabb and the geometry.
		 * @param nodeBounds
		 * @param geometry
		 * @return if they are intersecting
		 */
		boolean intersects (BoundingBox nodeBounds, T geometry);

		/** Method to calculate intersection between frustum and the geometry.
		 * @param frustum
		 * @param geometry
		 * @return if they are intersecting
		 */
		boolean intersects (Frustum frustum, T geometry);

		/** Method to calculate intersection between ray and the geometry.
		 * @param ray
		 * @param geometry
		 * @return distance between ray and geometry
		 */
		float intersects (Ray ray, T geometry);
	}

	public static class RayCastResult<T> {
		T geometry;
		float distance;
		float maxDistanceSq = Float.MAX_VALUE;
	}
}
