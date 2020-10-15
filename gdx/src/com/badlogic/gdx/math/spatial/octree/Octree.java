package com.badlogic.gdx.math.spatial.octree;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.spatial.SpatialDatabase;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Octree implements SpatialDatabase {

    private int maxDepth = 5;

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

    private OctreeNode root;
    private Map<Object, Object> geometryMap = new HashMap<Object, Object>();

    public Octree() {
        super();
        this.root = new OctreeNode(Vector3.Zero, Vector3.Zero);
    }

    public Octree(Vector3 minimum, Vector3 maximum) {
        super();

        Vector3 realMin = new Vector3(
            Math.min(minimum.x, maximum.x),
            Math.min(minimum.y, maximum.y),
            Math.min(minimum.z, maximum.z));

        Vector3 realMax = new Vector3(
            Math.max(minimum.x, maximum.x),
            Math.max(minimum.y, maximum.y),
            Math.max(minimum.z, maximum.z));

        this.root = new OctreeNode(realMin, realMax);
    }

    public void insert(Object object, Vector3 p) {
        geometryMap.put(object, p);
        if (!root.contains(p)) {
            resize(p);
            root.insert(p, maxDepth);
        }
    }

    @Override
    public void remove(Object userObject) {

    }

    private OctreeNode resize(Vector3 p) {
        Set<Object> geometries = getAllGeometries();

        Vector3 min = new Vector3(
            Math.min(root.getMin().x, p.x),
            Math.min(root.getMin().y, p.y),
            Math.min(root.getMin().z, p.z));

        Vector3 max = new Vector3(
            Math.max(root.getMax().x, p.x),
            Math.max(root.getMax().y, p.y),
            Math.max(root.getMax().z, p.z));

        resize(geometries, min, max);
        return root;
    }

    private void resize(Set<Object> geometries, Vector3 min, Vector3 max) {
        root.getMin().set(min);
        root.getMax().set(max);
        // Resize children
        root.initChildren();

        // Reinsert geometries
        for (Object object : geometries) {
            if (object instanceof Vector3) {
                root.insert((Vector3) object, maxDepth);
            }
        }
    }

    private Set<Object> getAllGeometries() {
        Set<Object> geometries = new LinkedHashSet<Object>();
        getAllGeometries(geometries, root);

        return geometries;
    }

    private void getAllGeometries(Set<Object> geometries, OctreeNode node) {
        if (node.isLeaf()) {
            geometries.addAll(node.getGeometries());
            return;
        }
        if (node.children == null) {
            return;
        }

        for (OctreeNode child : node.children) {
            getAllGeometries(geometries, child);
        }
    }

    public Set<Object> queryNear(Frustum frustum) {
        // TODO not implemented yet
        return null;
    }

    public Set<Object> queryNear(Vector3 p) {
        OctreeNode node = root.queryNode(p);
        return node.getGeometries();
    }

    public OctreeNode getRoot() {
        return root;
    }

    public Octree maxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Set<Object> getAll() {
        return getAllGeometries();
    }
}
  
