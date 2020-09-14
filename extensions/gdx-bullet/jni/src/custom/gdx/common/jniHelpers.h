#ifndef jniHelpers_H
#define jniHelpers_H

#include <jni.h>

#ifndef GDXPOOL_POOLCLAZZ
#define GDXPOOL_POOLCLAZZ "Lcom/badlogic/gdx/utils/Pool;"
#endif

#ifndef GDXPOOL_OBTAINFUNC
#define GDXPOOL_OBTAINFUNC "obtain"
#endif

#ifndef GDXPOOL_OBTAINSIG
#define GDXPOOL_OBTAINSIG "()Ljava/lang/Object;"
#endif

#ifndef GDXPOOL_FREEFUNC
#define GDXPOOL_FREEFUNC "free"
#endif

#ifndef GDXPOOL_FREESIG
#define GDXPOOL_FREESIG "(Ljava/lang/Object;)V"
#endif


struct GdxPool {
	const char * const &poolField;
	const char * const &typeName;
	const char * const &tempField;
	const char * const &poolClazz;
	const char * const &obtainName;
	const char * const &obtainSig;
	const char * const &freeName;
	const char * const &freeSig;

	JNIEnv *env;
	jclass cls;
	jobject pool;
	jmethodID obtainMethod;
	jmethodID freeMethod;
	jobject tmp;

	/** eg: if you have in java:
	 * class CommonJNI { 
	 *		public static com.xxx.a.Pool<com.xxx.b.Clazz> poolClazz;
	 *		public static com.xxx.b.Clazz tempClazz;
	 * }
	 * then construct using:
	 * GdxPool(jenv, jclass("CommonJNI"), "poolClazz", "Lcom/xxx/b/Clazz;", "tempClazz"); */
	GdxPool(const char * const &poolField, const char * const &typeName = 0, const char * const &tempField = 0,
		const char * const &poolClazz = GDXPOOL_POOLCLAZZ,
		const char * const &obtainName = GDXPOOL_OBTAINFUNC, const char * const &obtainSig = GDXPOOL_OBTAINSIG,
		const char * const &freeName = GDXPOOL_FREEFUNC, const char * const &freeSig = GDXPOOL_FREESIG);
	virtual ~GdxPool();
	void setEnv(JNIEnv * const &env);
	/** Obtain a jobject from the pool */
	jobject obtain(JNIEnv * const &env);
	/** Free a jobject back to the pool */
	void free(jobject &obj);
	/** Get the temp instance (if available) */
	jobject temp(JNIEnv * const &e);
};

struct GdxPooledObject {
	GdxPool * const &pool;
	jobject obj;
	const bool autoFree;

	GdxPooledObject(JNIEnv * const &e, GdxPool * const &pool, const bool &autoFree);
	GdxPooledObject(JNIEnv * const &e, GdxPool * &pool, const bool &autoFree);
	virtual ~GdxPooledObject();

	void free();
};

#endif //jniHelpers_H