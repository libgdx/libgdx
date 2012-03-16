/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.AABB;
import static org.jbox2d.collision.broadphase.TreeNode.NULL_NODE;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.pooling.stacks.DynamicIntStack;

// updated to rev 100
/**
 * A dynamic tree arranges data in a binary tree to accelerate queries such as volume queries and
 * ray casts. Leafs are proxies with an AABB. In the tree we expand the proxy AABB by _fatAABBFactor
 * so that the proxy AABB is bigger than the client object. This allows the client object to move by
 * small amounts without triggering a tree update.
 * 
 * @author daniel
 */
public class DynamicTree {
  public static final int MAX_STACK_SIZE = 64;

  private int m_root;
  private TreeNode[] m_nodes;
  private int m_nodeCount;
  private int m_nodeCapacity;

  private int m_freeList;

  private int m_insertionCount;

  private final Vec2[] drawVecs = new Vec2[4];
  private final DynamicIntStack intStack = new DynamicIntStack(10);

  public DynamicTree() {
    m_root = TreeNode.NULL_NODE;
    m_nodeCount = 0;
    m_nodeCapacity = 16;
    m_nodes = new TreeNode[16];

    // Build a linked list for the free list.
    for (int i = 0; i < m_nodeCapacity; i++) {
      m_nodes[i] = new TreeNode();
      m_nodes[i].parent = i + 1;
      m_nodes[i].height = -1;
    }
    m_nodes[m_nodeCapacity - 1].parent = TreeNode.NULL_NODE;
    m_freeList = 0;

    m_insertionCount = 0;

    for (int i = 0; i < drawVecs.length; i++) {
      drawVecs[i] = new Vec2();
    }
  }

  /**
   * Create a proxy. Provide a tight fitting AABB and a userData pointer.
   * 
   * @param aabb
   * @param userData
   * @return
   */
  public final int createProxy(final AABB aabb, Object userData) {
    int proxyId = allocateNode();

    // Fatten the aabb
    final TreeNode node = m_nodes[proxyId];
    node.aabb.lowerBound.x = aabb.lowerBound.x - Settings.aabbExtension;
    node.aabb.lowerBound.y = aabb.lowerBound.y - Settings.aabbExtension;
    node.aabb.upperBound.x = aabb.upperBound.x + Settings.aabbExtension;
    node.aabb.upperBound.y = aabb.upperBound.y + Settings.aabbExtension;
    node.userData = userData;

    insertLeaf(proxyId);

    return proxyId;
  }

  /**
   * Destroy a proxy
   * 
   * @param proxyId
   */
  public final void destroyProxy(int proxyId) {
    assert (0 <= proxyId && proxyId < m_nodeCapacity);
    assert (m_nodes[proxyId].isLeaf());

    removeLeaf(proxyId);
    freeNode(proxyId);
  }

  // djm pooling
  /**
   * Move a proxy with a swepted AABB. If the proxy has moved outside of its fattened AABB, then the
   * proxy is removed from the tree and re-inserted. Otherwise the function returns immediately.
   * 
   * @return true if the proxy was re-inserted.
   */
  public final boolean moveProxy(int proxyId, final AABB aabb, Vec2 displacement) {
    assert (0 <= proxyId && proxyId < m_nodeCapacity);
    final TreeNode node = m_nodes[proxyId];
    assert (node.isLeaf());

    if (node.aabb.contains(aabb)) {
      return false;
    }

    removeLeaf(proxyId);

    // Extend AABB
    final Vec2 lowerBound = aabb.lowerBound;
    final Vec2 upperBound = aabb.upperBound;
    lowerBound.x -= Settings.aabbExtension;
    lowerBound.y -= Settings.aabbExtension;
    upperBound.x += Settings.aabbExtension;
    upperBound.y += Settings.aabbExtension;

    
    // Predict AABB displacement.
    final float dx = displacement.x * Settings.aabbMultiplier;
    final float dy = displacement.y * Settings.aabbMultiplier;
    if (dx < 0.0f) {
      lowerBound.x += dx;
    } else {
      upperBound.x += dx;
    }

    if (dy < 0.0f) {
      lowerBound.y += dy;
    } else {
      upperBound.y += dy;
    }
    node.aabb.lowerBound.x = lowerBound.x;
    node.aabb.lowerBound.y = lowerBound.y;
    node.aabb.upperBound.x = upperBound.x;
    node.aabb.upperBound.y = upperBound.y;

    insertLeaf(proxyId);
    return true;
  }

