package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class Octree<T> {

    private final int maxItemsPerNode;

    protected OctreeNode root;
    private final Collider<T> collider;

    private static final Vector3 tmp = new Vector3();

    public Octree (Vector3 minimum, Vector3 maximum, int maxDepth, int maxItemsPerNode, Collider<T> collider) {
        super();
        Vector3 realMin = new Vector3(Math.min(minimum.x, maximum.x), Math.min(minimum.y, maximum.y), Math.min(minimum.z, maximum.z));
        Vector3 realMax = new Vector3(Math.max(minimum.x, maximum.x), Math.max(minimum.y, maximum.y), Math.max(minimum.z, maximum.z));

        this.root = new OctreeNode(realMin, realMax, maxDepth);
        this.collider = collider;
        this.maxItemsPerNode = maxItemsPerNode;
    }

    public void add (T object) {
        root.add(object, collider);
    }

    public void remove (T object) {
        root.remove(object);
    }

    /**
     * Method to retrieve all the geometries
     *
     * @param resultSet
     * @return the result set
     */
    public ObjectSet<T> getAll (ObjectSet<T> resultSet) {
        root.getAll(resultSet);
        return resultSet;
    }

    /**
     * Method to query geometries inside nodes that the aabb intersects
     * Can be used as broad phase
     * @param aabb - The bounding box to query
     * @param result - Set to be populated with objects inside the BoundingBoxes
     */
    public ObjectSet<T> query (BoundingBox aabb, ObjectSet<T> result) {
        root.query(aabb, result);
        return result;
    }

    /**
     * Method to query geometries inside nodes that the frustum intersects
     * Can be used as broad phase
     *
     * @param frustum - The frustum to query
     * @param result set populated with objects near from the frustum
     */
    public ObjectSet<T> query (Frustum frustum, ObjectSet<T> result) {
        root.query(frustum, result);
        return result;
    }

    public T rayCast (Ray ray, RayCastResult<T> result) {
        root.rayCast(ray, result);
        return result.geometry;
    }

    /**
     * Method to get nodes boxes. Useful for debug purpose
     * @param boxes
     */
    public ObjectSet<BoundingBox> getNodesBoxes (ObjectSet<BoundingBox> boxes) {
        root.getBoundingBox(boxes);
        return boxes;
    }

    protected class OctreeNode {

        protected final int level;

        // Represents the boundary of the node
        private BoundingBox bounds;

        private Array<OctreeNode> children;
        private ObjectSet<T> geometries = null;

        public OctreeNode (Vector3 min, Vector3 max, int level) {
            bounds = new BoundingBox(min, max);
            this.level = level;
        }

        private void split (Collider<T> collider) {
            children = new Array<>(8);

            float midx = (bounds.max.x + bounds.min.x) * 0.5f;
            float midy = (bounds.max.y + bounds.min.y) * 0.5f;
            float midz = (bounds.max.z + bounds.min.z) * 0.5f;

            int deeperLevel = level - 1;

            children.add(new OctreeNode(new Vector3(bounds.min.x, midy, midz), new Vector3(midx, bounds.max.y, bounds.max.z), deeperLevel));
            children.add(new OctreeNode(new Vector3(midx, midy, midz), new Vector3(bounds.max.x, bounds.max.y, bounds.max.z), deeperLevel));
            children.add(new OctreeNode(new Vector3(midx, midy, bounds.min.z), new Vector3(bounds.max.x, bounds.max.y, midz), deeperLevel));
            children.add(new OctreeNode(new Vector3(bounds.min.x, midy, bounds.min.z), new Vector3(midx, bounds.max.y, midz), deeperLevel));
            children.add(new OctreeNode(new Vector3(bounds.min.x, bounds.min.y, midz), new Vector3(midx, midy, bounds.max.z), deeperLevel));
            children.add(new OctreeNode(new Vector3(midx, bounds.min.y, midz), new Vector3(bounds.max.x, midy, bounds.max.z), deeperLevel));
            children.add(new OctreeNode(new Vector3(midx, bounds.min.y, bounds.min.z), new Vector3(bounds.max.x, midy, midz), deeperLevel));
            children.add(new OctreeNode(new Vector3(bounds.min.x, bounds.min.y, bounds.min.z), new Vector3(midx, midy, midz), deeperLevel));

            ObjectSet<T> geometries = this.geometries;
            this.geometries = null;
            // Move geometries from parent to children
            for (OctreeNode child : children) {
                for (T geometry : geometries) {
                    child.add(geometry, collider);
                }
            }
        }

        protected boolean contains (Collider<T> boundingBoxHandler, T geometry) {
            return boundingBoxHandler.intersects(bounds, geometry);
        }

        protected void add (T geometry, Collider<T> collider) {
            // If is not leaf, check children
            if (!isLeaf()) {
                for (OctreeNode child : children) {
                    child.add(geometry, collider);
                }
                return;
            }

            if (!collider.intersects(bounds, geometry)) {
                return;
            }

            addGeometry(geometry);
            if (geometries.size > maxItemsPerNode && level > 0) {
                split(collider);
            }
        }

        protected void remove (T aabb) {
            if (isLeaf()) {
                if (geometries != null) {
                    geometries.remove(aabb);
                }
            } else {
                for (OctreeNode node : children) {
                    node.remove(aabb);
                }
            }
        }

        protected boolean isLeaf () {
            return children == null;
        }

        protected void addGeometry (T geometry) {
            if (geometries == null) {
                geometries = new ObjectSet<>();
            }
            geometries.add(geometry);
        }

        protected ObjectSet<T> query (BoundingBox aabb, ObjectSet<T> result) {
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.query(aabb, result);
                }
            }
            if (aabb.contains(bounds)) {
                if (geometries != null) {
                    result.addAll(geometries);
                }
            }
            return result;
        }

        protected ObjectSet<T> query (Frustum frustum, ObjectSet<T> result) {
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.query(frustum, result);
                }
            }
            if (frustum.boundsInFrustum(bounds)) {
                if (geometries != null) {
                    result.addAll(geometries);
                }
            }
            return result;
        }

        protected void rayCast (Ray ray, RayCastResult<T> result) {
            // Check intersection with node
            boolean intersect = Intersector.intersectRayBounds(ray, bounds, tmp);
            if (!intersect) {
                return;
            } else {
                float dst2 = tmp.dst2(ray.origin);
                if (dst2 >= result.distance) {
                    return;
                }
            }

            // Check intersection with children
            if (!isLeaf()) {
                for (OctreeNode child : children) {
                    child.rayCast(ray, result);
                }
            } else {
                // Check intersection with geometries
                if (geometries != null) {
                    for (T geometry: geometries) {
                        float distance = collider.intersects(ray, geometry);
                        if (result.geometry == null || distance < result.distance) {
                            result.geometry = geometry;
                            result.distance = distance;
                        }
                    }
                }
            }
        }

        /**
         * Get all geometries using Depth-First Search recursion
         * @param resultSet
         */
        protected void getAll (ObjectSet<T> resultSet) {
            if (!isLeaf()) {
                for (OctreeNode child : children) {
                    child.getAll(resultSet);
                }
            }
            if (geometries != null) {
                resultSet.addAll(geometries);
            }
        }

        /**
         * Get bounding boxes using Depth-First Search recursion
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

    public interface Collider<T> {
        /**
         * Method to calculate intersection between aabb and the geometry
         *
         * @param nodeBounds
         * @param geometry
         * @return if they are intersecting
         */
        boolean intersects (BoundingBox nodeBounds, T geometry);

        /**
         * Method to calculate intersection between ray and the geometry
         *
         * @param ray
         * @param geometry
         * @return distance between ray and geometry
         */
        float intersects (Ray ray, T geometry);
    }

    public static class RayCastResult<T> {
        T geometry;
        float distance;
        float maxDistance = Float.MAX_VALUE;
    }
}
