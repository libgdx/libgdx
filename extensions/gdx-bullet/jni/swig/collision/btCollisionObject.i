/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btCollisionObject

CREATE_MANAGED_OBJECT(btCollisionObject);

%typemap(javainterfaces) btCollisionObject %{
	com.badlogic.gdx.utils.Disposable
%}

%rename(internalSetCollisionShape) btCollisionObject::setCollisionShape;
%javamethodmodifiers btCollisionObject::setCollisionShape "private";
%rename(internalGetCollisionShape) btCollisionObject::getCollisionShape;
%javamethodmodifiers btCollisionObject::getCollisionShape "private";


%typemap(javabody) btCollisionObject %{
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected btCollisionObject(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
		gdxBridge = new GdxCollisionObjectBridge();
		internalSetGdxBridge(gdxBridge);
		addInstance(this);
	}
	
	protected void beforeDelete() {
		if (swigCPtr != 0)
			removeInstance(this);
		if (gdxBridge != null)
			gdxBridge.delete();
		gdxBridge = null;
	}
	
	protected GdxCollisionObjectBridge gdxBridge;
	protected int userValue;
	protected int contactCallbackFlag = 1;
	protected int contactCallbackFilter;
	protected btCollisionShape collisionShape;
	
	/** User definable data, not used by Bullet itself. */
	public Object userData;

	@Override
	public void dispose() {
		delete();
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
		collisionShape = shape;
		internalSetCollisionShape(shape);
	}
	
	public btCollisionShape getCollisionShape() {
		return collisionShape != null ? collisionShape : (collisionShape = internalGetCollisionShape()); 
	}
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObject.h>
#include <gdx/GdxCollisionObjectBridge.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionObject.h"
%include "gdx/GdxCollisionObjectBridge.h"

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