  public final Object getUserData(int proxyId) {
    assert (0 <= proxyId && proxyId < m_nodeCapacity);
    return m_nodes[proxyId].userData;
  }

  public final AABB getFatAABB(int proxyId) {
    assert (0 <= proxyId && proxyId < m_nodeCapacity);
    return m_nodes[proxyId].aabb;
  }

  /**
   * Query an AABB for overlapping proxies. The callback class is called for each proxy that
   * overlaps the supplied AABB.
   * 
   * @param callback
   * @param araabbgAABB
   */
  public final void query(TreeCallback callback, AABB aabb) {
    intStack.reset();
    intStack.push(m_root);

    while (intStack.getCount() > 0) {
      int nodeId = intStack.pop();
      if (nodeId == TreeNode.NULL_NODE) {
        continue;
      }

      final TreeNode node = m_nodes[nodeId];

      if (AABB.testOverlap(node.aabb, aabb)) {
        if (node.isLeaf()) {
          boolean proceed = callback.treeCallback(nodeId);
          if (!proceed) {
            return;
          }
        } else {
          intStack.push(node.child1);
          intStack.push(node.child2);
        }
      }
    }
  }

  private final Vec2 r = new Vec2();
  private final Vec2 v = new Vec2();
  private final Vec2 absV = new Vec2();
  private final Vec2 temp = new Vec2();
  private final Vec2 c = new Vec2();
  private final Vec2 h = new Vec2();
  private final Vec2 t = new Vec2();
  private final AABB aabb = new AABB();
  private final RayCastInput subInput = new RayCastInput();

  /**
   * Ray-cast against the proxies in the tree. This relies on the callback to perform a exact
   * ray-cast in the case were the proxy contains a shape. The callback also performs the any
   * collision filtering. This has performance roughly equal to k * log(n), where k is the number of
   * collisions and n is the number of proxies in the tree.
   * 
   * @param input the ray-cast input data. The ray extends from p1 to p1 + maxFraction * (p2 - p1).
   * @param callback a callback class that is called for each proxy that is hit by the ray.
   */
  public void raycast(TreeRayCastCallback callback, RayCastInput input) {
    final Vec2 p1 = input.p1;
    final Vec2 p2 = input.p2;
    r.set(p2).subLocal(p1);
    assert (r.lengthSquared() > 0f);
    r.normalize();

    // v is perpendicular to the segment.
    Vec2.crossToOutUnsafe(1f, r, v);
    absV.set(v).absLocal();

    // Separating axis for segment (Gino, p80).
    // |dot(v, p1 - c)| > dot(|v|, h)

    float maxFraction = input.maxFraction;

    // Build a bounding box for the segment.
    final AABB segAABB = aabb;
    // Vec2 t = p1 + maxFraction * (p2 - p1);
    temp.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
    Vec2.minToOut(p1, temp, segAABB.lowerBound);
    Vec2.maxToOut(p1, temp, segAABB.upperBound);

    intStack.push(m_root);
    while (intStack.getCount() > 0) {
      int nodeId = intStack.pop();
      if (nodeId == TreeNode.NULL_NODE) {
        continue;
      }

      final TreeNode node = m_nodes[nodeId];

      if (!AABB.testOverlap(node.aabb, segAABB)) {
        continue;
      }

      // Separating axis for segment (Gino, p80).
      // |dot(v, p1 - c)| > dot(|v|, h)
      node.aabb.getCenterToOut(c);
      node.aabb.getExtentsToOut(h);
      temp.set(p1).subLocal(c);
      float separation = MathUtils.abs(Vec2.dot(v, temp)) - Vec2.dot(absV, h);
      if (separation > 0.0f) {
        continue;
      }

      if (node.isLeaf()) {
        subInput.p1.set(input.p1);
        subInput.p2.set(input.p2);
        subInput.maxFraction = maxFraction;

        float value = callback.raycastCallback(subInput, nodeId);

        if (value == 0.0f) {
          // The client has terminated the ray cast.
          return;
        }

        if (value > 0.0f) {
          // Update segment bounding box.
          maxFraction = value;
          t.set(p2).subLocal(p1).mulLocal(maxFraction).addLocal(p1);
          Vec2.minToOut(p1, t, segAABB.lowerBound);
          Vec2.maxToOut(p1, t, segAABB.upperBound);
        }
      } else {
        intStack.push(node.child1);
        intStack.push(node.child2);
      }
    }
  }

  /**
   * Compute the height of the tree.
   */
  public final int computeHeight() {
    return computeHeight(m_root);
  }

