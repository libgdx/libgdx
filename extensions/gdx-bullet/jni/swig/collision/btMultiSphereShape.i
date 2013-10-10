%module btMultiSphereShape

%include "../common/gdxDisableBuffers.i"
%include "../common/gdxEnableArrays.i"

%fragment("gdxBulletHelpersVector3Array", "header") {
	btVector3* Vector3ArrayToBtVector3Array(JNIEnv * jenv, jobjectArray source) {
		static jfieldID xField = NULL, yField = NULL, zField = NULL;
		jint len = jenv->GetArrayLength(source);
		if (len <= 0)
			return NULL;
			
		btVector3* result = new btVector3[len];
			
		if (xField == NULL) {
			jobject vec = jenv->GetObjectArrayElement(source, 0);
			jclass sc = jenv->GetObjectClass(vec);
			xField = jenv->GetFieldID(sc, "x", "F");
			yField = jenv->GetFieldID(sc, "y", "F");
			zField = jenv->GetFieldID(sc, "z", "F");
			jenv->DeleteLocalRef(sc);
		}
		
		for (int i = 0; i < len; i++) {
			jobject vec = jenv->GetObjectArrayElement(source, i);
			result[i].setValue(jenv->GetFloatField(vec, xField), jenv->GetFloatField(vec, yField), jenv->GetFloatField(vec, zField));
		}
		return result;
	}
	
	class gdxAutoDeleteBtVector3Array {
	private:
	  btVector3* array;
	public:
	  gdxAutoDeleteBtVector3Array(btVector3* arr) : 
	    array(arr) { }
	  virtual ~gdxAutoDeleteBtVector3Array() {
		  if (array != NULL)
			  delete[] array;
	  }
	};
}

%typemap(jstype) 			btVector3* 	"Vector3[]"
%typemap(jtype) 			btVector3* 	"Vector3[]"
%typemap(javain)			btVector3*	"$javainput"
%typemap(jni) 				btVector3* 	"jobjectArray"

%typemap(in, fragment="gdxBulletHelpersVector3Array", noblock=1)		btVector3*	{
	static jfieldID xField = NULL, yField = NULL, zField = NULL;
	$1 = Vector3ArrayToBtVector3Array(jenv, $input);
	gdxAutoDeleteBtVector3Array auto_delete($1);
}

%{
#include <BulletCollision/CollisionShapes/btMultiSphereShape.h>
%}
%include "BulletCollision/CollisionShapes/btMultiSphereShape.h"

%clear btVector3*;
%include "../common/gdxDisableArrays.i"
%include "../common/gdxEnableBuffers.i"