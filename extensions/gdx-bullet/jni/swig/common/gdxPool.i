/* Provides com.badlogic.gdx.utils.Pool, to use:"
 * 1. Create a Pool, ie:
 * %pragma(java) jniclasscode=%{
 *	public static final Pool<SomeJavaClass> SomeJavaClassPool = new Pool<SomeJavaClass>() {
 * 		@Override
 *		protected SomeJavaClass newObject() {
 *			return new SomeJavaClass();
 *		}
 *	};
 * }
 * 2. Obtain a pooled object, ie:
 * %typemap(...) ... {
 * 		somevar = gdx_takePoolObject(jenv, "SomeJavaClassPool");
 * }
 * 3. Release a pooled object, ie:
 * gdx_releasePoolObject(jenv, "SomeJavaClassPool", somevar);
 * 4. Or let it be released when out of scope, ie:
 * gdxPoolAutoRelease autoRelease_somevar(jenv, "SomeJavaClassPool", somevar);
 */
%define CREATE_POOLED_METHODS(JTYPE, CLAZZ)
%fragment("gdxPoolMethods##JTYPE", "header") {

	/* Gets a global ref to the temp class.  Do not release this. */
	SWIGINTERN inline jclass gdx_getTempClass##JTYPE(JNIEnv * jenv) {
	  static jclass cls = NULL;
	  if (cls == NULL) {
		cls = (jclass) jenv->NewGlobalRef(jenv->FindClass(CLAZZ));
	  }
	  return cls;
	}
	
	SWIGINTERN inline jobject gdx_takePoolObject##JTYPE(JNIEnv * jenv, const char * poolName) {
	  jclass tempClass = gdx_getTempClass##JTYPE(jenv);
	  
	  static jfieldID poolField = NULL;
	  if (poolField == NULL) {
		poolField = jenv->GetStaticFieldID(tempClass, poolName, "Lcom/badlogic/gdx/utils/Pool;");
	  }
	  
	  jobject poolObject = jenv->GetStaticObjectField(tempClass, poolField);
	  jclass poolClass = jenv->GetObjectClass(poolObject);
	  
	  static jmethodID obtainMethod = NULL;
	  if (obtainMethod == NULL) {
		obtainMethod = (jmethodID) jenv->GetMethodID(poolClass, "obtain", "()Ljava/lang/Object;");
	  }
	  
	  jobject ret = jenv->CallObjectMethod(poolObject, obtainMethod);
	
	  jenv->DeleteLocalRef(poolObject);
	  jenv->DeleteLocalRef(poolClass);
	
	  return ret;
	}
	
	SWIGINTERN inline void gdx_releasePoolObject##JTYPE(JNIEnv * jenv, const char * poolName, jobject obj) {
	  jclass tempClass = gdx_getTempClass##JTYPE(jenv);
	  
	  static jfieldID poolField = NULL;
	  if (poolField == NULL) {
		poolField = jenv->GetStaticFieldID(tempClass, poolName, "Lcom/badlogic/gdx/utils/Pool;");
	  }
	  
	  jobject poolObject = jenv->GetStaticObjectField(tempClass, poolField);
	  jclass poolClass = jenv->GetObjectClass(poolObject);
	  
	  static jmethodID freeMethod = NULL;
	  if (freeMethod == NULL) {
		freeMethod = (jmethodID) jenv->GetMethodID(poolClass, "free", "(Ljava/lang/Object;)V");
	  }
	  
	  jenv->CallVoidMethod(poolObject, freeMethod, obj);
	  
	  jenv->DeleteLocalRef(poolObject);
	  jenv->DeleteLocalRef(poolClass);
	  jenv->DeleteLocalRef(obj);
	}
	
	/*
	 * A simple RAII wrapper to release jobjects we obtain from pools in 
	 * directorin typemaps.  SWIG doesn't have hooks to release them after
	 * they're used. 
	 */
	class gdxPoolAutoRelease##JTYPE {
	private:
	  JNIEnv * jenv;
	  const char * poolName;
	  jobject obj;
	public:
	  gdxPoolAutoRelease##JTYPE(JNIEnv * jenv, const char * poolName, jobject obj) : 
		jenv(jenv), poolName(poolName), obj(obj) { };
	  virtual ~gdxPoolAutoRelease##JTYPE() {
		gdx_releasePoolObject##JTYPE(this->jenv, this->poolName, this->obj);
	  };
	};
}
%enddef