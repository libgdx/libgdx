/** Managed objects are java objects of which the reference is maintained by the wrapper and the instance is reused
 * by using a LongMap lookup with the native pointer.
 * 
 * Use managed objects for classes which are likely to be created by the user and are expected to live long. 
 * @author Xoppa */
%define CREATE_MANAGED_OBJECT(_TYPE)

%typemap(javaout) 	_TYPE *, const _TYPE *, _TYPE * const & {
	return _TYPE.getInstance($jnicall, $owner);
}

%typemap(javadirectorin) _TYPE *, const _TYPE *, _TYPE * const &	"_TYPE.getInstance($1, false)"

%typemap(javadestruct, methodname="delete", methodmodifiers="public synchronized") _TYPE %{ {
	beforeDelete();
	if (swigCPtr != 0) {
		if (swigCMemOwn) {
			swigCMemOwn = false;
			gdxBulletJNI.delete_##_TYPE(swigCPtr);
		}
		swigCPtr = 0;
	}
} %}

%typemap(javabody) _TYPE %{
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected _TYPE(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
		addInstance(this);
	}
	
	/** Gets call before this object is deleted, override to add custom implementation. */ 
	protected void beforeDelete() {
		if (swigCPtr != 0)
			removeInstance(this);
	}
%}
	
%typemap(javacode) _TYPE %{
	/** Provides direct access to the instances this wrapper managed. */
	public final static com.badlogic.gdx.utils.LongMap<_TYPE> instances = new com.badlogic.gdx.utils.LongMap<_TYPE>();
	
	/** @return The existing instance for the specified pointer, or null if the instance doesn't exist */
	public static _TYPE getInstance(final long swigCPtr) {
		return swigCPtr == 0 ? null : instances.get(swigCPtr);
	}
	
	/** @return The existing isntance for the specified pointer, or a newly created instance if the instance didn't exist */
	public static _TYPE getInstance(final long swigCPtr, boolean owner) {
		if (swigCPtr == 0)
			return null;
		_TYPE result = instances.get(swigCPtr);
		if (result == null)
			result = new _TYPE(swigCPtr, owner);
		return result;
	}
	
	/** Add the instance to the managed instances.
	 * You should avoid using this method. This method is intended for internal purposes only. */
	public static void addInstance(final _TYPE obj) {
		instances.put(getCPtr(obj), obj);
	}
	
	/** Remove the instance to the managed instances.
	 * Be careful using this method. This method is intended for internal purposes only. */	
	public static void removeInstance(final _TYPE obj) {
		instances.remove(getCPtr(obj));
	}

	/** Take ownership of the native instance, causing the native object to be deleted when this object gets out of scope. */
	public void takeOwnership() {
		swigCMemOwn = true;
	}
	
	/** Release ownership of the native instance, causing the native object NOT to be deleted when this object gets out of scope. */
	public void releaseOwnership() {
		swigCMemOwn = false;
	}
	
	/** @return True if the native is destroyed when this object gets out of scope, false otherwise. */
	public boolean hasOwnership() {
		return swigCMemOwn;
	}
	
	/** @return The value of the pointer to the native instance this object represents. */
	public static long getCPtr($javaclassname obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
%}

%enddef // CREATE_MANAGED_OBJECT