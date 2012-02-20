/*
 * Use some libgdx types instead of Bullet types.
 */
 
%{
#include <LinearMath/btVector3.h>
#include <LinearMath/btQuaternion.h>
#include <LinearMath/btMatrix3x3.h>
%}

%fragment("gdxBulletHelpers", "header") {

/* Gets a global ref to the temp class.  Do not release this. */
SWIGINTERN inline jclass gdx_getTempClass(JNIEnv * jenv) {
  static jclass cls = NULL;
  if (cls == NULL) {
	cls = (jclass) jenv->NewGlobalRef(jenv->FindClass("com/badlogic/gdx/physics/bullet/gdxBulletJNI"));
  }
  return cls;
}

/* Gets a global ref to the temp class's Return Vector3.  Do not release this. */ 
SWIGINTERN inline jobject gdx_getReturnVector3(JNIEnv * jenv) {
  static jobject ret = NULL;
  if (ret == NULL) {
    jclass tempClass = gdx_getTempClass(jenv);
    jfieldID field = jenv->GetStaticFieldID(tempClass, "_RET_VECTOR3", "Lcom/badlogic/gdx/math/Vector3;");
    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(tempClass, field));
  }
  return ret;
}

/* Gets a global reference to the temp class's Return Quaternion.  Do not release this. */
SWIGINTERN inline jobject gdx_getReturnQuaternion(JNIEnv * jenv) {
  static jobject ret = NULL;
  if (ret == NULL) {
    jclass tempClass = gdx_getTempClass(jenv);
    jfieldID field = jenv->GetStaticFieldID(tempClass, "_RET_QUATERNION", "Lcom/badlogic/gdx/math/Quaternion;");
    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(tempClass, field));
  }
  return ret;
}

/* Gets a global reference to the temp class's Return Matrix3.  Do not release this. */
SWIGINTERN inline jobject gdx_getReturnMatrix3(JNIEnv * jenv) {
  static jobject ret = NULL;
  if (ret == NULL) {
    jclass tempClass = gdx_getTempClass(jenv);
    jfieldID field = jenv->GetStaticFieldID(tempClass, "_RET_MATRIX3", "Lcom/badlogic/gdx/math/Matrix3;");
    ret = jenv->NewGlobalRef(jenv->GetStaticObjectField(tempClass, field));
  }
  return ret;
}

