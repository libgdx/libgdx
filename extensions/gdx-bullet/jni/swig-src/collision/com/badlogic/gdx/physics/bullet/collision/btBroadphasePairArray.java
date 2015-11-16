/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
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

public class btBroadphasePairArray extends BulletBase {
	private long swigCPtr;
	
	protected btBroadphasePairArray(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btBroadphasePairArray, normally you should not need this constructor it's intended for low-level usage. */ 
	public btBroadphasePairArray(long cPtr, boolean cMemoryOwn) {
		this("btBroadphasePairArray", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btBroadphasePairArray obj) {
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
				CollisionJNI.delete_btBroadphasePairArray(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	/**
	 * @param out The array to fill with collision objects
	 * @param other The collision object the pair must contain (which itself is excluded from the result)
	 * @param tempArray A temporary array used by the method, not more object than the length of this array are added 
	 * @return The array specified by out */
	public com.badlogic.gdx.utils.Array<btCollisionObject> getCollisionObjects(final com.badlogic.gdx.utils.Array<btCollisionObject> out, final btCollisionObject other, final int[] tempArray) {
		final int c = getCollisionObjects(tempArray, tempArray.length, (int)btCollisionObject.getCPtr(other));
		for (int i = 0; i < c; i++)
			out.add(btCollisionObject.getInstance(tempArray[i], false));
		return out;
	}
	
	/** Fills the given array with user value set using {@link btCollisionObject#setUserValue(int)} of the collision objects
	 * within this pair array colliding with the given collision object.
	 * @param out The array to fill with the user values
	 * @param other The collision object the pair must contain (which itself is excluded from the result)
	 * @return The amount of user values set in the out array. */
	public int getCollisionObjectsValue(final int[] out, final btCollisionObject other) {
		return getCollisionObjectsValue(out, out.length, (int)btCollisionObject.getCPtr(other));
	}

  public int size() {
    return CollisionJNI.btBroadphasePairArray_size(swigCPtr, this);
  }

  public btBroadphasePair at(int n) {
	return CollisionJNI.btBroadphasePairArray_at(swigCPtr, this, n);
}

  public int getCollisionObjects(int[] result, int max, int other) {
    return CollisionJNI.btBroadphasePairArray_getCollisionObjects(swigCPtr, this, result, max, other);
  }

  public int getCollisionObjectsValue(int[] result, int max, int other) {
    return CollisionJNI.btBroadphasePairArray_getCollisionObjectsValue(swigCPtr, this, result, max, other);
  }

  public btBroadphasePairArray() {
    this(CollisionJNI.new_btBroadphasePairArray(), true);
  }

}
