#include "jniHelpers.h"

GdxPool::GdxPool(const char * const &poolField, const char * const &typeName, const char * const &tempField,
		const char * const &poolClazz,
		const char * const &obtainName, const char * const &obtainSig,
		const char * const &freeName, const char * const &freeSig)
	: env(0), cls(0), pool(0), tmp(0), poolField(poolField), typeName(typeName), tempField(tempField), poolClazz(poolClazz), obtainName(obtainName), obtainSig(obtainSig), freeName(freeName), freeSig(freeSig)
{ }

GdxPool::~GdxPool() {
	if (pool)
		env->DeleteGlobalRef(pool);
	if (tmp)
		env->DeleteGlobalRef(tmp);
	if (cls)
		env->DeleteGlobalRef(cls);
}

void GdxPool::setEnv(JNIEnv * const &e) {
	env = e;
	cls = (jclass)env->NewGlobalRef(env->FindClass(typeName));
	jfieldID poolFieldID = env->GetStaticFieldID(cls, poolField, poolClazz);
	pool = env->NewGlobalRef(env->GetStaticObjectField(cls, poolFieldID));
	jclass poolClass = env->GetObjectClass(pool);
	obtainMethod = env->GetMethodID(poolClass, obtainName, obtainSig);
	freeMethod = env->GetMethodID(poolClass, freeName, freeSig);
	env->DeleteLocalRef(poolClass);

	if (typeName && tempField) {
		jfieldID tempFieldID = env->GetStaticFieldID(cls, tempField, typeName);
		tmp = env->NewGlobalRef(env->GetStaticObjectField(cls, tempFieldID));
	}
}

jobject GdxPool::obtain(JNIEnv * const &e) {
	if (!env) setEnv(e);
	jobject result = env->CallObjectMethod(pool, obtainMethod);
	return result;
}

void GdxPool::free(jobject &obj) {
	env->CallVoidMethod(pool, freeMethod, obj);
}

jobject GdxPool::temp(JNIEnv * const &e) {
	if (!env) setEnv(e);
	return tmp;
}




GdxPooledObject::GdxPooledObject(JNIEnv * const &e, GdxPool * const &pool, const bool &autoFree)
	: pool(pool), autoFree(autoFree), obj(pool->obtain(e)) {
}
GdxPooledObject::GdxPooledObject(JNIEnv * const &e, GdxPool * &pool, const bool &autoFree)
	: pool(pool), autoFree(autoFree), obj(pool->obtain(e)) {
}
GdxPooledObject::~GdxPooledObject() {
	if (autoFree)
		free();
}
void GdxPooledObject::free() {
	pool->free(obj);
}