#include <com.badlogic.gdx.jnigen.test.JniGenTest.h>
JNIEXPORT void JNICALL Java_com_badlogic_gdx_jnigen_test_JniGenTest_test
(JNIEnv* env, jclass clazz, jboolean boolArg, jbyte byteArg, jchar charArg, jshort shortArg, jint intArg, jlong longArg, jfloat floatArg, jdouble doubleArg, jobject obj_byteBuffer, jbooleanArray obj_boolArray, jcharArray obj_charArray, jshortArray obj_shortArray, jintArray obj_intArray, jlongArray obj_longArray, jfloatArray obj_floatArray, jdoubleArray obj_doubleArray, jstring obj_string) {
	char* byteBuffer = (char*)env->GetDirectBufferAddress(obj_byteBuffer);
	char* string = (char*)env->GetStringUTFChars(obj_string, 0);
	unsigned char* boolArray = (unsigned char*)env->GetPrimitiveArrayCritical(obj_boolArray, 0);
	unsigned short* charArray = (unsigned short*)env->GetPrimitiveArrayCritical(obj_charArray, 0);
	short* shortArray = (short*)env->GetPrimitiveArrayCritical(obj_shortArray, 0);
	int* intArray = (int*)env->GetPrimitiveArrayCritical(obj_intArray, 0);
	long long* longArray = (long long*)env->GetPrimitiveArrayCritical(obj_longArray, 0);
	float* floatArray = (float*)env->GetPrimitiveArrayCritical(obj_floatArray, 0);
	double* doubleArray = (double*)env->GetPrimitiveArrayCritical(obj_doubleArray, 0);

	printf("boolean: %s\n", boolArg?"true":"false");
	printf("byte: %d\n", byteArg);
	printf("char: %c\n", charArg);
	printf("short: %d\n", shortArg);
	printf("int: %d\n", intArg);
	printf("long: %ll\n", longArg);
	printf("float: %f\n", floatArg);
	printf("double: %d\n", doubleArg);
	printf("byteBuffer: %d\n", byteBuffer[0]);
	printf("bool[0]: %s\n", boolArray[0]?"true":"false");
	printf("char[0]: %c\n", charArray[0]);
	printf("short[0]: %d\n", shortArray[0]);
	printf("int[0]: %d\n", intArray[0]);
	printf("long[0]: %ll\n", longArray[0]);
	printf("float[0]: %f\n", floatArray[0]);
	printf("double[0]: %f\n", doubleArray[0]);
	printf("string: %s\n", string);
	
	env->ReleasePrimitiveArrayCritical(obj_boolArray, boolArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_charArray, charArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_shortArray, shortArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_intArray, intArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_longArray, longArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_floatArray, floatArray, 0);
	env->ReleasePrimitiveArrayCritical(obj_doubleArray, doubleArray, 0);
	env->ReleaseStringUTFChars(obj_string, string);
}
