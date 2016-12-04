/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btSimulationIslandManager extends BulletBase {
	private long swigCPtr;
	
	protected btSimulationIslandManager(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btSimulationIslandManager, normally you should not need this constructor it's intended for low-level usage. */ 
	public btSimulationIslandManager(long cPtr, boolean cMemoryOwn) {
		this("btSimulationIslandManager", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btSimulationIslandManager obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}

  @Override protected synchronized void delete() {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				CollisionJNI.delete_btSimulationIslandManager(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public btSimulationIslandManager() {
    this(CollisionJNI.new_btSimulationIslandManager(), true);
  }

  public void initUnionFind(int n) {
    CollisionJNI.btSimulationIslandManager_initUnionFind(swigCPtr, this, n);
  }

  public btUnionFind getUnionFind() {
    return new btUnionFind(CollisionJNI.btSimulationIslandManager_getUnionFind(swigCPtr, this), false);
  }

  public void updateActivationState(btCollisionWorld colWorld, btDispatcher dispatcher) {
    CollisionJNI.btSimulationIslandManager_updateActivationState(swigCPtr, this, btCollisionWorld.getCPtr(colWorld), colWorld, btDispatcher.getCPtr(dispatcher), dispatcher);
  }

  public void storeIslandActivationState(btCollisionWorld world) {
    CollisionJNI.btSimulationIslandManager_storeIslandActivationState(swigCPtr, this, btCollisionWorld.getCPtr(world), world);
  }

  public void findUnions(btDispatcher dispatcher, btCollisionWorld colWorld) {
    CollisionJNI.btSimulationIslandManager_findUnions(swigCPtr, this, btDispatcher.getCPtr(dispatcher), dispatcher, btCollisionWorld.getCPtr(colWorld), colWorld);
  }

  static public class IslandCallback extends BulletBase {
  	private long swigCPtr;
  	
  	protected IslandCallback(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new IslandCallback, normally you should not need this constructor it's intended for low-level usage. */ 
  	public IslandCallback(long cPtr, boolean cMemoryOwn) {
  		this("IslandCallback", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(IslandCallback obj) {
  		return (obj == null) ? 0 : obj.swigCPtr;
  	}
  
  	@Override
  	protected void finalize() throws Throwable {
  		if (!destroyed)
  			destroy();
  		super.finalize();
  	}
  
    @Override protected synchronized void delete() {
  		if (swigCPtr != 0) {
  			if (swigCMemOwn) {
  				swigCMemOwn = false;
  				CollisionJNI.delete_btSimulationIslandManager_IslandCallback(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public void processIsland(SWIGTYPE_p_p_btCollisionObject bodies, int numBodies, SWIGTYPE_p_p_btPersistentManifold manifolds, int numManifolds, int islandId) {
      CollisionJNI.btSimulationIslandManager_IslandCallback_processIsland(swigCPtr, this, SWIGTYPE_p_p_btCollisionObject.getCPtr(bodies), numBodies, SWIGTYPE_p_p_btPersistentManifold.getCPtr(manifolds), numManifolds, islandId);
    }
  
  }

  public void buildAndProcessIslands(btDispatcher dispatcher, btCollisionWorld collisionWorld, btSimulationIslandManager.IslandCallback callback) {
    CollisionJNI.btSimulationIslandManager_buildAndProcessIslands(swigCPtr, this, btDispatcher.getCPtr(dispatcher), dispatcher, btCollisionWorld.getCPtr(collisionWorld), collisionWorld, btSimulationIslandManager.IslandCallback.getCPtr(callback), callback);
  }

  public void buildIslands(btDispatcher dispatcher, btCollisionWorld colWorld) {
    CollisionJNI.btSimulationIslandManager_buildIslands(swigCPtr, this, btDispatcher.getCPtr(dispatcher), dispatcher, btCollisionWorld.getCPtr(colWorld), colWorld);
  }

  public boolean getSplitIslands() {
    return CollisionJNI.btSimulationIslandManager_getSplitIslands(swigCPtr, this);
  }

  public void setSplitIslands(boolean doSplitIslands) {
    CollisionJNI.btSimulationIslandManager_setSplitIslands(swigCPtr, this, doSplitIslands);
  }

}