  private final int computeHeight(int nodeId) {
    assert (0 <= nodeId && nodeId < m_nodeCapacity);

    final TreeNode node = m_nodes[nodeId];

    if (node.isLeaf()) {
      return 0;
    }
    int height1 = computeHeight(node.child1);
    int height2 = computeHeight(node.child2);
    return 1 + MathUtils.max(height1, height2);
  }

  /**
   * Validate this tree. For testing.
   */
  public void validate() {
    validateStructure(m_root);
    validateMetrics(m_root);

    int freeCount = 0;
    int freeIndex = m_freeList;
    while (freeIndex != NULL_NODE) {
      assert (0 <= freeIndex && freeIndex < m_nodeCapacity);
      freeIndex = m_nodes[freeIndex].parent;
      ++freeCount;
    }

    assert (getHeight() == computeHeight());

    assert (m_nodeCount + freeCount == m_nodeCapacity);
  }

  /**
   * Compute the height of the binary tree in O(N) time. Should not be called often.
   * 
   * @return
   */
  public int getHeight() {
    if (m_root == NULL_NODE) {
      return 0;
    }
    return m_nodes[m_root].height;
  }

  /**
   * Get the maximum balance of an node in the tree. The balance is the difference in height of the
   * two children of a node.
   * 
   * @return
   */
  public int getMaxBalance() {
    int maxBalance = 0;
    for (int i = 0; i < m_nodeCapacity; ++i) {
      final TreeNode node = m_nodes[i];
      if (node.height <= 1) {
        continue;
      }

      assert (node.isLeaf() == false);

      int child1 = node.child1;
      int child2 = node.child2;
      int balance = MathUtils.abs(m_nodes[child2].height - m_nodes[child1].height);
      maxBalance = MathUtils.max(maxBalance, balance);
    }

    return maxBalance;
  }

  /**
   * Get the ratio of the sum of the node areas to the root area.
   * 
   * @return
   */
  public float getAreaRatio() {
    if (m_root == NULL_NODE) {
      return 0.0f;
    }

    final TreeNode root = m_nodes[m_root];
    float rootArea = root.aabb.getPerimeter();

    float totalArea = 0.0f;
    for (int i = 0; i < m_nodeCapacity; ++i) {
      final TreeNode node = m_nodes[i];
      if (node.height < 0) {
        // Free node in pool
        continue;
      }

      totalArea += node.aabb.getPerimeter();
    }

    return totalArea / rootArea;
  }

  /**
   * Build an optimal tree. Very expensive. For testing.
   */
  public void rebuildBottomUp() {
    int[] nodes = new int[m_nodeCount];
    int count = 0;

    // Build array of leaves. Free the rest.
    for (int i = 0; i < m_nodeCapacity; ++i) {
      if (m_nodes[i].height < 0) {
        // free node in pool
        continue;
      }

      if (m_nodes[i].isLeaf()) {
        m_nodes[i].parent = NULL_NODE;
        nodes[count] = i;
        ++count;
      } else {
        freeNode(i);
      }
    }

    AABB b = new AABB();
    while (count > 1) {
      float minCost = Float.MAX_VALUE;
      int iMin = -1, jMin = -1;
      for (int i = 0; i < count; ++i) {
        AABB aabbi = m_nodes[nodes[i]].aabb;

        for (int j = i + 1; j < count; ++j) {
          AABB aabbj = m_nodes[nodes[j]].aabb;
          b.combine(aabbi, aabbj);
          float cost = b.getPerimeter();
          if (cost < minCost) {
            iMin = i;
            jMin = j;
            minCost = cost;
          }
        }
      }

      int index1 = nodes[iMin];
      int index2 = nodes[jMin];
      TreeNode child1 = m_nodes[index1];
      TreeNode child2 = m_nodes[index2];

      int parentIndex = allocateNode();
      TreeNode parent = m_nodes[parentIndex];
      parent.child1 = index1;
      parent.child2 = index2;
      parent.height = 1 + MathUtils.max(child1.height, child2.height);
      parent.aabb.combine(child1.aabb, child2.aabb);
      parent.parent = NULL_NODE;

      child1.parent = parentIndex;
      child2.parent = parentIndex;

      nodes[jMin] = nodes[count - 1];
      nodes[iMin] = parentIndex;
      --count;
    }

    m_root = nodes[0];

    validate();
  }

