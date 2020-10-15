package com.badlogic.gdx.math.spatial.octree;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.HashSet;
import java.util.Set;

import static com.badlogic.gdx.math.spatial.octree.Octree.*;

public class OctreeNode {

    protected int depth = 0;

    // Represent the boundary of the BoundingBox
    protected BoundingBox box;

    protected OctreeNode[] children;
    private Set<Object> geometries = null;

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

        children[TopLeftFront] = new OctreeNode(
            box.min.x, midy, midz,
            midx, box.max.y, box.max.z)
            .depth(depth + 1);

        children[TopRightFront] = new OctreeNode(
            midx, midy, midz,
            box.max.x, box.max.y, box.max.z)
            .depth(depth + 1);

        children[TopRightBack] = new OctreeNode(
            midx, midy, box.min.z,
            box.max.x, box.max.y, midz)
            .depth(depth + 1);

        children[TopLeftBack] = new OctreeNode(
            box.min.x, midy, box.min.z,
            midx, box.max.y, midz)
            .depth(depth + 1);

        children[BottomLeftFront] = new OctreeNode(
            box.min.x, box.min.y, midz,
            midx, midy, box.max.z)
            .depth(depth + 1);

        children[BottomRightFront] = new OctreeNode(
            midx, box.min.y, midz,
            box.max.x, midy, box.max.z)
            .depth(depth + 1);

        children[BottomRightBack] = new OctreeNode(
            midx, box.min.y, box.min.z,
            box.max.x, midy, midz)
            .depth(depth + 1);

        children[BottomLeftBack] = new OctreeNode(
            box.min.x, box.min.y, box.min.z,
            midx, midy, midz)
            .depth(depth + 1);
    }

    private OctreeNode depth(int depth) {
        this.depth = depth;
        return this;
    }

    public boolean contains(Vector3 p) {
        return OctreePointHandler.contains(this, p);
    }

    public boolean insert(Vector3 p, int maxDepth) {
        return OctreePointHandler.insert(this, p, maxDepth);
    }

    public OctreeNode queryNode(Vector3 p) {
        return OctreePointHandler.queryNode(this, p);
    }

    protected void addGeometry(Object geometry) {
        if (geometries == null) {
            geometries = new HashSet<Object>();
        }
        geometries.add(geometry);
    }

    public Set<Object> getGeometries() {
        return geometries;
    }

    public boolean isLeaf() {
        return geometries != null;
    }

    public Vector3 getMax() {
        return box.max;
    }

    public Vector3 getMin() {
        return box.min;
    }
}
