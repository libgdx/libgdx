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

package com.badlogic.gwtref.client;

import com.google.gwt.core.client.EntryPoint;

public class Test implements EntryPoint {
	public static enum Enu {
		Winter, Summer, Bleh;
	}

	public static class A {
		String text;
		float numberf;
		int numberi;

		public String getText () {
			return text;
		}

		public void setText (String text) {
			this.text = text;
		}

		public float getNumberf () {
			return numberf;
		}

		public void setNumberf (float numberf) {
			this.numberf = numberf;
		}

		public int getNumberi () {
			return numberi;
		}

		public void setNumberi (int numberi) {
			this.numberi = numberi;
		}

		public float getSum (float a, float b) {
			return a + b;
		}
	}

	public static class B extends A {
		String text = "This is a string";

		public void testWithPackagePrivate (C c, int a) {
		}

		public void testWidthPrivate (A c) {
		}

		public void testVoid () {
		}

		public native void test (A c) /*-{
												//			this.@com.badlogic.gwtref.client.Test.B::testWidthPrivate(LC;)(c);
												}-*/;
	}

	public static class C {
	}

	@Override
	public void onModuleLoad () {
		Type ta = ReflectionCache.getType(A.class);
		Type tb = ReflectionCache.getType(B.class);
		B b = (B)tb.newInstance();
		for (Field f : tb.getFields())
			System.out.println(f);
		for (Method m : tb.getMethods())
			System.out.println(m);

		try {
			tb.getDeclaredFields()[0].set(b, "Field of B");
			ta.getDeclaredFields()[0].set(b, "Field of A");
			System.out.println(ta.getMethod("getText").invoke(b));
			System.out.println(ta.getMethod("getSum", float.class, float.class).invoke(b, 1, 2));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
}