  private final int allocateNode() {
    if (m_freeList == NULL_NODE) {
      assert (m_nodeCount == m_nodeCapacity);

      TreeNode[] old = m_nodes;
      m_nodeCapacity *= 2;
      m_nodes = new TreeNode[m_nodeCapacity];
      System.arraycopy(old, 0, m_nodes, 0, old.length);

      // Build a linked list for the free list.
      for (int i = m_nodeCount; i < m_nodeCapacity; i++) {
        m_nodes[i] = new TreeNode();
        m_nodes[i].parent = i + 1;
        m_nodes[i].height = -1;
      }
      m_nodes[m_nodeCapacity - 1].parent = TreeNode.NULL_NODE;
      m_freeList = m_nodeCount;
    }
    int nodeId = m_freeList;
    m_freeList = m_nodes[nodeId].parent;

    m_nodes[nodeId].parent = NULL_NODE;
    m_nodes[nodeId].child1 = NULL_NODE;
    m_nodes[nodeId].child2 = NULL_NODE;
    m_nodes[nodeId].height = 0;
    m_nodes[nodeId].userData = null;
    ++m_nodeCount;
    return nodeId;
  }

  /**
   * returns a node to the pool
   * 
   * @param argNode
   */
  private final void freeNode(int nodeId) {
    assert (nodeId != NULL_NODE);
    assert (0 < m_nodeCount);
    m_nodes[nodeId].parent = m_freeList;
    m_nodes[nodeId].height = -1;
    m_freeList = nodeId;
    m_nodeCount--;
  }


  public int getInsertionCount() {
    return m_insertionCount;
  }

  private final Vec2 center = new Vec2();
  private final Vec2 delta1 = new Vec2();
  private final Vec2 delta2 = new Vec2();
  private final AABB combinedAABB = new AABB();

  private final void insertLeaf(int leaf) {
    m_insertionCount++;

    if (m_root == NULL_NODE) {
      m_root = leaf;
      m_nodes[m_root].parent = NULL_NODE;
      return;
    }

    // find the best sibling
    AABB leafAABB = m_nodes[leaf].aabb;
    int index = m_root;
    while (m_nodes[index].isLeaf() == false) {
      final TreeNode node = m_nodes[index];
      int child1 = node.child1;
      int child2 = node.child2;

      float area = node.aabb.getPerimeter();

      combinedAABB.combine(node.aabb, leafAABB);
      float combinedArea = combinedAABB.getPerimeter();

      // Cost of creating a new parent for this node and the new leaf
      float cost = 2.0f * combinedArea;

      // Minimum cost of pushing the leaf further down the tree
      float inheritanceCost = 2.0f * (combinedArea - area);

      // Cost of descending into child1
      float cost1;
      if (m_nodes[child1].isLeaf()) {
        combinedAABB.combine(leafAABB, m_nodes[child1].aabb);
        cost1 = combinedAABB.getPerimeter() + inheritanceCost;
      } else {
        combinedAABB.combine(leafAABB, m_nodes[child1].aabb);
        float oldArea = m_nodes[child1].aabb.getPerimeter();
        float newArea = combinedAABB.getPerimeter();
        cost1 = (newArea - oldArea) + inheritanceCost;
      }

      // Cost of descending into child2
      float cost2;
      if (m_nodes[child2].isLeaf()) {
        combinedAABB.combine(leafAABB, m_nodes[child2].aabb);
        cost2 = combinedAABB.getPerimeter() + inheritanceCost;
      } else {
        combinedAABB.combine(leafAABB, m_nodes[child2].aabb);
        float oldArea = m_nodes[child2].aabb.getPerimeter();
        float newArea = combinedAABB.getPerimeter();
        cost2 = newArea - oldArea + inheritanceCost;
      }

      // Descend according to the minimum cost.
      if (cost < cost1 && cost < cost2) {
        break;
      }

      // Descend
      if (cost1 < cost2) {
        index = child1;
      } else {
        index = child2;
      }
    }

    int sibling = index;
    int oldParent = m_nodes[sibling].parent;
    int newParentId = allocateNode();
    final TreeNode newParent = m_nodes[newParentId];
    newParent.parent = oldParent;
    newParent.userData = null;
    newParent.aabb.combine(leafAABB, m_nodes[sibling].aabb);
    newParent.height = m_nodes[sibling].height + 1;

    if (oldParent != NULL_NODE) {
      // The sibling was not the root.
      if (m_nodes[oldParent].child1 == sibling) {
        m_nodes[oldParent].child1 = newParentId;
      } else {
        m_nodes[oldParent].child2 = newParentId;
      }

      m_nodes[newParentId].child1 = sibling;
      m_nodes[newParentId].child2 = leaf;
      m_nodes[sibling].parent = newParentId;
      m_nodes[leaf].parent = newParentId;
    } else {
      // The sibling was the root.
      m_nodes[newParentId].child1 = sibling;
      m_nodes[newParentId].child2 = leaf;
      m_nodes[sibling].parent = newParentId;
      m_nodes[leaf].parent = newParentId;
      m_root = newParentId;
    }

    // Walk back up the tree fixing heights and AABBs
    index = m_nodes[leaf].parent;
    while (index != NULL_NODE) {
      index = balance(index);

      int child1 = m_nodes[index].child1;
      int child2 = m_nodes[index].child2;

      assert (child1 != NULL_NODE);
      assert (child2 != NULL_NODE);

      m_nodes[index].height = 1 + MathUtils.max(m_nodes[child1].height, m_nodes[child2].height);
      m_nodes[index].aabb.combine(m_nodes[child1].aabb, m_nodes[child2].aabb);

      index = m_nodes[index].parent;
    }

    // validate();
  }

