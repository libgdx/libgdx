package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectSet;

public class Octree {

    private int maxDepth = 8;
    private int maxItemsPerNode = 32;

    protected static final int TopLeftFront = 0;
    protected static final int TopRightFront = 1;
    protected static final int TopLeftBack = 2;
    protected static final int TopRightBack = 3;
    protected static final int BottomLeftFront = 4;
    protected static final int BottomRightFront = 5;
    protected static final int BottomLeftBack = 6;
    protected static final int BottomRightBack = 7;

    protected OctreeNode root;

    public Octree(Vector3 minimum, Vector3 maximum) {
        super();

        Vector3 realMin = new Vector3(Math.min(minimum.x, maximum.x), Math.min(minimum.y, maximum.y), Math.min(minimum.z, maximum.z));
        Vector3 realMax = new Vector3(Math.max(minimum.x, maximum.x), Math.max(minimum.y, maximum.y), Math.max(minimum.z, maximum.z));

        this.root = new OctreeNode(realMin, realMax, maxDepth);
    }

    public void add(BoundingBox aabb) {
        root.add(aabb, maxItemsPerNode);
    }

    public void remove(BoundingBox aabb) {
        root.remove(aabb);
    }

    public ObjectSet<BoundingBox> getAll(ObjectSet<BoundingBox> resultSet) {
        getAll(root, resultSet);
        return resultSet;
    }

    private void getAll(OctreeNode node, ObjectSet<BoundingBox> resultSet) {
        if (!node.isLeaf()) {
            for (OctreeNode child : node.children) {
                getAll(child, resultSet);
            }
        }
        if (node.geometries != null) {
            resultSet.addAll(node.getGeometries());
        }
    }

    /**
     * Method that represents the broad phase
     *
     * @param aabb
     * @param result set populated with objects inside the BoundingBox
     */
    public ObjectSet<BoundingBox> query(BoundingBox aabb, ObjectSet<BoundingBox> result) {
        root.query(aabb, result);
        return result;
    }

    /**
     * Method that represents the broad phase
     *
     * @param frustum
     * @param result set populated with objects near from the frustum
     */
    public ObjectSet<BoundingBox> query(Frustum frustum, ObjectSet<BoundingBox> result) {
        root.query(frustum, result);
        return result;
    }

    public Octree setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        this.root.setLevel(maxDepth);
        return this;
    }

    public Octree setMaxItemsPerNode(int maxItemsPerNode) {
        this.maxItemsPerNode = maxItemsPerNode;
        return this;
    }

    /**
     * Method to get nodes boxes. Useful for render purpose
     * @param boxes
     */
    public ObjectSet<BoundingBox> getNodesAsBox(ObjectSet<BoundingBox> boxes) {
        root.getBoundingBox(boxes);
        return boxes;
    }

    protected static class OctreeNode {

        protected int level;

        // Represents the boundary of the node
        private BoundingBox bounds;

        private OctreeNode[] children;
        private ObjectSet<BoundingBox> geometries = null;

        public OctreeNode(Vector3 min, Vector3 max, int level) {
            this(min.x, min.y, min.z, max.x, max.y, max.z, level);
        }

        public OctreeNode(float x1, float y1, float z1, float x2, float y2, float z2, int level) {
            bounds = new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
            this.level = level;
        }

        protected void split(int maxItemsPerNode) {
            children = new OctreeNode[8];

            float midx = (bounds.max.x + bounds.min.x) * 0.5f;
            float midy = (bounds.max.y + bounds.min.y) * 0.5f;
            float midz = (bounds.max.z + bounds.min.z) * 0.5f;

            int deeperLevel = level - 1;

            children[TopLeftFront] = new OctreeNode(bounds.min.x, midy, midz, midx, bounds.max.y, bounds.max.z, deeperLevel);
            children[TopRightFront] = new OctreeNode(midx, midy, midz, bounds.max.x, bounds.max.y, bounds.max.z, deeperLevel);
            children[TopRightBack] = new OctreeNode(midx, midy, bounds.min.z, bounds.max.x, bounds.max.y, midz, deeperLevel);
            children[TopLeftBack] = new OctreeNode(bounds.min.x, midy, bounds.min.z, midx, bounds.max.y, midz, deeperLevel);
            children[BottomLeftFront] = new OctreeNode(bounds.min.x, bounds.min.y, midz, midx, midy, bounds.max.z, deeperLevel);
            children[BottomRightFront] = new OctreeNode(midx, bounds.min.y, midz, bounds.max.x, midy, bounds.max.z, deeperLevel);
            children[BottomRightBack] = new OctreeNode(midx, bounds.min.y, bounds.min.z, bounds.max.x, midy, midz, deeperLevel);
            children[BottomLeftBack] = new OctreeNode(bounds.min.x, bounds.min.y, bounds.min.z, midx, midy, midz, deeperLevel);

            ObjectSet<BoundingBox> geometries = this.geometries;
            this.geometries = null;
            // Move geometries from parent to children
            for (Octree.OctreeNode child : children) {
                for (BoundingBox geometry : geometries) {
                    child.add(geometry, maxItemsPerNode);
                }
            }
        }

        public boolean intersects(BoundingBox aabb) {
            return bounds.intersects(aabb);
        }

        public void add(BoundingBox aabb, int maxItemsPerNode) {
            // If is not leaf, check children
            if (!isLeaf()) {
                for (Octree.OctreeNode child : children) {
                    child.add(aabb, maxItemsPerNode);
                }
                return;
            }

            if (!intersects(aabb)) {
                return;
            }

            addGeometry(aabb);
            if (geometries.size > maxItemsPerNode && level > 0) {
                split(maxItemsPerNode);
            }
        }

        public void remove(BoundingBox aabb) {
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

        public boolean isLeaf() {
            return children == null;
        }

        private void addGeometry(BoundingBox geometry) {
            if (geometries == null) {
                geometries = new ObjectSet<BoundingBox>();
            }
            geometries.add(geometry);
        }

        public void query(BoundingBox aabb, ObjectSet<BoundingBox> result) {
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
        }

        public void query(Frustum frustum, ObjectSet<BoundingBox> result) {
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
        }

        public ObjectSet<BoundingBox> getGeometries() {
            return geometries;
        }

        public void setLevel(int level) {
            this.level = level;
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
}
  
