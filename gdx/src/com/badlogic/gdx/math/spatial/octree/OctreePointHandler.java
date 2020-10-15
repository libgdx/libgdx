package com.badlogic.gdx.math.spatial.octree;

import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.math.spatial.octree.Octree.*;

public class OctreePointHandler {

    public static boolean contains(OctreeNode node, Vector3 p) {
        // If the point is out of bounds
        if (p.x > node.getMax().x
            || p.x < node.getMin().x
            || p.y > node.getMax().y
            || p.y < node.getMin().y
            || p.z > node.getMax().z
            || p.z < node.getMin().z) {
            return false;
        }
        return true;
    }

    public static boolean insert(OctreeNode node, Vector3 p, int maxDepth) {
        // If the point is out of bounds
        if (!contains(node, p)) {
            return false;
        }

        //If is not Leaf
        if (node.depth < maxDepth) {
            if (node.children == null) {
                node.initChildren();
            }
            // Binary search to insert the point
            int pos = findPosition(node, p);
            return node.children[pos].insert(p, maxDepth);
        }

        // If is leaf
        node.addGeometry(p);
        return true;
    }

    public static OctreeNode queryNode(OctreeNode node, Vector3 p) {
        // If point is out of bound
        if (!contains(node, p)) {
            return null;
        }

        int pos = findPosition(node, p);

        // If not a leaf
        if (node.children != null) {
            return node.children[pos].queryNode(p);
        }

        return node;
    }

    private static int findPosition(OctreeNode node, Vector3 p) {
        // Perform binary search
        // for each ordinate
        float midx = (node.getMax().x + node.getMin().x) * 0.5f;
        float midy = (node.getMax().y + node.getMin().y) * 0.5f;
        float midz = (node.getMax().z + node.getMin().z) * 0.5f;

        int pos;

        // Deciding the position
        // where to move
        if (p.x <= midx) {
            if (p.y <= midy) {
                if (p.z <= midz) {
                    pos = BottomLeftBack;
                } else {
                    pos = BottomLeftFront;
                }
            } else {
                if (p.z <= midz) {
                    pos = TopLeftBack;
                } else {
                    pos = TopLeftFront;
                }
            }
        } else {
            if (p.y <= midy) {
                if (p.z <= midz) {
                    pos = BottomRightBack;
                } else {
                    pos = BottomRightFront;
                }
            } else {
                if (p.z <= midz) {
                    pos = TopRightBack;
                } else {
                    pos = TopRightFront;
                }
            }
        }
        return pos;
    }
}