  private final void removeLeaf(int leaf) {
    if (leaf == m_root) {
      m_root = NULL_NODE;
      return;
    }

    int parent = m_nodes[leaf].parent;
    int grandParent = m_nodes[parent].parent;
    int sibling;
    if (m_nodes[parent].child1 == leaf) {
      sibling = m_nodes[parent].child2;
    } else {
      sibling = m_nodes[parent].child1;
    }

    if (grandParent != NULL_NODE) {
      // Destroy parent and connect sibling to grandParent.
      if (m_nodes[grandParent].child1 == parent) {
        m_nodes[grandParent].child1 = sibling;
      } else {
        m_nodes[grandParent].child2 = sibling;
      }
      m_nodes[sibling].parent = grandParent;
      freeNode(parent);

      // Adjust ancestor bounds.
      int index = grandParent;
      while (index != NULL_NODE) {
        index = balance(index);

        int child1 = m_nodes[index].child1;
        int child2 = m_nodes[index].child2;

        m_nodes[index].aabb.combine(m_nodes[child1].aabb, m_nodes[child2].aabb);
        m_nodes[index].height = 1 + MathUtils.max(m_nodes[child1].height, m_nodes[child2].height);

        index = m_nodes[index].parent;
      }
    } else {
      m_root = sibling;
      m_nodes[sibling].parent = NULL_NODE;
      freeNode(parent);
    }

    // validate();
  }

  // Perform a left or right rotation if node A is imbalanced.
  // Returns the new root index.
  private int balance(int iA) {
    assert (iA != NULL_NODE);

    TreeNode A = m_nodes[iA];
    if (A.isLeaf() || A.height < 2) {
      return iA;
    }

    int iB = A.child1;
    int iC = A.child2;
    assert (0 <= iB && iB < m_nodeCapacity);
    assert (0 <= iC && iC < m_nodeCapacity);

    TreeNode B = m_nodes[iB];
    TreeNode C = m_nodes[iC];

    int balance = C.height - B.height;

    // Rotate C up
    if (balance > 1) {
      int iF = C.child1;
      int iG = C.child2;
      TreeNode F = m_nodes[iF];
      TreeNode G = m_nodes[iG];
      assert (0 <= iF && iF < m_nodeCapacity);
      assert (0 <= iG && iG < m_nodeCapacity);

      // Swap A and C
      C.child1 = iA;
      C.parent = A.parent;
      A.parent = iC;

      // A's old parent should point to C
      if (C.parent != NULL_NODE) {
        if (m_nodes[C.parent].child1 == iA) {
          m_nodes[C.parent].child1 = iC;
        } else {
          assert (m_nodes[C.parent].child2 == iA);
          m_nodes[C.parent].child2 = iC;
        }
      } else {
        m_root = iC;
      }

      // Rotate
      if (F.height > G.height) {
        C.child2 = iF;
        A.child2 = iG;
        G.parent = iA;
        A.aabb.combine(B.aabb, G.aabb);
        C.aabb.combine(A.aabb, F.aabb);

        A.height = 1 + MathUtils.max(B.height, G.height);
        C.height = 1 + MathUtils.max(A.height, F.height);
      } else {
        C.child2 = iG;
        A.child2 = iF;
        F.parent = iA;
        A.aabb.combine(B.aabb, F.aabb);
        C.aabb.combine(A.aabb, G.aabb);

        A.height = 1 + MathUtils.max(B.height, F.height);
        C.height = 1 + MathUtils.max(A.height, G.height);
      }

      return iC;
    }

    // Rotate B up
    if (balance < -1) {
      int iD = B.child1;
      int iE = B.child2;
      TreeNode D = m_nodes[iD];
      TreeNode E = m_nodes[iE];
      assert (0 <= iD && iD < m_nodeCapacity);
      assert (0 <= iE && iE < m_nodeCapacity);

      // Swap A and B
      B.child1 = iA;
      B.parent = A.parent;
      A.parent = iB;

      // A's old parent should point to B
      if (B.parent != NULL_NODE) {
        if (m_nodes[B.parent].child1 == iA) {
          m_nodes[B.parent].child1 = iB;
        } else {
          assert (m_nodes[B.parent].child2 == iA);
          m_nodes[B.parent].child2 = iB;
        }
      } else {
        m_root = iB;
      }

      // Rotate
      if (D.height > E.height) {
        B.child2 = iD;
        A.child1 = iE;
        E.parent = iA;
        A.aabb.combine(C.aabb, E.aabb);
        B.aabb.combine(A.aabb, D.aabb);

        A.height = 1 + MathUtils.max(C.height, E.height);
        B.height = 1 + MathUtils.max(A.height, D.height);
      } else {
        B.child2 = iE;
        A.child1 = iD;
        D.parent = iA;
        A.aabb.combine(C.aabb, D.aabb);
        B.aabb.combine(A.aabb, E.aabb);

        A.height = 1 + MathUtils.max(C.height, D.height);
        B.height = 1 + MathUtils.max(A.height, E.height);
      }

      return iB;
    }

    return iA;
  }

