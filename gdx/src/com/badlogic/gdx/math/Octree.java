package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class Octree<T> {

    private int maxDepth = 8;
    private int maxItemsPerNode = 32;

    protected OctreeNode root;

    private final Collider<T> collider;

    public Octree (Vector3 minimum, Vector3 maximum, Collider<T> collider) {
        super();

        Vector3 realMin = new Vector3(Math.min(minimum.x, maximum.x), Math.min(minimum.y, maximum.y), Math.min(minimum.z, maximum.z));
        Vector3 realMax = new Vector3(Math.max(minimum.x, maximum.x), Math.max(minimum.y, maximum.y), Math.max(minimum.z, maximum.z));

        this.root = new OctreeNode(realMin, realMax, maxDepth);
        this.collider = collider;
    }

    public void add (T aabb) {
        root.add(aabb, maxItemsPerNode, collider);
    }

    public void remove (T aabb) {
        root.remove(aabb);
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

    public RayCastResult<T> rayCast (Ray ray, Collider<T> narrowPhase) {
        return rayCast(ray, narrowPhase, Float.POSITIVE_INFINITY);
    }

    public RayCastResult<T> rayCast (Ray ray, Collider<T> narrowPhase, float maxDistance) {
        RayCastResult<T> result = new RayCastResult<>();
        result.distance = Float.NEGATIVE_INFINITY;
        root.rayCast(ray, maxDistance, narrowPhase, result);
        return result;
    }

    public void setMaxDepth (int maxDepth) {
        this.maxDepth = maxDepth;
        this.root.setLevel(maxDepth);
    }

    public void setMaxItemsPerNode (int maxItemsPerNode) {
        this.maxItemsPerNode = maxItemsPerNode;
    }

    /**
     * Method to get nodes boxes. Useful for render purpose
     * @param boxes
     */
    public ObjectSet<BoundingBox> getNodesBoxes (ObjectSet<BoundingBox> boxes) {
        root.getBoundingBox(boxes);
        return boxes;
    }

    protected class OctreeNode {

        protected int level;

        // Represents the boundary of the node
        private BoundingBox bounds;

        private Array<OctreeNode> children;
        private ObjectSet<T> geometries = null;

        public OctreeNode (Vector3 min, Vector3 max, int level) {
            this(min.x, min.y, min.z, max.x, max.y, max.z, level);
        }

        public OctreeNode (float x1, float y1, float z1, float x2, float y2, float z2, int level) {
            bounds = new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
            this.level = level;
        }

        protected void split (int maxItemsPerNode, Collider<T> collider) {
            children = new Array<>(8);

            float midx = (bounds.max.x + bounds.min.x) * 0.5f;
            float midy = (bounds.max.y + bounds.min.y) * 0.5f;
            float midz = (bounds.max.z + bounds.min.z) * 0.5f;

            int deeperLevel = level - 1;

            children.add(new OctreeNode(bounds.min.x, midy, midz, midx, bounds.max.y, bounds.max.z, deeperLevel));
            children.add(new OctreeNode(midx, midy, midz, bounds.max.x, bounds.max.y, bounds.max.z, deeperLevel));
            children.add(new OctreeNode(midx, midy, bounds.min.z, bounds.max.x, bounds.max.y, midz, deeperLevel));
            children.add(new OctreeNode(bounds.min.x, midy, bounds.min.z, midx, bounds.max.y, midz, deeperLevel));
            children.add(new OctreeNode(bounds.min.x, bounds.min.y, midz, midx, midy, bounds.max.z, deeperLevel));
            children.add(new OctreeNode(midx, bounds.min.y, midz, bounds.max.x, midy, bounds.max.z, deeperLevel));
            children.add(new OctreeNode(midx, bounds.min.y, bounds.min.z, bounds.max.x, midy, midz, deeperLevel));
            children.add(new OctreeNode(bounds.min.x, bounds.min.y, bounds.min.z, midx, midy, midz, deeperLevel));

            ObjectSet<T> geometries = this.geometries;
            this.geometries = null;
            // Move geometries from parent to children
            for (OctreeNode child : children) {
                for (T geometry : geometries) {
                    child.add(geometry, maxItemsPerNode, collider);
                }
            }
        }

        public boolean contains (Collider<T> boundingBoxHandler, T geometry) {
            return boundingBoxHandler.intersects(bounds, geometry);
        }

        public void add (T geometry, int maxItemsPerNode, Collider<T> collider) {
            // If is not leaf, check children
            if (!isLeaf()) {
                for (OctreeNode child : children) {
                    child.add(geometry, maxItemsPerNode, collider);
                }
                return;
            }

            if (!collider.intersects(bounds, geometry)) {
                return;
            }

            addGeometry(geometry);
            if (geometries.size > maxItemsPerNode && level > 0) {
                split(maxItemsPerNode, collider);
            }
        }

        public void remove (T aabb) {
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

        public boolean isLeaf () {
            return children == null;
        }

        private void addGeometry (T geometry) {
            if (geometries == null) {
                geometries = new ObjectSet<>();
            }
            geometries.add(geometry);
        }

        public ObjectSet<T> query (BoundingBox aabb, ObjectSet<T> result) {
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

        public ObjectSet<T> query (Frustum frustum, ObjectSet<T> result) {
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

        Vector3 tmp = new Vector3();
        public void rayCast (Ray ray, float maxDistance, Collider<T> narrowPhase, RayCastResult<T> result) {
            // Check intersection with node
            boolean intersect = Intersector.intersectRayBounds(ray, bounds, tmp);
            if (!intersect) {
                return;
            } else {
                float dst2 = tmp.dst2(ray.origin);
                if (dst2 > maxDistance) {
                    return;
                }
            }

            // Check intersection with children
            if (!isLeaf()) {
                for (OctreeNode child : children) {
                    child.rayCast(ray, maxDistance, narrowPhase, result);
                }
            } else {
                // Check intersection with geometries
                if (geometries != null) {
                    for (T geometry: geometries) {
                        float distance = narrowPhase.intersects(ray, geometry);
                        if (result.geometry == null || distance < result.distance) {
                            result.geometry = geometry;
                            result.distance = distance;
                        }
                    }
                }
            }
        }

        public void setLevel (int level) {
            this.level = level;
        }

        /**
         * Get all geometries using Depth-First Search recursion
         * @param resultSet
         */
        public void getAll (ObjectSet<T> resultSet) {
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
        public void getBoundingBox(ObjectSet<BoundingBox> bounds) {
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.getBoundingBox(bounds);
                }
            }
            bounds.add(this.bounds);
        }
    }

    public interface Collider<T> {
        boolean intersects (BoundingBox aabb, T geometry);

        float intersects (Ray ray, T geometry);
    }

    public static class RayCastResult<T> {
        T geometry;
        float distance;
    }
}
