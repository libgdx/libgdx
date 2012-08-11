/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.jnigen.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class MyJniClass {
	public static native void test (boolean boolArg, byte byteArg, char charArg, short shortArg, int intArg, long longArg,
		float floatArg, double doubleArg, Buffer byteBuffer, boolean[] boolArray, char[] charArray, short[] shortArray,
		int[] intArray, long[] longArray, float[] floatArray, double[] doubleArray, double[][] multidim, String string); /*
																																								 * printf(
																																								 * "boolean: %s\n"
																																								 * ,
																																								 * boolArg
																																								 * ?
																																								 * "true":
																																								 * "false"
																																								 * );
																																								 * printf(
																																								 * "byte: %d\n"
																																								 * ,
																																								 * byteArg
																																								 * );
																																								 * printf(
																																								 * "char: %c\n"
																																								 * ,
																																								 * charArg
																																								 * );
																																								 * printf(
																																								 * "short: %d\n"
																																								 * ,
																																								 * shortArg
																																								 * );
																																								 * printf(
																																								 * "int: %d\n"
																																								 * ,
																																								 * intArg
																																								 * );
																																								 * printf(
																																								 * "long: %l\n"
																																								 * ,
																																								 * longArg
																																								 * );
																																								 * printf(
																																								 * "float: %f\n"
																																								 * ,
																																								 * floatArg
																																								 * );
																																								 * printf(
																																								 * "double: %d\n"
																																								 * ,
																																								 * doubleArg
																																								 * );
																																								 * printf(
																																								 * "byteBuffer: %d\n"
																																								 * ,
																																								 * byteBuffer
																																								 * [0]);
																																								 * printf(
																																								 * "bool[0]: %s\n"
																																								 * ,
																																								 * boolArray
																																								 * [
																																								 * 0]?"true"
																																								 * :
																																								 * "false"
																																								 * );
																																								 * printf(
																																								 * "char[0]: %c\n"
																																								 * ,
																																								 * charArray
																																								 * [0]);
																																								 * printf(
																																								 * "short[0]: %d\n"
																																								 * ,
																																								 * shortArray
																																								 * [0]);
																																								 * printf(
																																								 * "int[0]: %d\n"
																																								 * ,
																																								 * intArray
																																								 * [0]);
																																								 * printf(
																																								 * "long[0]: %ll\n"
																																								 * ,
																																								 * longArray
																																								 * [0]);
																																								 * printf(
																																								 * "float[0]: %f\n"
																																								 * ,
																																								 * floatArray
																																								 * [0]);
																																								 * printf(
																																								 * "double[0]: %f\n"
																																								 * ,
																																								 * doubleArray
																																								 * [0]);
																																								 * printf(
																																								 * "string: %s fuck this nuts\n"
																																								 * ,
																																								 * string
																																								 * );
																																								 */

	// @off
	/*JNI
	#include <stdio.h>
	 */
	
//	public static class TestInner {
//		public native void testInner(int arg); /*
//			printf("%d\n", arg);
//		*/
//	}
	
	public static void main(String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", "jni", new String[] { "**/MyJniClass.java" }, null);
		
		// generate build scripts, for win32 only
		BuildConfig buildConfig = new BuildConfig("test");
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.compilerPrefix = "";
		win32.cppFlags += " -g";
		new AntScriptGenerator().generate(buildConfig, win32);
		
		// build natives
		BuildExecutor.executeAnt("jni/build.xml", "clean all -v");
			
		// load the test-natives.jar and from it the shared library, then execute the test. 
		new JniGenSharedLibraryLoader("libs/test-natives.jar").load("test");
		ByteBuffer buffer = ByteBuffer.allocateDirect(1);
		buffer.put(0, (byte)8);
		MyJniClass.test(true, (byte)1, (char)2, (short)3, 4, 5, 6, 7, buffer, new boolean[] { false }, new char[] { 9 }, new short[] { 10 }, new int[] { 11 }, new long[] { 12 }, new float[] { 13 }, new double[] { 14 }, null, "Hurray");
	}
}