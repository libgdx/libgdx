/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btCollisionObject

%typemap(javaout) 	btCollisionObject *, const btCollisionObject * {
	long cPtr = $jnicall;
	return (cPtr == 0) ? null : btCollisionObject.getInstance(cPtr, $owner);
}

%typemap(javainterfaces) btCollisionObject %{
	com.badlogic.gdx.utils.Disposable
%}

%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") btCollisionObject %{ {
    if (swigCPtr != 0)
      removeInstance(this);
	if (gdxBridge != null) {
    	gdxBridge.delete();
    	gdxBridge = null;
    }
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        gdxBulletJNI.delete_btCollisionObject(swigCPtr);
      }
      swigCPtr = 0;
    }
  }
%}

%typemap(javabody) btCollisionObject %{
	public final static com.badlogic.gdx.utils.LongMap<btCollisionObject> instances = new com.badlogic.gdx.utils.LongMap<btCollisionObject>();
	
	public static btCollisionObject getInstance(final long swigCPtr) {
		return instances.get(swigCPtr);
	}
	
	public static btCollisionObject getInstance(final long swigCPtr, boolean owner) {
		btCollisionObject result = getInstance(swigCPtr);
		if (result == null)
			result = new btCollisionObject(swigCPtr, owner);
		return result;
	}
	
	public static void addInstance(final btCollisionObject obj) {
		instances.put(getCPtr(obj), obj);
	}
	
	public static void removeInstance(final btCollisionObject obj) {
		instances.remove(getCPtr(obj));
	}
	
	private long swigCPtr;
	protected boolean swigCMemOwn;
	protected GdxCollisionObjectBridge gdxBridge;
	protected int userValue;
	protected int contactCallbackFlag = 1;
	protected int contactCallbackFilter;
	
	public Object userData;
	
	protected btCollisionObject(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
		gdxBridge = new GdxCollisionObjectBridge();
		internalSetGdxBridge(gdxBridge);
		addInstance(this);
	}
	
	@Override
	public void dispose() {
		delete();
	}
	
	public void takeOwnership() {
		swigCMemOwn = true;
	}
	
	public void releaseOwnership() {
		swigCMemOwn = false;
	}
	
	public static long getCPtr($javaclassname obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
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
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObject.h>
#include <GdxCustom/GdxCollisionObjectBridge.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionObject.h"
%include "GdxCustom/GdxCollisionObjectBridge.h"

%extend btCollisionObject {
	void internalSetGdxBridge(GdxCollisionObjectBridge *bridge) {
		$self->setUserPointer(bridge);
	}
	
	GdxCollisionObjectBridge *internalGetGdxBridge() {
		return (GdxCollisionObjectBridge *)($self->getUserPointer());
	}

	void getAnisotropicFriction(btVector3 & out) {
		out = $self->getAnisotropicFriction();
	}

    void getWorldTransform(btTransform & out) {
		out = $self->getWorldTransform();
	}
	
    void getInterpolationWorldTransform(btTransform & out) {
		out = $self->getInterpolationWorldTransform();
	}
	
	void getInterpolationLinearVelocity(btVector3 & out) {
		out = $self->getInterpolationLinearVelocity();
	}
		
	void getInterpolationAngularVelocity(btVector3 & out) {
		out = $self->getInterpolationAngularVelocity();
	}
};