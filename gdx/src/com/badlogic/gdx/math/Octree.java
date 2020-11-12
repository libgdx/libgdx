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
        root.add(object);
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
        result.distance = result.maxDistanceSq;
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
        private final BoundingBox bounds;

        private Array<OctreeNode> children;
        private ObjectSet<T> geometries = null;

        public OctreeNode (Vector3 min, Vector3 max, int level) {
            bounds = new BoundingBox(min, max);
            this.level = level;
        }

        private void split () {
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
                    child.add(geometry);
                }
            }
        }

        protected boolean contains (Collider<T> boundingBoxHandler, T geometry) {
            return boundingBoxHandler.intersects(bounds, geometry);
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
                addGeometry(geometry);
                if (geometries.size > maxItemsPerNode && level > 0) {
                    split();
                }
            }
        }

        protected void remove (T object) {
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.remove(object);
                }
            } else {
                if (geometries != null) {
                    geometries.remove(object);
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

        protected void query (BoundingBox aabb, ObjectSet<T> result) {
            if (!aabb.intersects(bounds)) {
                return;
            }

            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.query(aabb, result);
                }
            } else if (geometries != null) {
                for (T geometry : geometries) {
                    // Filter geometries using collider
                    if (collider.intersects(bounds, geometry)) {
                        result.add(geometry);
                    }
                }
            }
        }

        protected void query (Frustum frustum, ObjectSet<T> result) {
            if (!frustumIntersectsBounds(frustum, bounds)) {
                return;
            }
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.query(frustum, result);
                }
            } else if (geometries != null) {
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
            } else if (geometries != null) {
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

    /** Returns whether the given {@link BoundingBox} is in the frustum.
     *
     * @param frustum The frustum
     * @param bounds The bounding box
     * @return Whether the bounding box is in the frustum */
    private boolean frustumIntersectsBounds(Frustum frustum, BoundingBox bounds) {

        boolean boundsIntersectsFrustum = frustum.pointInFrustum(bounds.getCorner000(tmp)) ||
               frustum.pointInFrustum(bounds.getCorner001(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner010(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner011(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner100(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner101(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner110(tmp)) ||
                frustum.pointInFrustum(bounds.getCorner111(tmp));

        if (boundsIntersectsFrustum) {
            return true;
        }

        boolean frustumIsInsideBounds = false;
        for (Vector3 point : frustum.planePoints) {
            frustumIsInsideBounds |= bounds.contains(point);
        }

        return frustumIsInsideBounds;
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
         * Method to calculate intersection between frustum and the geometry
         *
         * @param frustum
         * @param geometry
         * @return if they are intersecting
         */
        boolean intersects (Frustum frustum, T geometry);

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
        float maxDistanceSq = Float.MAX_VALUE;
    }
}
