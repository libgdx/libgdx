/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btCollisionObject extends BulletBase implements 
	com.badlogic.gdx.utils.Disposable
 {
	private long swigCPtr;
	
	protected btCollisionObject(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected btCollisionObject(long cPtr, boolean cMemoryOwn) {
		this("btCollisionObject", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btCollisionObject obj) {
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
				gdxBulletJNI.delete_btCollisionObject(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	/** Provides direct access to the instances this wrapper managed. */
	public final static com.badlogic.gdx.utils.LongMap<btCollisionObject> instances = new com.badlogic.gdx.utils.LongMap<btCollisionObject>();
	
	/** @return The existing instance for the specified pointer, or null if the instance doesn't exist */
	public static btCollisionObject getInstance(final long swigCPtr) {
		return swigCPtr == 0 ? null : instances.get(swigCPtr);
	}
	
	/** @return The existing instance for the specified pointer, or a newly created instance if the instance didn't exist */
	public static btCollisionObject getInstance(final long swigCPtr, boolean owner) {
		if (swigCPtr == 0)
			return null;
		btCollisionObject result = instances.get(swigCPtr);
		if (result == null)
			result = new btCollisionObject(swigCPtr, owner);
		return result;
	}
	
	/** Add the instance to the managed instances.
	 * You should avoid using this method. This method is intended for internal purposes only. */
	public static void addInstance(final btCollisionObject obj) {
		instances.put(getCPtr(obj), obj);
	}
	
	/** Remove the instance to the managed instances.
	 * Be careful using this method. This method is intended for internal purposes only. */	
	public static void removeInstance(final btCollisionObject obj) {
		instances.remove(getCPtr(obj));
	}
	
	protected GdxCollisionObjectBridge gdxBridge;
	protected int userValue;
	protected int contactCallbackFlag = 1;
	protected int contactCallbackFilter;
	protected btCollisionShape collisionShape;
	
	/** User definable data, not used by Bullet itself. */
	public Object userData;
	
	@Override
	protected void construct() {
		super.construct();
		gdxBridge = new GdxCollisionObjectBridge();
		internalSetGdxBridge(gdxBridge);
		addInstance(this);
	}

	@Override
	public void dispose() {
		if (swigCPtr != 0)
			removeInstance(this);
		if (gdxBridge != null)
			gdxBridge.dispose();
		gdxBridge = null;
		if (collisionShape != null)
			collisionShape.release();
		collisionShape = null;
		super.dispose();
	}

	/** @return A user definable value set using {@link #setUserValue(int)}, intended to quickly identify the collision object */ 
	public int getUserValue() {
		return userValue;
	}
	
	/** @param value A user definable value which allows you to quickly identify this collision object. Some frequently called
	 * methods rather return this value than the collision object itself to minimize JNI overhead. */
	public void setUserValue(int value) {
		gdxBridge.setUserValue(userValue = value);
	}
	
	/** @return The flag (defaults to 1) used to filter contact callbacks with this object */
	public int getContactCallbackFlag() {
		return contactCallbackFlag;
	}
	
	/** @param flag The new flag used to filter contact callbacks with this object */
	public void setContactCallbackFlag(int flag) {
		gdxBridge.setContactCallbackFlag(contactCallbackFlag = flag);
	}
	
	/** @return The filter (default to 0) that is used to match the flag of the other object for a contact callback to be triggered */
	public int getContactCallbackFilter() {
		return contactCallbackFilter;
	}
	
	/** @param filter The new filter that is used to match the flag of the other object for a contact callback to be triggered */
	public void setContactCallbackFilter(int filter) {
		gdxBridge.setContactCallbackFilter(contactCallbackFilter = filter);
	}
	
	public void setCollisionShape(btCollisionShape shape) {
		refCollisionShape(shape);
		internalSetCollisionShape(shape);
	}
	
	protected void refCollisionShape(btCollisionShape shape) {
		if (collisionShape == shape)
			return;
		if (collisionShape != null)
			collisionShape.release();
		collisionShape = shape;
		collisionShape.obtain();
	}
	
	public btCollisionShape getCollisionShape() {
		return collisionShape; 
	}

  public boolean mergesSimulationIslands() {
    return gdxBulletJNI.btCollisionObject_mergesSimulationIslands(swigCPtr, this);
  }

  public Vector3 getAnisotropicFriction() {
	return gdxBulletJNI.btCollisionObject_getAnisotropicFriction__SWIG_0(swigCPtr, this);
}

  public void setAnisotropicFriction(Vector3 anisotropicFriction, int frictionMode) {
    gdxBulletJNI.btCollisionObject_setAnisotropicFriction__SWIG_0(swigCPtr, this, anisotropicFriction, frictionMode);
  }

  public void setAnisotropicFriction(Vector3 anisotropicFriction) {
    gdxBulletJNI.btCollisionObject_setAnisotropicFriction__SWIG_1(swigCPtr, this, anisotropicFriction);
  }

  public boolean hasAnisotropicFriction(int frictionMode) {
    return gdxBulletJNI.btCollisionObject_hasAnisotropicFriction__SWIG_0(swigCPtr, this, frictionMode);
  }

  public boolean hasAnisotropicFriction() {
    return gdxBulletJNI.btCollisionObject_hasAnisotropicFriction__SWIG_1(swigCPtr, this);
  }

  public void setContactProcessingThreshold(float contactProcessingThreshold) {
    gdxBulletJNI.btCollisionObject_setContactProcessingThreshold(swigCPtr, this, contactProcessingThreshold);
  }

  public float getContactProcessingThreshold() {
    return gdxBulletJNI.btCollisionObject_getContactProcessingThreshold(swigCPtr, this);
  }

  public boolean isStaticObject() {
    return gdxBulletJNI.btCollisionObject_isStaticObject(swigCPtr, this);
  }

  public boolean isKinematicObject() {
    return gdxBulletJNI.btCollisionObject_isKinematicObject(swigCPtr, this);
  }

  public boolean isStaticOrKinematicObject() {
    return gdxBulletJNI.btCollisionObject_isStaticOrKinematicObject(swigCPtr, this);
  }

  public boolean hasContactResponse() {
    return gdxBulletJNI.btCollisionObject_hasContactResponse(swigCPtr, this);
  }

  public btCollisionObject() {
    this(gdxBulletJNI.new_btCollisionObject(), true);
  }

  private void internalSetCollisionShape(btCollisionShape collisionShape) {
    gdxBulletJNI.btCollisionObject_internalSetCollisionShape(swigCPtr, this, btCollisionShape.getCPtr(collisionShape), collisionShape);
  }

  private btCollisionShape internalGetCollisionShape() {
    long cPtr = gdxBulletJNI.btCollisionObject_internalGetCollisionShape__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : btCollisionShape.newDerivedObject(cPtr, false);
  }

  public SWIGTYPE_p_void internalGetExtensionPointer() {
    long cPtr = gdxBulletJNI.btCollisionObject_internalGetExtensionPointer(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void internalSetExtensionPointer(SWIGTYPE_p_void pointer) {
    gdxBulletJNI.btCollisionObject_internalSetExtensionPointer(swigCPtr, this, SWIGTYPE_p_void.getCPtr(pointer));
  }

  public int getActivationState() {
    return gdxBulletJNI.btCollisionObject_getActivationState(swigCPtr, this);
  }

  public void setActivationState(int newState) {
    gdxBulletJNI.btCollisionObject_setActivationState(swigCPtr, this, newState);
  }

  public void setDeactivationTime(float time) {
    gdxBulletJNI.btCollisionObject_setDeactivationTime(swigCPtr, this, time);
  }

  public float getDeactivationTime() {
    return gdxBulletJNI.btCollisionObject_getDeactivationTime(swigCPtr, this);
  }

  public void forceActivationState(int newState) {
    gdxBulletJNI.btCollisionObject_forceActivationState(swigCPtr, this, newState);
  }

  public void activate(boolean forceActivation) {
    gdxBulletJNI.btCollisionObject_activate__SWIG_0(swigCPtr, this, forceActivation);
  }

  public void activate() {
    gdxBulletJNI.btCollisionObject_activate__SWIG_1(swigCPtr, this);
  }

  public boolean isActive() {
    return gdxBulletJNI.btCollisionObject_isActive(swigCPtr, this);
  }

  public void setRestitution(float rest) {
    gdxBulletJNI.btCollisionObject_setRestitution(swigCPtr, this, rest);
  }

  public float getRestitution() {
    return gdxBulletJNI.btCollisionObject_getRestitution(swigCPtr, this);
  }

  public void setFriction(float frict) {
    gdxBulletJNI.btCollisionObject_setFriction(swigCPtr, this, frict);
  }

  public float getFriction() {
    return gdxBulletJNI.btCollisionObject_getFriction(swigCPtr, this);
  }

  public void setRollingFriction(float frict) {
    gdxBulletJNI.btCollisionObject_setRollingFriction(swigCPtr, this, frict);
  }

  public float getRollingFriction() {
    return gdxBulletJNI.btCollisionObject_getRollingFriction(swigCPtr, this);
  }

  public int getInternalType() {
    return gdxBulletJNI.btCollisionObject_getInternalType(swigCPtr, this);
  }

  public Matrix4 getWorldTransform() {
	return gdxBulletJNI.btCollisionObject_getWorldTransform__SWIG_0(swigCPtr, this);
}

  public void setWorldTransform(Matrix4 worldTrans) {
    gdxBulletJNI.btCollisionObject_setWorldTransform(swigCPtr, this, worldTrans);
  }

  public btBroadphaseProxy getBroadphaseHandle() {
    long cPtr = gdxBulletJNI.btCollisionObject_getBroadphaseHandle__SWIG_0(swigCPtr, this);
    return (cPtr == 0) ? null : new btBroadphaseProxy(cPtr, false);
  }

  public void setBroadphaseHandle(btBroadphaseProxy handle) {
    gdxBulletJNI.btCollisionObject_setBroadphaseHandle(swigCPtr, this, btBroadphaseProxy.getCPtr(handle), handle);
  }

  public Matrix4 getInterpolationWorldTransform() {
	return gdxBulletJNI.btCollisionObject_getInterpolationWorldTransform__SWIG_0(swigCPtr, this);
}

  public void setInterpolationWorldTransform(Matrix4 trans) {
    gdxBulletJNI.btCollisionObject_setInterpolationWorldTransform(swigCPtr, this, trans);
  }

  public void setInterpolationLinearVelocity(Vector3 linvel) {
    gdxBulletJNI.btCollisionObject_setInterpolationLinearVelocity(swigCPtr, this, linvel);
  }

  public void setInterpolationAngularVelocity(Vector3 angvel) {
    gdxBulletJNI.btCollisionObject_setInterpolationAngularVelocity(swigCPtr, this, angvel);
  }

  public Vector3 getInterpolationLinearVelocity() {
	return gdxBulletJNI.btCollisionObject_getInterpolationLinearVelocity__SWIG_0(swigCPtr, this);
}

  public Vector3 getInterpolationAngularVelocity() {
	return gdxBulletJNI.btCollisionObject_getInterpolationAngularVelocity__SWIG_0(swigCPtr, this);
}

  public int getIslandTag() {
    return gdxBulletJNI.btCollisionObject_getIslandTag(swigCPtr, this);
  }

  public void setIslandTag(int tag) {
    gdxBulletJNI.btCollisionObject_setIslandTag(swigCPtr, this, tag);
  }

  public int getCompanionId() {
    return gdxBulletJNI.btCollisionObject_getCompanionId(swigCPtr, this);
  }

  public void setCompanionId(int id) {
    gdxBulletJNI.btCollisionObject_setCompanionId(swigCPtr, this, id);
  }

  public float getHitFraction() {
    return gdxBulletJNI.btCollisionObject_getHitFraction(swigCPtr, this);
  }

  public void setHitFraction(float hitFraction) {
    gdxBulletJNI.btCollisionObject_setHitFraction(swigCPtr, this, hitFraction);
  }

  public int getCollisionFlags() {
    return gdxBulletJNI.btCollisionObject_getCollisionFlags(swigCPtr, this);
  }

  public void setCollisionFlags(int flags) {
    gdxBulletJNI.btCollisionObject_setCollisionFlags(swigCPtr, this, flags);
  }

  public float getCcdSweptSphereRadius() {
    return gdxBulletJNI.btCollisionObject_getCcdSweptSphereRadius(swigCPtr, this);
  }

  public void setCcdSweptSphereRadius(float radius) {
    gdxBulletJNI.btCollisionObject_setCcdSweptSphereRadius(swigCPtr, this, radius);
  }

  public float getCcdMotionThreshold() {
    return gdxBulletJNI.btCollisionObject_getCcdMotionThreshold(swigCPtr, this);
  }

  public float getCcdSquareMotionThreshold() {
    return gdxBulletJNI.btCollisionObject_getCcdSquareMotionThreshold(swigCPtr, this);
  }

  public void setCcdMotionThreshold(float ccdMotionThreshold) {
    gdxBulletJNI.btCollisionObject_setCcdMotionThreshold(swigCPtr, this, ccdMotionThreshold);
  }

  public SWIGTYPE_p_void getUserPointer() {
    long cPtr = gdxBulletJNI.btCollisionObject_getUserPointer(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void setUserPointer(SWIGTYPE_p_void userPointer) {
    gdxBulletJNI.btCollisionObject_setUserPointer(swigCPtr, this, SWIGTYPE_p_void.getCPtr(userPointer));
  }

  public boolean checkCollideWith(btCollisionObject co) {
    return gdxBulletJNI.btCollisionObject_checkCollideWith(swigCPtr, this, btCollisionObject.getCPtr(co), co);
  }

  public int calculateSerializeBufferSize() {
    return gdxBulletJNI.btCollisionObject_calculateSerializeBufferSize(swigCPtr, this);
  }

  public String serialize(SWIGTYPE_p_void dataBuffer, SWIGTYPE_p_btSerializer serializer) {
    return gdxBulletJNI.btCollisionObject_serialize(swigCPtr, this, SWIGTYPE_p_void.getCPtr(dataBuffer), SWIGTYPE_p_btSerializer.getCPtr(serializer));
  }

  public void serializeSingleObject(SWIGTYPE_p_btSerializer serializer) {
    gdxBulletJNI.btCollisionObject_serializeSingleObject(swigCPtr, this, SWIGTYPE_p_btSerializer.getCPtr(serializer));
  }

  private void internalSetGdxBridge(GdxCollisionObjectBridge bridge) {
    gdxBulletJNI.btCollisionObject_internalSetGdxBridge(swigCPtr, this, GdxCollisionObjectBridge.getCPtr(bridge), bridge);
  }

  private GdxCollisionObjectBridge internalGetGdxBridge() {
    long cPtr = gdxBulletJNI.btCollisionObject_internalGetGdxBridge(swigCPtr, this);
    return (cPtr == 0) ? null : new GdxCollisionObjectBridge(cPtr, false);
  }

  public void getAnisotropicFriction(Vector3 out) {
    gdxBulletJNI.btCollisionObject_getAnisotropicFriction__SWIG_1(swigCPtr, this, out);
  }

  public void getWorldTransform(Matrix4 out) {
    gdxBulletJNI.btCollisionObject_getWorldTransform__SWIG_2(swigCPtr, this, out);
  }

  public void getInterpolationWorldTransform(Matrix4 out) {
    gdxBulletJNI.btCollisionObject_getInterpolationWorldTransform__SWIG_2(swigCPtr, this, out);
  }

  public void getInterpolationLinearVelocity(Vector3 out) {
    gdxBulletJNI.btCollisionObject_getInterpolationLinearVelocity__SWIG_1(swigCPtr, this, out);
  }

  public void getInterpolationAngularVelocity(Vector3 out) {
    gdxBulletJNI.btCollisionObject_getInterpolationAngularVelocity__SWIG_1(swigCPtr, this, out);
  }

  public final static class CollisionFlags {
    public final static int CF_STATIC_OBJECT = 1;
    public final static int CF_KINEMATIC_OBJECT = 2;
    public final static int CF_NO_CONTACT_RESPONSE = 4;
    public final static int CF_CUSTOM_MATERIAL_CALLBACK = 8;
    public final static int CF_CHARACTER_OBJECT = 16;
    public final static int CF_DISABLE_VISUALIZE_OBJECT = 32;
    public final static int CF_DISABLE_SPU_COLLISION_PROCESSING = 64;
  }

  public final static class CollisionObjectTypes {
    public final static int CO_COLLISION_OBJECT = 1;
    public final static int CO_RIGID_BODY = 2;
    public final static int CO_GHOST_OBJECT = 4;
    public final static int CO_SOFT_BODY = 8;
    public final static int CO_HF_FLUID = 16;
    public final static int CO_USER_TYPE = 32;
  }

  public final static class AnisotropicFrictionFlags {
    public final static int CF_ANISOTROPIC_FRICTION_DISABLED = 0;
    public final static int CF_ANISOTROPIC_FRICTION = 1;
    public final static int CF_ANISOTROPIC_ROLLING_FRICTION = 2;
  }

}
