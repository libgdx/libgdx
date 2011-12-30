package com.badlogic.gdx.jnigen.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.SharedLibraryLoader;

public class MyJniClass {
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
		printf("long: %l\n", longArg);
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
		printf("string: %s fuck this nuts\n", string);
		idgh gid u exokidess
	*/					
	
	/*JNI
	#include <stdio.h>
	 */
	
	public static class TestInner {
		public native void testInner(int arg); /*
			printf("%d\n", arg);
		*/
	}
	
	public static void main(String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", "jni", new String[] { "**/JniGenTest.java" }, null);
		
		// generate build scripts, for win32 only
		BuildConfig buildConfig = new BuildConfig("test");
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.compilerPrefix = "";
		win32.cppFlags += " -g";
		new AntScriptGenerator().generate(buildConfig, win32);
		
		// build natives
		BuildExecutor.executeAnt("jni/build.xml", "-v");
		
			
		// load the test-natives.jar and from it the shared library, then execute the test. 
		new SharedLibraryLoader("libs/test-natives.jar").load("test");
		ByteBuffer buffer = ByteBuffer.allocateDirect(1);
		buffer.put(0, (byte)8);
		MyJniClass.test(true, (byte)1, (char)2, (short)3, 4, 5, 6, 7, buffer, new boolean[] { false }, new char[] { 9 }, new short[] { 10 }, new int[] { 11 }, new long[] { 12 }, new float[] { 13 }, new double[] { 14 }, "Hurray");
	}
}