SWIGINTERN inline jobject gdx_takePoolObject(JNIEnv * jenv, const char * poolName) {
  jclass tempClass = gdx_getTempClass(jenv);
  
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

SWIGINTERN inline void gdx_releasePoolObject(JNIEnv * jenv, const char * poolName, jobject obj) {
  jclass tempClass = gdx_getTempClass(jenv);
  
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
}

/* Sets the data in the Bullet type from the Gdx type. */
SWIGINTERN inline void gdx_setBtVector3FromGdxVector3(JNIEnv * jenv, btVector3 & target, jobject source) {
  jclass sourceClass = jenv->GetObjectClass(source);
  
  static jfieldID xField = NULL, yField = NULL, zField = NULL;
  if (xField == NULL) {
    xField = jenv->GetFieldID(sourceClass, "x", "F");
    yField = jenv->GetFieldID(sourceClass, "y", "F");
    zField = jenv->GetFieldID(sourceClass, "z", "F");
  }
	
  target.setValue(
    jenv->GetFloatField(source, xField),
    jenv->GetFloatField(source, yField),
    jenv->GetFloatField(source, zField));
    
  jenv->DeleteLocalRef(sourceClass);
}

SWIGINTERN inline void gdx_setBtVector3FromGdxVector3(JNIEnv * jenv, btVector3 * target, jobject source) {
  gdx_setBtVector3FromGdxVector3(jenv, *target, source);
}

/* Sets the data in the Gdx type from the Bullet type. */
SWIGINTERN inline void gdx_setGdxVector3FromBtVector3(JNIEnv * jenv, jobject target, const btVector3 & source) {
  jclass targetClass = jenv->GetObjectClass(target);
  
  static jfieldID xField = NULL, yField = NULL, zField = NULL;
  if (xField == NULL) {
    xField = jenv->GetFieldID(targetClass, "x", "F");
    yField = jenv->GetFieldID(targetClass, "y", "F");
    zField = jenv->GetFieldID(targetClass, "z", "F");
  }

  jenv->SetFloatField(target, xField, source.getX());
  jenv->SetFloatField(target, yField, source.getY());
  jenv->SetFloatField(target, zField, source.getZ());
  
  jenv->DeleteLocalRef(targetClass);
}

SWIGINTERN inline void gdx_setGdxVector3FromBtVector3(JNIEnv * jenv, jobject target, const btVector3 * source) {
  gdx_setGdxVector3FromBtVector3(jenv, target, *source);
}

/* Sets the data in the Bullet type from the Gdx type. */
SWIGINTERN inline void gdx_setBtQuaternionFromGdxQuaternion(JNIEnv * jenv, btQuaternion & target, jobject source) {
  jclass sourceClass = jenv->GetObjectClass(source); 
  
  static jfieldID xField = NULL, yField = NULL, zField = NULL, wField = NULL;
  if (xField == NULL) {
    xField = jenv->GetFieldID(sourceClass, "x", "F");
    yField = jenv->GetFieldID(sourceClass, "y", "F");
    zField = jenv->GetFieldID(sourceClass, "z", "F");
    wField = jenv->GetFieldID(sourceClass, "w", "F");
  }
  
  target.setValue(
    jenv->GetFloatField(source, xField),
    jenv->GetFloatField(source, yField),
    jenv->GetFloatField(source, zField),
    jenv->GetFloatField(source, wField));
    
  jenv->DeleteLocalRef(sourceClass);
}

SWIGINTERN inline void gdx_setBtQuaternionFromGdxQuaternion(JNIEnv * jenv, btQuaternion * target, jobject source) {
  gdx_setBtQuaternionFromGdxQuaternion(jenv, *target, source);
}

/* Sets the data in the Gdx type from the Bullet type. */
SWIGINTERN inline void gdx_setGdxQuaternionFromBtQuaternion(JNIEnv * jenv, jobject target, const btQuaternion & source) {
  jclass targetClass = jenv->GetObjectClass(target);
  
  static jfieldID xField = NULL, yField = NULL, zField = NULL, wField = NULL;
  if (xField == NULL) {
    xField = jenv->GetFieldID(targetClass, "x", "F");
    yField = jenv->GetFieldID(targetClass, "y", "F");
    zField = jenv->GetFieldID(targetClass, "z", "F");
    wField = jenv->GetFieldID(targetClass, "w", "F");
  }

  jenv->SetFloatField(target, xField, source.getX());
  jenv->SetFloatField(target, yField, source.getY());
  jenv->SetFloatField(target, zField, source.getZ());
  jenv->SetFloatField(target, wField, source.getW());
  
  jenv->DeleteLocalRef(targetClass);
}

SWIGINTERN inline void gdx_setGdxQuaternionFromBtQuaternion(JNIEnv * jenv, jobject target, const btQuaternion * source) {
  gdx_setGdxQuaternionFromBtQuaternion(jenv, target, *source);
}

/* Sets the data in the Bullet type from the Gdx type. */
SWIGINTERN inline void gdx_setBtMatrix3x3FromGdxMatrix3(JNIEnv * jenv, btMatrix3x3 & target, jobject source) {
  jclass sourceClass = jenv->GetObjectClass(source); 
  
  static jfieldID valsField = NULL;
  if (valsField == NULL) {
    valsField = jenv->GetFieldID(sourceClass, "vals", "[F");
  }
  
  jfloatArray valsArray = (jfloatArray) jenv->GetObjectField(source, valsField);
  jfloat * elements = jenv->GetFloatArrayElements(valsArray, NULL);
  
  // Convert to column-major
  target.setValue(
    elements[0], elements[3], elements[6],
    elements[1], elements[4], elements[7],
    elements[2], elements[5], elements[8]);
  
  jenv->ReleaseFloatArrayElements(valsArray, elements, JNI_ABORT);
  jenv->DeleteLocalRef(valsArray);
  jenv->DeleteLocalRef(sourceClass);
}

SWIGINTERN inline void gdx_setBtMatrix3x3FromGdxMatrix3(JNIEnv * jenv, btMatrix3x3 * target, jobject source) {
  gdx_setBtMatrix3x3FromGdxMatrix3(jenv, *target, source);
}

/* Sets the data in the Gdx type from the Bullet type. */
SWIGINTERN inline void gdx_setGdxMatrix3FromBtMatrix3x3(JNIEnv * jenv, jobject target, const btMatrix3x3 & source) {
  jclass targetClass = jenv->GetObjectClass(target);
  
  static jfieldID valsField = NULL;
  if (valsField == NULL) {
    valsField = jenv->GetFieldID(targetClass, "vals", "[F");
  }
  
  jfloatArray valsArray = (jfloatArray) jenv->GetObjectField(target, valsField);
  jfloat * elements = jenv->GetFloatArrayElements(valsArray, NULL);

  // Convert to column-major
  elements[0] = (jfloat) source.getColumn(0).getX();
  elements[1] = (jfloat) source.getColumn(0).getY();
  elements[2] = (jfloat) source.getColumn(0).getZ();
  elements[3] = (jfloat) source.getColumn(1).getX();
  elements[4] = (jfloat) source.getColumn(1).getY();
  elements[5] = (jfloat) source.getColumn(1).getZ();
  elements[6] = (jfloat) source.getColumn(2).getX();
  elements[7] = (jfloat) source.getColumn(2).getY();
  elements[8] = (jfloat) source.getColumn(2).getZ();

  jenv->ReleaseFloatArrayElements(valsArray, elements, 0);  
  jenv->DeleteLocalRef(valsArray);
  jenv->DeleteLocalRef(targetClass);
}

SWIGINTERN inline void gdx_setGdxMatrix3FromBtMatrix3x3(JNIEnv * jenv, jobject target, const btMatrix3x3 * source) {
  gdx_setGdxMatrix3FromBtMatrix3x3(jenv, target, *source);
}

/*
 * A simple RAII wrapper to release jobjects we obtain from pools in 
 * directorin typemaps.  SWIG doesn't have hooks to release them after
 * they're used. 
 */
class gdxPoolAutoRelease {
private:
  JNIEnv * jenv;
  const char * poolName;
  jobject obj;
public:
  gdxPoolAutoRelease(JNIEnv * jenv, const char * poolName, jobject obj) : 
    jenv(jenv), poolName(poolName), obj(obj) { };
  virtual ~gdxPoolAutoRelease() {
    gdx_releasePoolObject(this->jenv, this->poolName, this->obj);
  };
};

/*
 * RAII wrapper to commit changes made to a local btVector3 back to 
 * the Gdx Vector3.
 */
class gdxAutoCommitVector3 {
private:
  JNIEnv * jenv;
  jobject gdxV3;
  btVector3 & btV3;
public:
  gdxAutoCommitVector3(JNIEnv * jenv, jobject gdxV3, btVector3 & btV3) : 
    jenv(jenv), gdxV3(gdxV3), btV3(btV3) { };
  gdxAutoCommitVector3(JNIEnv * jenv, jobject gdxV3, btVector3 * btV3) : 
    jenv(jenv), gdxV3(gdxV3), btV3(*btV3) { };
  virtual ~gdxAutoCommitVector3() {
    gdx_setGdxVector3FromBtVector3(this->jenv, this->gdxV3, this->btV3);
  };
};

/*
 * RAII wrapper to commit changes made to a local btQuaternion back to 
 * the Gdx Quaternion.
 */
class gdxAutoCommitQuaternion {
private:
  JNIEnv * jenv;
  jobject gdxQ;
  btQuaternion & btQ;
public:
  gdxAutoCommitQuaternion(JNIEnv * jenv, jobject gdxQ, btQuaternion & btQ) : 
    jenv(jenv), gdxQ(gdxQ), btQ(btQ) { };
  gdxAutoCommitQuaternion(JNIEnv * jenv, jobject gdxQ, btQuaternion * btQ) : 
    jenv(jenv), gdxQ(gdxQ), btQ(*btQ) { };
  virtual ~gdxAutoCommitQuaternion() {
    gdx_setGdxQuaternionFromBtQuaternion(this->jenv, this->gdxQ, this->btQ);
  };
};

/*
 * RAII wrapper to commit changes made to a local btMatrix3 back to 
 * the Gdx Matrix3.
 */
class gdxAutoCommitMatrix3 {
private:
  JNIEnv * jenv;
  jobject gdxM;
  btMatrix3x3 & btM;
public:
  gdxAutoCommitMatrix3(JNIEnv * jenv, jobject gdxM, btMatrix3x3 & btM) : 
    jenv(jenv), gdxM(gdxM), btM(btM) { };
  gdxAutoCommitMatrix3(JNIEnv * jenv, jobject gdxM, btMatrix3x3 * btM) : 
    jenv(jenv), gdxM(gdxM), btM(*btM) { };
  virtual ~gdxAutoCommitMatrix3() {
    gdx_setGdxMatrix3FromBtMatrix3x3(this->jenv, this->gdxM, this->btM);
  };
};

}

