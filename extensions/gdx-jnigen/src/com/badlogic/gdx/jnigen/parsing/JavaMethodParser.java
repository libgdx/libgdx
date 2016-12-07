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
			"jdoubleArray"), String("jstring"), Class("jclass"), Throwable("jthrowable"), Object("jobject"), ObjectArray("jobjectArray");

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
	}

	/** @author mzechner */
	public static class JavaMethod implements JavaSegment {
		private final String className;
		private final String name;
		private final boolean isStatic;
		private boolean isManual;
		private final String returnType;
		private String nativeCode;
		private final ArrayList<Argument> arguments;
		private final boolean hasDisposableArgument;
		private final int startIndex;
		private final int endIndex;

		public JavaMethod (String className, String name, boolean isStatic, String returnType, String nativeCode,
			ArrayList<Argument> arguments, int startIndex, int endIndex) {
			this.className = className;
			this.name = name;
			this.isStatic = isStatic;
			this.returnType = returnType;
			this.nativeCode = nativeCode;
			this.arguments = arguments;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
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

		public void setManual (boolean isManual) {
			this.isManual = isManual;
		}

		public boolean isManual () {
			return this.isManual;
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
			return "JavaMethod [className=" + className + ", name=" + name + ", isStatic=" + isStatic + ", returnType=" + returnType
				+ ", nativeCode=" + nativeCode + ", arguments=" + arguments + ", hasDisposableArgument=" + hasDisposableArgument
				+ ", startIndex=" + startIndex + ", endIndex=" + endIndex + "]";
		}
	}
}
