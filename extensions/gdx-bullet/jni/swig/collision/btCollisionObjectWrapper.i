%module btCollisionObjectWrapper

CREATE_POOLED_OBJECT(btCollisionObjectWrapper, com/badlogic/gdx/physics/bullet/btCollisionObjectWrapper);

%nodefaultdtor btCollisionObjectWrapper;

%typemap(javacode) btCollisionObjectWrapper %{
	/** Temporary instance, use by native methods that return a btCollisionObjectWrapper instance */
	protected final static btCollisionObjectWrapper temp = new btCollisionObjectWrapper(0, false);
	public static btCollisionObjectWrapper internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	/** Pool of btCollisionObjectWrapper instances, used by director interface to provide the arguments. */
	protected static final com.badlogic.gdx.utils.Pool<btCollisionObjectWrapper> pool = new com.badlogic.gdx.utils.Pool<btCollisionObjectWrapper>() {
		@Override
		protected btCollisionObjectWrapper newObject() {
			return new btCollisionObjectWrapper(0, false);
		}
	};
	/** Reuses a previous freed instance or creates a new instance and set it to reflect the specified native object */
	public static btCollisionObjectWrapper obtain(long cPtr, boolean own) {
		final btCollisionObjectWrapper result = pool.obtain();
		result.reset(cPtr, own);
		return result;
	}
	/** delete the native object if required and allow the instance to be reused by the obtain method */
	public static void free(final btCollisionObjectWrapper inst) {
		inst.dispose();
		pool.free(inst);
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h>
%}

%ignore btCollisionObjectWrapper::getWorldTransform;
%ignore btCollisionObjectWrapper::getCollisionObject;

%include "BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"