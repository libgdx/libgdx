%module btCollisionObjectWrapper

%typemap(javadirectorin) btCollisionObjectWrapper, const btCollisionObjectWrapper, const btCollisionObjectWrapper &, btCollisionObjectWrapper & 	"btCollisionObjectWrapper.obtainForArgument($1, false)"
%typemap(javadirectorin) btCollisionObjectWrapper *, const btCollisionObjectWrapper *, btCollisionObjectWrapper * const &	"btCollisionObjectWrapper.obtainForArgument($1, false)"

%typemap(javaout) 	btCollisionObjectWrapper *, const btCollisionObjectWrapper *, btCollisionObjectWrapper * const & {
	return btCollisionObjectWrapper.internalTemp($jnicall, $owner);
}

%nodefaultdtor btCollisionObjectWrapper;

%typemap(javacode) btCollisionObjectWrapper %{
	/** Temporary instance, use by native methods that return a btCollisionObjectWrapper instance */
	protected final static btCollisionObjectWrapper temp = new btCollisionObjectWrapper(0, false);
	public static btCollisionObjectWrapper internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}

	private static btCollisionObjectWrapper[] argumentInstances = new btCollisionObjectWrapper[] {new btCollisionObjectWrapper(0, false),
		new btCollisionObjectWrapper(0, false), new btCollisionObjectWrapper(0, false), new btCollisionObjectWrapper(0, false)};
	private static int argumentIndex = -1;
	/** Obtains a temporary instance, used for callback methods with one or more btManifoldPoint arguments */
	protected static btCollisionObjectWrapper obtainForArgument(final long swigCPtr, boolean owner) {
		btCollisionObjectWrapper instance = argumentInstances[argumentIndex = (argumentIndex + 1) & 3];
		instance.reset(swigCPtr, owner);
		return instance;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}
%}

%javamethodmodifiers CollisionObjectWrapper::getWrapper "private";

%typemap(javacode) CollisionObjectWrapper %{
	public btCollisionObjectWrapper wrapper;
	
	@Override
	protected void construct() {
		super.construct();
		wrapper = new btCollisionObjectWrapper(getWrapper().getCPointer(), false);
	}

	@Override
	public void dispose() {
		if (wrapper != null) {
			wrapper.dispose();
			wrapper = null;
		}
		super.dispose();
	}
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h>
#include <gdx/collision/CollisionObjectWrapper.h>
%}

%ignore btCollisionObjectWrapper::getWorldTransform;
%ignore btCollisionObjectWrapper::getCollisionObject;

%include "BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"
%include "gdx/collision/CollisionObjectWrapper.h"