  private void validateStructure(int index) {
    if (index == NULL_NODE) {
      return;
    }

    if (index == m_root) {
      assert (m_nodes[index].parent == NULL_NODE);
    }

    final TreeNode node = m_nodes[index];

    int child1 = node.child1;
    int child2 = node.child2;

    if (node.isLeaf()) {
      assert (child1 == NULL_NODE);
      assert (child2 == NULL_NODE);
      assert (node.height == 0);
      return;
    }

    assert (0 <= child1 && child1 < m_nodeCapacity);
    assert (0 <= child2 && child2 < m_nodeCapacity);

    assert (m_nodes[child1].parent == index);
    assert (m_nodes[child2].parent == index);

    validateStructure(child1);
    validateStructure(child2);
  }

  private void validateMetrics(int index) {
    if (index == NULL_NODE) {
      return;
    }

    final TreeNode node = m_nodes[index];

    int child1 = node.child1;
    int child2 = node.child2;

    if (node.isLeaf()) {
      assert (child1 == NULL_NODE);
      assert (child2 == NULL_NODE);
      assert (node.height == 0);
      return;
    }

    assert (0 <= child1 && child1 < m_nodeCapacity);
    assert (0 <= child2 && child2 < m_nodeCapacity);

    int height1 = m_nodes[child1].height;
    int height2 = m_nodes[child2].height;
    int height;
    height = 1 + MathUtils.max(height1, height2);
    assert (node.height == height);

    AABB aabb = new AABB();
    aabb.combine(m_nodes[child1].aabb, m_nodes[child2].aabb);

    assert (aabb.lowerBound.equals(node.aabb.lowerBound));
    assert (aabb.upperBound.equals(node.aabb.upperBound));

    validateMetrics(child1);
    validateMetrics(child2);
  }

  public void drawTree(DebugDraw argDraw) {
    if (m_root == NULL_NODE) {
      return;
    }
    int height = computeHeight();
    drawTree(argDraw, m_root, 0, height);
  }

  private final Color3f color = new Color3f();
  private final Vec2 textVec = new Vec2();

  public void drawTree(DebugDraw argDraw, int nodeId, int spot, int height) {
    final TreeNode node = m_nodes[nodeId];
    node.aabb.getVertices(drawVecs);

    color.set(1, (height - spot) * 1f / height, (height - spot) * 1f / height);
    argDraw.drawPolygon(drawVecs, 4, color);

    argDraw.getViewportTranform().getWorldToScreen(node.aabb.upperBound, textVec);
    argDraw.drawString(textVec.x, textVec.y, nodeId + "-" + (spot + 1) + "/" + height, color);

    if (node.child1 != NULL_NODE) {
      drawTree(argDraw, node.child1, spot + 1, height);
    }
    if (node.child2 != NULL_NODE) {
      drawTree(argDraw, node.child2, spot + 1, height);
    }
  }
}