/* 
 * Put some static temporary variables in the intermediate JNI class where we can 
 * write results to avoid allocations.
 */ 
%pragma(java) jniclasscode=%{

  // Used to avoid allocation when returning from Java
  private final static Vector3 _RET_VECTOR3 = new Vector3(0, 0, 0);
  private final static Quaternion _RET_QUATERNION = new Quaternion(0, 0, 0, 0);
  private final static Matrix3 _RET_MATRIX3 = new Matrix3();
  
  // Used to avoid allocation for parameters in director calls into Java
  public static final Pool<Vector3> _DIR_VECTOR3 = new Pool<Vector3>() {
    @Override
	protected Vector3 newObject() {
      return new Vector3();
	}
  };
  public static final Pool<Quaternion> _DIR_QUATERNION = new Pool<Quaternion>() {
    @Override
	protected Quaternion newObject() {
      return new Quaternion(0, 0, 0, 0);
	}
  };
  public static final Pool<Matrix3> _DIR_MATRIX3 = new Pool<Matrix3>() {
    @Override
	protected Matrix3 newObject() {
      return new Matrix3();
	}
  };
%}

/* 
 * Use Vector3 instead of btVector3.  To avoid allocation on return,
 * the native code writes into a static field in the Java proxy class.
 */
 
%typemap(jstype) 			btVector3, btVector3 &, const btVector3 & 	"Vector3"
%typemap(jtype) 			btVector3, btVector3 &, const btVector3 & 	"Vector3"
%typemap(javain)			btVector3, btVector3 &, const btVector3 &	"$javainput"
%typemap(javadirectorin)	btVector3, btVector3 &, const btVector3 &	"$1"
%typemap(javadirectorout)	btVector3, btVector3 &, const btVector3 &	"$javacall"
%typemap(jni) 				btVector3, btVector3 &, const btVector3 & 	"jobject"

