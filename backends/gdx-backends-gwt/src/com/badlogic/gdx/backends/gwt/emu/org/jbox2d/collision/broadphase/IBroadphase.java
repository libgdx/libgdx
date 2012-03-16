package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;

public interface IBroadphase {
  
  int createProxy(AABB aabb, Object userData);
  
  void destroyProxy(int proxyId);
  
  void moveProxy(int proxyIdA, int proxyIdB);
  
  void touchProxy(int proxyId);
  
  AABB getFatAABB(int proxyId);
  
  Object getUserData(int proxyId);
  
  boolean testOverlap(int proxyIdA, int proxyIdB);
  
  int getProxyCount();
  
  void query(QueryCallback callback, AABB aabb);
  
  void raycast(RayCastCallback callback, RayCastInput input);
  
  int getTreeHeight();
  
  int getTreeBalance();
  
  float getTreeQuality();
  
}
