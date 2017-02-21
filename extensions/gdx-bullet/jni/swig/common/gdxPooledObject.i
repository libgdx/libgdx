/** Creates a pool for the type and sets SWIG to use the pool instead of creating a new java object every time.
 * TODO: Add better pointer support
 * @author Xoppa */
%define CREATE_POOLED_OBJECT_EXT(CTYPE, JTYPE, _JCLASS)

// Add pooling to the java class
%typemap(javacode) CTYPE %{
	/** Temporary instance, use by native methods that return a JTYPE instance */
	protected final static JTYPE temp = new JTYPE(0, false);
	public static JTYPE internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	/** Pool of JTYPE instances, used by director interface to provide the arguments. */
	protected static final com.badlogic.gdx.utils.Pool<JTYPE> pool = new com.badlogic.gdx.utils.Pool<JTYPE>() {
		@Override
		protected JTYPE newObject() {
			return new JTYPE(0, false);
		}
	};
	/** Reuses a previous freed instance or creates a new instance and set it to reflect the specified native object */
	public static JTYPE obtain(long cPtr, boolean own) {
		final JTYPE result = pool.obtain();
		result.reset(cPtr, own);
		return result;
	}
	/** delete the native object if required and allow the instance to be reused by the obtain method */
	public static void free(final JTYPE inst) {
		inst.dispose();
		pool.free(inst);
	}
%}

%fragment(gdxToString(gdxPooled##JTYPE), "header") {
	// Inline (cached) method to retrieve the type's jclass
	SWIGINTERN inline jclass &gdx_getClass##JTYPE(JNIEnv * const &jenv) {
		static jclass cls = NULL;
		if (cls == NULL)
			cls = (jclass) jenv->NewGlobalRef(jenv->FindClass(gdxToString(_JCLASS)));
		return cls;
	}
	
	// Inline method to get the termporary instance
	SWIGINTERN inline jobject gdx_getTemp##JTYPE(JNIEnv * jenv, void *cPtr, bool ownMem) {
	  static jobject ret = NULL;
	  jclass &clazz = gdx_getClass##JTYPE(jenv);
	  if (ret == NULL) {
	    jfieldID field = jenv->GetStaticFieldID(clazz, "temp", gdxToString(L##_JCLASS##;));
	    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(clazz, field));
	  }
	  
	  static jmethodID reuseMethod = NULL;
	  if (reuseMethod == NULL)
		  reuseMethod = (jmethodID) jenv->GetMethodID(clazz, "reset", "(JZ)V");
	  
	  long ptr;
	  *(const void **)&ptr = cPtr;
	  jenv->CallVoidMethod(ret, reuseMethod, ptr, (jboolean)ownMem);
	  return ret;
	}

	// Inline method to obtain an instance from the pool
	SWIGINTERN inline jobject gdx_obtain##JTYPE(JNIEnv * jenv, jclass clazz, void *cPtr, bool ownMem) {
		static jmethodID obtainMethod = NULL;
		if (obtainMethod == NULL)
			obtainMethod = (jmethodID) jenv->GetStaticMethodID(clazz, "obtain", gdxToString((JZ)L##_JCLASS##;));
		
		long ptr;
		*(const void **)&ptr = cPtr; 
		jobject ret = jenv->CallStaticObjectMethod(clazz, obtainMethod, ptr, (jboolean)ownMem);
		
		return ret;
	}
	
	// Inline method to free an instance from the pool
	SWIGINTERN inline void gdx_free##JTYPE(JNIEnv * jenv, const jclass clazz, const jobject obj) {
		static jmethodID freeMethod = NULL;
		if (freeMethod == NULL)
			freeMethod = (jmethodID) jenv->GetStaticMethodID(clazz, "free", gdxToString((L##_JCLASS##;)V));
		
		jenv->CallStaticVoidMethod(clazz, freeMethod, obj);
		
		jenv->DeleteLocalRef(obj);
	}
	
	// Simple raii class to auto free the instance from the pool 
	class gdxAutoFree##JTYPE {
	private:
		JNIEnv * jenv;
		jobject j##JTYPE;
		jclass jclazz;
	public:
		gdxAutoFree##JTYPE(JNIEnv * jenv, jclass jclazz, jobject j##JTYPE) : 
			jenv(jenv), j##JTYPE(j##JTYPE), jclazz(jclazz) { }
		virtual ~gdxAutoFree##JTYPE() {
			gdx_free##JTYPE(this->jenv, this->jclazz, this->j##JTYPE);
		}
	};
}

// The typemaps
%typemap(jni)				CTYPE *	"jlong"
%typemap(jtype)				CTYPE *	"long"
%typemap(jstype)			CTYPE * "JTYPE"
%typemap(jni) 				CTYPE, CTYPE &, const CTYPE &	"jobject"
%typemap(jstype) 			CTYPE, CTYPE &, const CTYPE &	"JTYPE"
%typemap(jtype) 			CTYPE, CTYPE &, const CTYPE &	"JTYPE"
%typemap(javain)			CTYPE, CTYPE &, const CTYPE &	"$javainput"
%typemap(javadirectorin)	CTYPE, CTYPE &, const CTYPE &	"$1"
%typemap(javadirectorout)	CTYPE, CTYPE &, const CTYPE &	"$javacall"

//%typemap(directorin, fragment=gdxToString(gdxPooled##JTYPE), descriptor=gdxToString(L##_JCLASS;), noblock=1)	const CTYPE &  {
//	jclass jc$1 = gdx_getClass##JTYPE(jenv);
//	$input = gdx_obtain##JTYPE(jenv, jc$1, (void*)$1, false);
//	gdxAutoFree##JTYPE autoRelease_$input(jenv, jc$1, $input);
//}
%typemap(directorin, fragment=gdxToString(gdxPooled##JTYPE), descriptor=gdxToString(L##_JCLASS;), noblock=1)	CTYPE, CTYPE &, const CTYPE &  {
	jclass jc$1 = gdx_getClass##JTYPE(jenv);
	$input = gdx_obtain##JTYPE(jenv, jc$1, (void*)&$1, false);
	gdxAutoFree##JTYPE autoRelease_$input(jenv, jc$1, $input);
}
%typemap(out, fragment=gdxToString(gdxPooled##JTYPE), noblock=1)		CTYPE, CTYPE &, const CTYPE &	{
	$result = gdx_getTemp##JTYPE(jenv, &$1, false);
}
%typemap(javaout)		CTYPE, CTYPE &, const CTYPE &	{
	return $jnicall;
}
%typemap(javaout)		CTYPE * {
	return JTYPE.internalTemp($jnicall, false);
}

%enddef // CREATE_POOLED_OBJECT_EXT

%define CREATE_POOLED_OBJECT(_TYPE, _JCLASS)
CREATE_POOLED_OBJECT_EXT(_TYPE, _TYPE, _JCLASS)
%enddef
