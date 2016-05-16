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

package com.badlogic.gdx.jnigen.parsing;

import java.util.ArrayList;

public interface JavaMethodParser {
	public ArrayList<JavaSegment> parse (String classFile) throws Exception;

	public interface JavaSegment {
		public int getStartIndex ();

		public int getEndIndex ();
	}

	public class JniSection implements JavaSegment {
		private String nativeCode;
		private final int startIndex;
		private final int endIndex;

		public JniSection (String nativeCode, int startIndex, int endIndex) {
			this.nativeCode = nativeCode;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		public String getNativeCode () {
			return nativeCode;
		}

		public void setNativeCode (String nativeCode) {
			this.nativeCode = nativeCode;
		}

		public int getStartIndex () {
			return startIndex;
		}

		public int getEndIndex () {
			return endIndex;
		}

		@Override
		public String toString () {
			return "JniSection [nativeCode=" + nativeCode + ", startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
		}
	}

	public enum ArgumentType {
		Boolean("jboolean"), Byte("jbyte"), Char("jchar"), Short("jshort"), Integer("jint"), Long("jlong"), Float("jfloat"), Double(
			"jdouble"), Buffer("jobject"), ByteBuffer("jobject"), CharBuffer("jobject"), ShortBuffer("jobject"), IntBuffer("jobject"), LongBuffer(
			"jobject"), FloatBuffer("jobject"), DoubleBuffer("jobject"), BooleanArray("jbooleanArray"), ByteArray("jbyteArray"), CharArray(
			"jcharArray"), ShortArray("jshortArray"), IntegerArray("jintArray"), LongArray("jlongArray"), FloatArray("jfloatArray"), DoubleArray(
			"jdoubleArray"), String("jstring"), Object("jobject"), ObjectArray("jobjectArray");

		private final String jniType;

		ArgumentType (String jniType) {
			this.jniType = jniType;
		}

		public boolean isPrimitiveArray () {
			return toString().endsWith("Array") && this != ObjectArray;
		}

		public boolean isBuffer () {
			return toString().endsWith("Buffer");
		}

		public boolean isObject () {
			return toString().equals("Object") || this == ObjectArray;
		}

		public boolean isString () {
			return toString().equals("String");
		}

		public boolean isPlainOldDataType () {
			return !isString() && !isPrimitiveArray() && !isBuffer() && !isObject();
		}

		public String getBufferCType () {
			if (!this.isBuffer()) throw new RuntimeException("ArgumentType " + this + " is not a Buffer!");
			if (this == Buffer) return "unsigned char*";
			if (this == ByteBuffer) return "char*";
			if (this == CharBuffer) return "unsigned short*";
			if (this == ShortBuffer) return "short*";
			if (this == IntBuffer) return "int*";
			if (this == LongBuffer) return "long long*";
			if (this == FloatBuffer) return "float*";
			if (this == DoubleBuffer) return "double*";
			throw new RuntimeException("Unknown Buffer type " + this);
		}

		public String getArrayCType () {
			if (!this.isPrimitiveArray()) throw new RuntimeException("ArgumentType " + this + " is not an Array!");
			if (this == BooleanArray) return "bool*";
			if (this == ByteArray) return "char*";
			if (this == CharArray) return "unsigned short*";
			if (this == ShortArray) return "short*";
			if (this == IntegerArray) return "int*";
			if (this == LongArray) return "long long*";
			if (this == FloatArray) return "float*";
			if (this == DoubleArray) return "double*";
			throw new RuntimeException("Unknown Array type " + this);
		}
		
		public String getArrayJniType () {
			if (!this.isPrimitiveArray()) throw new RuntimeException("ArgumentType " + this + " is not an Array!");
			if (this == BooleanArray) return "jboolean*";
			if (this == ByteArray) return "jbyte*";
			if (this == CharArray) return "jchar*";
			if (this == ShortArray) return "jshort*";
			if (this == IntegerArray) return "jint*";
			if (this == LongArray) return "jlong*";
			if (this == FloatArray) return "jfloat*";
			if (this == DoubleArray) return "jdouble*";
			throw new RuntimeException("Unknown Array type " + this);
		}

		public String getJniType () {
			return jniType;
		}
	}

	public static class Argument {
		final ArgumentType type;
		private final String name;

		public Argument (ArgumentType type, String name) {
			this.type = type;
			this.name = name;
		}

		public ArgumentType getType () {
			return type;
		}

		public String getName () {
			return name;
		}

		@Override
		public String toString () {
			return "Argument [type=" + type + ", name=" + name + "]";
		}

		/** This only checks whether, the ArgumentType is the same, the name is ignored! 
		 * DONT EVER CHANGE!*/
		@Override
		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Argument other = (Argument)obj;
			if (type != other.type) return false;
			return true;
		}
		
	}

	/** @author mzechner */
	public static class JavaMethod implements JavaSegment {
		private final String className;
		private final String name;
		private final boolean isStatic;
		private final boolean isSynchronized;
		private boolean isManual;
		private boolean isCritical;
		private final String returnType;
		private String nativeCode;
		private final ArrayList<Argument> arguments;
		private final boolean hasDisposableArgument;
		private final int startIndex;
		private final int endIndex;
		public static final String CRITICAL_METHOD_NAME = "_JNICritical";