%typemap(in, fragment="gdxBulletHelpers", noblock=1)		btVector3	{
	gdx_setBtVector3FromGdxVector3(jenv, $1, $input);
}
%typemap(in, fragment="gdxBulletHelpers", noblock=1)		btVector3 &, const btVector3 &	{
	btVector3 local_$1;
	gdx_setBtVector3FromGdxVector3(jenv, local_$1, $input);
	$1 = &local_$1;
	gdxAutoCommitVector3 auto_commit_$1(jenv, $input, &local_$1);
}
%typemap(directorin, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Vector3;", noblock=1)	btVector3, btVector3 &, const btVector3 &	{
	$input = gdx_takePoolObject(jenv, "_DIR_VECTOR3");
	gdxPoolAutoRelease autoRelease_$input(jenv, "_DIR_VECTOR3", $input);
	gdx_setGdxVector3FromBtVector3(jenv, $input, $1);
}

%typemap(out, fragment="gdxBulletHelpers", noblock=1)		btVector3, btVector3 &, const btVector3 &	{
	$result = gdx_getReturnVector3(jenv);
	gdx_setGdxVector3FromBtVector3(jenv, $result, $1);
}
%typemap(javaout)		btVector3, btVector3 &, const btVector3 &	{
	return $jnicall;
}
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Vector3;", noblock=1) 	btVector3 {
	gdx_setBtVector3FromGdxVector3(jenv, $result, $input);
}
/* allocate a local so we don't write to static default */
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Vector3;", noblock=1) 	btVector3 &, const btVector3 & {
	btVector3 local_$result;
	gdx_setBtVector3FromGdxVector3(jenv, local_$result, $input);
	$result = &local_$result;
}

/*
 * Use Quaternion instead of btQuaternion.  To avoid allocation on return,
 * the native code writes into a static field in the Java proxy class.
 */
 
%typemap(jstype)		 	btQuaternion, btQuaternion &, const btQuaternion & 	"Quaternion"
%typemap(jtype) 			btQuaternion, btQuaternion &, const btQuaternion & 	"Quaternion"
%typemap(javain)			btQuaternion, btQuaternion &, const btQuaternion &	"$javainput"
%typemap(javadirectorin)	btQuaternion, btQuaternion &, const btQuaternion &	"$1"
%typemap(javadirectorout)	btQuaternion, btQuaternion &, const btQuaternion &	"$javacall"
%typemap(jni) 				btQuaternion, btQuaternion &, const btQuaternion & 	"jobject"

