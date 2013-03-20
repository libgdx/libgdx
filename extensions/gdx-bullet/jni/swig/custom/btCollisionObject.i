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
    if (gdxBridge != null) {
    	gdxBridge.delete();
    	gdxBridge = null;
    }
    if (swigCPtr != 0) {
      instances.remove(swigCPtr);
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
	
	public static btCollisionObject getInstance(final long swigCPtr, boolean owner) {
		btCollisionObject result = instances.get(swigCPtr);
		if (result == null)
			result = new btCollisionObject(swigCPtr, owner);
		return result;
	}
	
	private long swigCPtr;
	protected boolean swigCMemOwn;
	public GdxCollisionObjectBridge gdxBridge;
	
	public Object userData;
	
	protected btCollisionObject(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
		instances.put(cPtr, this);
		gdxBridge = new GdxCollisionObjectBridge();
		internalSetGdxBridge(gdxBridge);
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
	
	int getUserValue() {
		return ((GdxCollisionObjectBridge*)($self->getUserPointer()))->userValue;;
	}
	
	void setUserValue(int value) {
		((GdxCollisionObjectBridge*)($self->getUserPointer()))->userValue = value;
	}
};