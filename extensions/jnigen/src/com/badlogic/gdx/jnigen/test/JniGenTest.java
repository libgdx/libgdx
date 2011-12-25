package com.badlogic.gdx.jnigen.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.jnigen.SharedLibraryLoader;

public class JniGenTest {
	public static native void test(boolean boolArg, 
								  byte byteArg, 
								  char charArg, 
								  short shortArg, 
								  int intArg, 
								  long longArg, 
								  float floatArg, 
								  double doubleArg, 
								  Buffer byteBuffer,
								  boolean[] boolArray,
								  char[] charArray,
								  short[] shortArray,
								  int[] intArray,
								  long[] longArray,
								  float[] floatArray,
								  double[] doubleArray,
								  String string); /*
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
	*/						  
	
	public static void main(String[] args) throws Exception {
		new SharedLibraryLoader().load("test");
		ByteBuffer buffer = ByteBuffer.allocateDirect(1);
		buffer.put(0, (byte)8);
		JniGenTest.test(true, (byte)1, (char)2, (short)3, 4, 5, 6, 7, buffer, new boolean[] { false }, new char[] { 9 }, new short[] { 10 }, new int[] { 11 }, new long[] { 12 }, new float[] { 13 }, new double[] { 14 }, "Hurray"); 
	}
}