		public JavaMethod (String className, String name, boolean isStatic, boolean isSynchronized, String returnType, String nativeCode,
			ArrayList<Argument> arguments, int startIndex, int endIndex) {
			this.className = className;
			this.name = name;
			this.isStatic = isStatic;
			this.isSynchronized = isSynchronized;
			this.returnType = returnType;
			this.nativeCode = nativeCode;
			this.arguments = arguments;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.isCritical = name.endsWith(CRITICAL_METHOD_NAME);
			for (Argument arg : arguments) {
				if (arg.type.isPrimitiveArray() || arg.type.isBuffer() || arg.type.isString()) {
					hasDisposableArgument = true;
					return;
				}
			}
			hasDisposableArgument = false;
		}

		public String getName () {
			return name;
		}

		public boolean isStatic () {
			return isStatic;
		}
		
		public boolean isSynchronized () {
			return isSynchronized;
		}

		public void setManual (boolean isManual) {
			this.isManual = isManual;
		}

		public boolean isManual () {
			return this.isManual;
		}		

		/** <p>
		 * Critical natives are alternative native functions in Hotspot JVM since Java7. They are designed to improve performance of
		 * native methods which only pass a few primitives and some (primitives) arrays (from 10 to 1K elements).
		 * </p>
		 * 
		 * <p>
		 * The declaration of a critical native is similar to a normal native function, but it starts with <b>JavaCritical_</b>
		 * instead of <b>Java_</b>, and it hasn't <b>JNIEnv*</b> and <b>jclass</b> arguments. The standard implementation must be
		 * preserved. Not only for running in other JVMs, but also because Hotspot has a "lazy entry policy" for critical natives.
		 * The first time calling a critical native will always run the normal native function (starts with "Java_"). After some
		 * invocations (could be hundreds) the Hotspot will replace the normal function with the critical one (starts with
		 * "JavaCritical_").
		 * </p>
		 * 
		 * <p>
		 * Critical natives have some limits:
		 * </p>
		 * <ul>
		 * <li>The method must be <b>static</b> and <b>non-synchronized</b>.</li>
		 * <li>You can not use JNIEnv* or jclass.</li>
		 * <li>Arguments must be <b>primitives</b> or <b>primitive arrays</b>, the primitive array will be passed in two parts:
		 * First one is a <b>jint</b>, which represents the length of the array. Second one is a pointer to the raw array data.</li>
		 * <li>It will block GC.</li>
		 * <li>Only available in Hotspot JVM of Java7+.</li>
		 * </ul>
		 * 
		 * For example:<br/>
		 * <br/>
		 * 
		 * If a native method is:<br/>
		 * <br/>
		 * 
		 * {@code public static native void foo(int i, float[] a1, float[] a2); //in somepackage.Someclass} <br/>
		 * <br/>
		 * 
		 * The normal native function is: <br/>
		 * <br/>
		 * 
		 * {@code Java_somepackage_Someclass_foo(JNIEnv*, jclass, jint, jfloatArray, jfloatArray);} <br/>
		 * <br/>
		 * 
		 * The critical native function would be: <br/>
		 * <br/>
		 * 
		 * {@code JavaCritical_somepackage_Someclass_foo(jint, jint, jfloat*, jint, jfloat*);} <br/>
		 * <br/>
		 * 
		 * <b>Critical natives aren't generated by default.</b><br/>
		 * You must first create a new (java) method, which has the same method name plus {@code_JNICritical} so the normal method
		 * {@code foo} would become {@code foo_JNICritical}.<br/>
		 * 
		 * So our java method for the critical native would be:<br>
		 * <br>
		 * Critical method:
		 * {@code public static native void foo_JNICritical(int i, int a1Length, float[] a1, int a2Length, float[] a2); //in somepackage.Someclass}
		 * <br/>
		 * Compared to the normal method some things changed:<br>
		 * <br>
		 * Normal method: {@code public static native void foo(int i, float[] a1, float[] a2); //in somepackage.Someclass} <br/>
		 * <br>
		 * <ul>
		 * <li>The name changed to {@code foo_JNICritical}</li>
		 * <li>The arguments changed, before every primitive array is a new param which contains the length of the primitive array.
		 * </li>
		 * </ul>
		 * 
		 * <p>
		 * For more informations, visit <a href="
		 * http://stackoverflow.com/questions/36298111/is-it-possible-to-use-sun-misc-unsafe-to-call-c-functions-without-jni/36309652#36309652
		 * ">the original post in Stackoverflow</a>
		 * </p>
		 * 
		 * @return Is this the critical version of another method, i.e. does the method name end with "_JNICritical". */
		public boolean isCritical () {
			return this.isCritical;
		}

		public String getReturnType () {
			return returnType;
		}

		public String getNativeCode () {
			return nativeCode;
		}

		public void setNativeCode (String nativeCode) {
			this.nativeCode = nativeCode;
		}

		public ArrayList<Argument> getArguments () {
			return arguments;
		}

		public boolean hasDisposableArgument () {
			return hasDisposableArgument;
		}

		@Override
		public int getStartIndex () {
			return startIndex;
		}

		@Override
		public int getEndIndex () {
			return endIndex;
		}

		public CharSequence getClassName () {
			return className;
		}

		@Override
		public String toString () {
			return "JavaMethod [className=" + className + ", name=" + name + ", isStatic=" + isStatic + ", isSynchronized="
				+ isSynchronized + ", returnType=" + returnType + ", nativeCode=" + nativeCode + ", arguments=" + arguments
				+ ", hasDisposableArgument=" + hasDisposableArgument + ", startIndex=" + startIndex + ", endIndex=" + endIndex
				+ ", isManual=" + isManual + ", isCritical=" + isCritical + "]";
		}
		
		public String shortToString () {
			return "JavaMethod [className=" + className + ", name=" + name + ", returnType=" + returnType + ", arguments="
				+ arguments + "]";
		}
	}
}