%typemap(in, fragment="gdxBulletHelpers", noblock=1)		btQuaternion	{
	gdx_setBtQuaternionFromGdxQuaternion(jenv, $1, $input);
}
%typemap(in, fragment="gdxBulletHelpers", noblock=1)		btQuaternion &, const btQuaternion &	{
	btQuaternion local_$1;
	gdx_setBtQuaternionFromGdxQuaternion(jenv, local_$1, $input);
	$1 = &local_$1;
	gdxAutoCommitQuaternion auto_commit_$1(jenv, $input, &local_$1);
}
%typemap(directorin, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Quaternion;", noblock=1)	btQuaternion, btQuaternion &, const btQuaternion &	{
	$input = gdx_takePoolObject(jenv, "_DIR_QUATERNION");
	gdxPoolAutoRelease autoRelease_$input(jenv, "_DIR_QUATERNION", $input);
	gdx_setGdxQuaternionFromBtQuaternion(jenv, $input, $1);
}

%typemap(out, fragment="gdxBulletHelpers", noblock=1)		btQuaternion, btQuaternion &, const btQuaternion &	{
	$result = gdx_getReturnQuaternion(jenv);
	gdx_setGdxQuaternionFromBtQuaternion(jenv, $result, $1);
}
%typemap(javaout)	btQuaternion, btQuaternion &, const btQuaternion &	{
	return $jnicall;
}
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Quaternion;", noblock=1) 	btQuaternion {
	gdx_setBtQuaternionFromGdxQuaternion(jenv, $result, $input);
}
/* allocate a local so we don't write to static default */
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Quaternion;", noblock=1) 	btQuaternion &, const btQuaternion & {
	btQuaternion local_$result;
	gdx_setBtQuaternionFromGdxQuaternion(jenv, local_$result, $input);
	$result = &local_$result;
}

/* 
 * Use Matrix3 instead of btMatrix3x3.  To avoid allocation on return,
 * the native code writes into a static field in the Java proxy class.
 */

%typemap(jstype) 			btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 & 	"Matrix3"
%typemap(jtype) 			btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 & 	"Matrix3"
%typemap(javain)			btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &		"$javainput"
%typemap(javadirectorin)	btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &		"$1"
%typemap(javadirectorout)	btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &		"$javacall"
%typemap(jni) 				btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 & 	"jobject"

%typemap(in, noblock=1)		btMatrix3x3	{
	gdx_setBtMatrix3x3FromGdxMatrix3(jenv, $1, $input);
}
%typemap(in, noblock=1)		btMatrix3x3 &, const btMatrix3x3 &	{
	btMatrix3x3 local_$1;
	gdx_setBtMatrix3x3FromGdxMatrix3(jenv, local_$1, $input);
	$1 = &local_$1;
	gdxAutoCommitMatrix3 auto_commit_$1(jenv, $input, &local_$1);
}
%typemap(directorin, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Matrix3;", noblock=1)	btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &	{
	$input = gdx_takePoolObject(jenv, "_DIR_MATRIX3"); 
	gdxPoolAutoRelease autoRelease_$input(jenv, "_DIR_MATRIX3", $input);
	gdx_setGdxMatrix3FromBtMatrix3x3(jenv, $input, $1);
}

%typemap(out, fragment="gdxBulletHelpers", noblock=1)		btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &	{
	$result = gdx_getReturnMatrix3(jenv);
	gdx_setGdxMatrix3FromBtMatrix3x3(jenv, $result, $1);
}
%typemap(javaout)	btMatrix3x3, btMatrix3x3 &, const btMatrix3x3 &	{
	return $jnicall;
}
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Matrix3;", noblock=1) 	btMatrix3x3 {
	gdx_setBtMatrix3x3FromGdxMatrix3(jenv, $result, $input);
}
/* allocate a local so we don't write to static default */
%typemap(directorout, fragment="gdxBulletHelpers", descriptor="Lcom/badlogic/gdx/math/Matrix3;", noblock=1) 	btMatrix3x3 &, const btMatrix3x3 & {
	btMatrix3x3 local_$result;
	gdx_setBtMatrix3x3FromGdxMatrix3(jenv, local_$result, $input);
	$result = &local_$result;
}