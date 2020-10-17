package com.badlogic.gdx.math;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectSet;

public class Octree {

    private int maxDepth = 5;
    private int maxItemsPerNode = 20;

    // Upper Level
    protected static final int TopLeftFront = 0;
    protected static final int TopRightFront = 1;
    protected static final int TopRightBack = 2;
    protected static final int TopLeftBack = 3;
    // Bottom Level
    protected static final int BottomLeftFront = 4;
    protected static final int BottomRightFront = 5;
    protected static final int BottomRightBack = 6;
    protected static final int BottomLeftBack = 7;

    protected OctreeNode root;

    public Octree() {
        super();
        this.root = new OctreeNode(Vector3.Zero, Vector3.Zero);
    }

    public Octree(Vector3 minimum, Vector3 maximum) {
        super();

        Vector3 realMin = new Vector3(Math.min(minimum.x, maximum.x), Math.min(minimum.y, maximum.y), Math.min(minimum.z, maximum.z));
        Vector3 realMax = new Vector3(Math.max(minimum.x, maximum.x), Math.max(minimum.y, maximum.y), Math.max(minimum.z, maximum.z));

        this.root = new OctreeNode(realMin, realMax);
        this.root.setMaxDepth(maxDepth);
    }

    public void insert(BoundingBox aabb) {
        root.insert(aabb);
    }

    public void remove(BoundingBox aabb) {
        root.remove(aabb);
    }

    public void getAll(ObjectSet<Object> resultSet) {
        getAll(root, resultSet);
    }

    private void getAll(OctreeNode node, ObjectSet<Object> resultSet) {
        if (node.geometries != null) {
            resultSet.addAll(node.getGeometries());
            return;
        }
        if (node.children == null) {
            return;
        }

        for (OctreeNode child : node.children) {
            getAll(child, resultSet);
        }
    }

    /**
     * Method that represents the broad phase
     *
     * @param frustum
     * @param result  set populated with objects near from the frustum
     */
    public void query(Frustum frustum, ObjectSet<Object> result) {
        root.query(frustum, result);
    }

    public Octree setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        this.root.setMaxDepth(maxDepth);
        return this;
    }

    public Octree setMaxItemsPerNode(int maxItemsPerNode) {
        this.maxItemsPerNode = maxItemsPerNode;
        return this;
    }

    private static class OctreeNode {

        protected int maxDepth = 0;

        // Represents the boundary of the node
        private BoundingBox box;

        private OctreeNode[] children;
        private ObjectSet<Object> geometries = null;

        public OctreeNode(Vector3 min, Vector3 max) {
            this(min.x, min.y, min.z, max.x, max.y, max.z);
        }

        public OctreeNode(float x1, float y1, float z1, float x2, float y2, float z2) {
            box = new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
        }

        protected void initChildren() {
            children = new OctreeNode[8];

            float midx = (box.max.x + box.min.x) * 0.5f;
            float midy = (box.max.y + box.min.y) * 0.5f;
            float midz = (box.max.z + box.min.z) * 0.5f;

            children[TopLeftFront] = new OctreeNode(box.min.x, midy, midz, midx, box.max.y, box.max.z).setMaxDepth(maxDepth - 1);
            children[TopRightFront] = new OctreeNode(midx, midy, midz, box.max.x, box.max.y, box.max.z).setMaxDepth(maxDepth - 1);
            children[TopRightBack] = new OctreeNode(midx, midy, box.min.z, box.max.x, box.max.y, midz).setMaxDepth(maxDepth - 1);
            children[TopLeftBack] = new OctreeNode(box.min.x, midy, box.min.z, midx, box.max.y, midz).setMaxDepth(maxDepth - 1);
            children[BottomLeftFront] = new OctreeNode(box.min.x, box.min.y, midz, midx, midy, box.max.z).setMaxDepth(maxDepth - 1);
            children[BottomRightFront] = new OctreeNode(midx, box.min.y, midz, box.max.x, midy, box.max.z).setMaxDepth(maxDepth - 1);
            children[BottomRightBack] = new OctreeNode(midx, box.min.y, box.min.z, box.max.x, midy, midz).setMaxDepth(maxDepth - 1);
            children[BottomLeftBack] = new OctreeNode(box.min.x, box.min.y, box.min.z, midx, midy, midz).setMaxDepth(maxDepth - 1);
        }

        public boolean intersects(BoundingBox aabb) {
            return box.intersects(aabb);
        }

        public void insert(BoundingBox aabb) {
            if (!intersects(aabb)) {
                return;
            }

            addGeometry(aabb);
            
            if (maxDepth != 0) {
                if (children == null) {
                    initChildren();
                }

                for (Octree.OctreeNode child : children) {
                    child.insert(aabb);
                }
            }
        }

        public void remove(BoundingBox aabb) {
            if (geometries != null) {
                geometries.remove(aabb);
            }

            if (children != null) {
                for (OctreeNode node : children) {
                    node.remove(aabb);
                }
            }
        }

        private void addGeometry(Object geometry) {
            if (geometries == null) {
                geometries = new ObjectSet<Object>();
            }
            geometries.add(geometry);
        }

        public void query(Frustum frustum, ObjectSet<Object> result) {
            if (!frustum.boundsInFrustum(box)) {
                return;
            }

            if (geometries != null) {
                result.addAll(geometries);
                return;
            }

            for (OctreeNode node : children) {
                node.query(frustum, result);
            }
        }

        public ObjectSet<Object> getGeometries() {
            return geometries;
        }

        private OctreeNode setMaxDepth(int depth) {
            this.maxDepth = depth;
            return this;
        }

    }

}
  
