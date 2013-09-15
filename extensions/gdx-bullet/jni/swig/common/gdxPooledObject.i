/** Creates a pool for the type and sets SWIG to use the pool instead of creating a new java object every time.
 * TODO: Add better pointer support
 * @author Xoppa */
%define CREATE_POOLED_OBJECT(_TYPE, _JCLASS)

// Add pooling to the java class
%typemap(javacode) _TYPE %{
	/** Temporary instance, use by native methods that return a _TYPE instance */
	protected final static _TYPE temp = new _TYPE(0, false);
	public static _TYPE internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	/** Pool of _TYPE instances, used by director interface to provide the arguments. */
	protected static final com.badlogic.gdx.utils.Pool<_TYPE> pool = new com.badlogic.gdx.utils.Pool<_TYPE>() {
		@Override
		protected _TYPE newObject() {
			return new _TYPE(0, false);
		}
	};
	/** Reuses a previous freed instance or creates a new instance and set it to reflect the specified native object */
	public static _TYPE obtain(long cPtr, boolean own) {
		final _TYPE result = pool.obtain();
		result.reset(cPtr, own);
		return result;
	}
	/** delete the native object if required and allow the instance to be reused by the obtain method */
	public static void free(final _TYPE inst) {
		inst.dispose();
		pool.free(inst);
	}
%}

%fragment(gdxToString(gdxPooled##_TYPE), "header") {
	// Inline (cached) method to retrieve the type's jclass
	SWIGINTERN inline jclass gdx_getClass##_TYPE(JNIEnv * jenv) {
		static jclass cls = NULL;
		if (cls == NULL)
			cls = (jclass) jenv->NewGlobalRef(jenv->FindClass(gdxToString(_JCLASS)));
		return cls;
	}
	
	// Inline method to get the termporary instance
	SWIGINTERN inline jobject gdx_getTemp##_TYPE(JNIEnv * jenv, void *cPtr, bool ownMem) {
	  static jobject ret = NULL;
	  static jclass clazz = gdx_getClass##_TYPE(jenv);
	  if (ret == NULL) {
	    jfieldID field = jenv->GetStaticFieldID(clazz, "temp", "_JCLASS");
	    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(clazz, field));
	  }
	  
	  static jmethodID reuseMethod = NULL;
	  if (reuseMethod == NULL)
		  reuseMethod = (jmethodID) jenv->GetMethodID(clazz, "reuse", "(JZ)V");
	  
	  long ptr;
	  *(const void **)&ptr = cPtr;
	  jenv->CallVoidMethod(ret, reuseMethod, ptr, (jboolean)ownMem);
	  return ret;
	}

	// Inline method to obtain an instance from the pool
	SWIGINTERN inline jobject gdx_obtain##_TYPE(JNIEnv * jenv, jclass clazz, void *cPtr, bool ownMem) {
		static jmethodID obtainMethod = NULL;
		if (obtainMethod == NULL)
			obtainMethod = (jmethodID) jenv->GetStaticMethodID(clazz, "obtain", gdxToString((JZ)L##_JCLASS##;));
		
		long ptr;
		*(const void **)&ptr = cPtr; 
		jobject ret = jenv->CallStaticObjectMethod(clazz, obtainMethod, ptr, (jboolean)ownMem);
		
		return ret;
	}
	
	// Inline method to free an instance from the pool
	SWIGINTERN inline void gdx_free##_TYPE(JNIEnv * jenv, const jclass clazz, const jobject obj) {
		static jmethodID freeMethod = NULL;
		if (freeMethod == NULL)
			freeMethod = (jmethodID) jenv->GetStaticMethodID(clazz, "free", gdxToString((L##_JCLASS##;)V));
		
		jenv->CallStaticVoidMethod(clazz, freeMethod, obj);
		
		jenv->DeleteLocalRef(obj);
	}
	
	// Simple raii class to auto free the instance from the pool 
	class gdxAutoFree##_TYPE {
	private:
		JNIEnv * jenv;
		jobject j##_TYPE;
		jclass jclazz;
	public:
		gdxAutoFree##_TYPE(JNIEnv * jenv, jclass jclazz, jobject j##_TYPE) : 
			jenv(jenv), j##_TYPE(j##_TYPE), jclazz(jclazz) { }
		virtual ~gdxAutoFree##_TYPE() {
			gdx_free##_TYPE(this->jenv, this->jclazz, this->j##_TYPE);
		}
	};
}

// The typemaps
%typemap(jni)				_TYPE *	"jlong"
%typemap(jtype)				_TYPE *	"long"
%typemap(jstype)			_TYPE * "_TYPE"
%typemap(jni) 				_TYPE, _TYPE &, const _TYPE &	"jobject"
%typemap(jstype) 			_TYPE, _TYPE &, const _TYPE &	"_TYPE"
%typemap(jtype) 			_TYPE, _TYPE &, const _TYPE &	"_TYPE"
%typemap(javain)			_TYPE, _TYPE &, const _TYPE &	"$javainput"
%typemap(javadirectorin)	_TYPE, _TYPE &, const _TYPE &	"$1"
%typemap(javadirectorout)	_TYPE, _TYPE &, const _TYPE &	"$javacall"

%typemap(directorin, fragment=gdxToString(gdxPooled##_TYPE), descriptor=gdxToString(L##_JCLASS;), noblock=1)	const _TYPE &  {
	jclass jc$1 = gdx_getClass##_TYPE(jenv);
	$input = gdx_obtain##_TYPE(jenv, jc$1, (void*)$1, false);
	gdxAutoFree##_TYPE autoRelease_$input(jenv, jc$1, $input);
}
%typemap(directorin, fragment=gdxToString(gdxPooled##_TYPE), descriptor=gdxToString(L##_JCLASS;), noblock=1)	_TYPE, _TYPE &  {
	jclass jc$1 = gdx_getClass##_TYPE(jenv);
	$input = gdx_obtain##_TYPE(jenv, jc$1, (void*)&$1, false);
	gdxAutoFree##_TYPE autoRelease_$input(jenv, jc$1, $input);
}
%typemap(out, fragment=gdxToString(gdxPooled##_TYPE), noblock=1)		_TYPE, _TYPE &, const _TYPE &	{
	$result = gdx_getTemp##_TYPE(jenv, &$1, false);
}
%typemap(javaout)		_TYPE, _TYPE &, const _TYPE &	{
	return $jnicall;
}
%typemap(javaout)		_TYPE * {
	return _TYPE.internalTemp($jnicall, false);
}

%enddef // CREATE_POOLED_OBJECT
