package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectSet;

public class Octree {

    private int maxDepth = 16;
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

    public void insert(BoundingBox aabb) {
        root.insert(aabb, maxItemsPerNode);
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
     * @param frustum
     * @param result set populated with objects near from the frustum
     */
    public ObjectSet<Object> query(Frustum frustum, ObjectSet<Object> result) {
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

    protected static class OctreeNode {

        protected int level;

        // Represents the boundary of the node
        private BoundingBox box;

        private OctreeNode[] children;
        private ObjectSet<BoundingBox> geometries = null;

        public OctreeNode(Vector3 min, Vector3 max, int level) {
            this(min.x, min.y, min.z, max.x, max.y, max.z, level);
        }

        public OctreeNode(float x1, float y1, float z1, float x2, float y2, float z2, int level) {
            box = new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
            this.level = level;
        }

        protected void split() {
            children = new OctreeNode[8];

            float midx = (box.max.x + box.min.x) * 0.5f;
            float midy = (box.max.y + box.min.y) * 0.5f;
            float midz = (box.max.z + box.min.z) * 0.5f;

            int deeperLevel = level - 1;

            children[TopLeftFront] = new OctreeNode(box.min.x, midy, midz, midx, box.max.y, box.max.z, deeperLevel);
            children[TopRightFront] = new OctreeNode(midx, midy, midz, box.max.x, box.max.y, box.max.z, deeperLevel);
            children[TopRightBack] = new OctreeNode(midx, midy, box.min.z, box.max.x, box.max.y, midz, deeperLevel);
            children[TopLeftBack] = new OctreeNode(box.min.x, midy, box.min.z, midx, box.max.y, midz, deeperLevel);
            children[BottomLeftFront] = new OctreeNode(box.min.x, box.min.y, midz, midx, midy, box.max.z, deeperLevel);
            children[BottomRightFront] = new OctreeNode(midx, box.min.y, midz, box.max.x, midy, box.max.z, deeperLevel);
            children[BottomRightBack] = new OctreeNode(midx, box.min.y, box.min.z, box.max.x, midy, midz, deeperLevel);
            children[BottomLeftBack] = new OctreeNode(box.min.x, box.min.y, box.min.z, midx, midy, midz, deeperLevel);
        }

        public boolean intersects(BoundingBox aabb) {
            return box.intersects(aabb);
        }

        public void insert(BoundingBox aabb, int maxItemsPerNode) {
            // If is not leaf, check children
            if (!isLeaf()) {
                for (Octree.OctreeNode child : children) {
                    child.insert(aabb, maxItemsPerNode);
                }
                return;
            }

            if (!intersects(aabb)) {
                return;
            }

            addGeometry(aabb);
            if (geometries.size > maxItemsPerNode && level > 0) {
                split();
                // Move geometries from parent to children
                for (Octree.OctreeNode child : children) {
                    for (BoundingBox geometry : geometries) {
                        child.insert(geometry, maxItemsPerNode);
                    }
                }
                this.geometries = null;
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

        public void query(Frustum frustum, ObjectSet<Object> result) {
            if (!isLeaf()) {
                for (OctreeNode node : children) {
                    node.query(frustum, result);
                }
            }
            if (frustum.boundsInFrustum(box)) {
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
    }
}
  
