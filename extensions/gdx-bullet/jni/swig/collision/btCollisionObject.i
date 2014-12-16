%module btCollisionObject

%typemap(javainterfaces) btCollisionObject %{
	com.badlogic.gdx.utils.Disposable
%}

%rename(internalSetCollisionShape) btCollisionObject::setCollisionShape;
%javamethodmodifiers btCollisionObject::setCollisionShape "private";
%rename(internalGetCollisionShape) btCollisionObject::getCollisionShape;
%javamethodmodifiers btCollisionObject::getCollisionShape "private";

%typemap(javaout) 	btCollisionObject *, const btCollisionObject *, btCollisionObject * const & {
	return btCollisionObject.getInstance($jnicall, $owner);
}

%typemap(javaout) 	btCollisionObject, const btCollisionObject, btCollisionObject & {
	return btCollisionObject.getInstance($jnicall, $owner);
}

%typemap(javadirectorin) btCollisionObject *, const btCollisionObject *, btCollisionObject * const &	"btCollisionObject.getInstance($1, false)"

%typemap(javacode) btCollisionObject %{
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
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObject.h>
#include <gdx/collision/GdxCollisionObjectBridge.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionObject.h"
%include "gdx/collision/GdxCollisionObjectBridge.h"

%javamethodmodifiers btCollisionObject::internalSetGdxBridge "private";
%javamethodmodifiers btCollisionObject::internalGetGdxBridge "private";

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