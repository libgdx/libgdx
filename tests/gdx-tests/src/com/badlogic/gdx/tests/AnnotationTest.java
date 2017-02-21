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

package com.badlogic.gdx.tests;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;

/** Performs some tests with {@link Annotation} and prints the results on the screen.
 * @author dludwig */
public class AnnotationTest extends GdxTest {
	String message = "";
	BitmapFont font;
	SpriteBatch batch;

	public enum TestEnum {
		EnumA, EnumB
	}

	/** Sample annotation. It is required to annotate annotations themselves with {@link RetentionPolicy}.RUNTIME to become
	 * available to the {@link ClassReflection} package. */
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface TestAnnotation {
		// String parameter, no default
		String name();

		// Array of integers, with default values
		int[] values() default {1, 2, 3};

		// Enumeration, with default value
		TestEnum someEnum() default TestEnum.EnumA;
	}

	/** Sample usage of class and field annotations. */
	@TestAnnotation(name = "MyAnnotatedClass", someEnum = TestEnum.EnumB)
	static public class AnnotatedClass {
		public int unanottatedField;
		public int unannotatedMethod() { return 0; }
		@TestAnnotation(name = "MyAnnotatedField", values = {4, 5}) public int annotatedValue;
		@TestAnnotation(name = "MyAnnotatedMethod", values = {6, 7}) public int annotatedMethod () { return 0; };		
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	static public @interface TestInheritAnnotation {
	}

	@TestInheritAnnotation
	static public class InheritClassA {
	}

	@TestAnnotation(name = "MyInheritClassB")
	static public class InheritClassB extends InheritClassA {
	}
	
	@Override
	public void create () {
		font = new BitmapFont();
		batch = new SpriteBatch();

		try {
			Annotation annotation = ClassReflection.getDeclaredAnnotation(AnnotatedClass.class, TestAnnotation.class);
			if (annotation != null) {
				TestAnnotation annotationInstance = annotation.getAnnotation(TestAnnotation.class);
				println("Class annotation:\n name=" + annotationInstance.name() + ",\n values="
					+ Arrays.toString(annotationInstance.values()) + ",\n enum=" + annotationInstance.someEnum().toString());
			} else {
				println("ERROR: Class annotation not found.");
			}

			Field field = ClassReflection.getDeclaredField(AnnotatedClass.class, "annotatedValue");
			if (field != null) {
				Annotation[] annotations = field.getDeclaredAnnotations();
				for (Annotation a : annotations) {
					if (a.getAnnotationType().equals(TestAnnotation.class)) {
						TestAnnotation annotationInstance = a.getAnnotation(TestAnnotation.class);
						println("Field annotation:\n name=" + annotationInstance.name() + ",\n values="
							+ Arrays.toString(annotationInstance.values()) + ",\n enum=" + annotationInstance.someEnum().toString());
						break;
					}
				}
			} else {
				println("ERROR: Field 'annotatedValue' not found.");
			}

			Method method = ClassReflection.getDeclaredMethod(AnnotatedClass.class, "annotatedMethod");
			if (method != null) {
				Annotation[] annotations = method.getDeclaredAnnotations();
				for (Annotation a : annotations) {
					if (a.getAnnotationType().equals(TestAnnotation.class)) {
						TestAnnotation annotationInstance = a.getAnnotation(TestAnnotation.class);
						println("Method annotation:\n name=" + annotationInstance.name() + ",\n values="
							+ Arrays.toString(annotationInstance.values()) + ",\n enum=" + annotationInstance.someEnum().toString());
						break;
					}
				}
			} else {
				println("ERROR: Method 'annotatedMethod' not found.");
			}

			println("Class annotations w/@Inherit:");
			Annotation[] annotations = ClassReflection.getAnnotations(InheritClassB.class);
			for (Annotation a : annotations) {
				println(" name=" + a.getAnnotationType().getSimpleName());
			}
			if (!ClassReflection.isAnnotationPresent(InheritClassB.class, TestInheritAnnotation.class)) {
				println("ERROR: Inherited class annotation not found.");
			}
		} catch (Exception e) {
			println("FAILED: " + e.getMessage());
			message += e.getClass();
		}
	}

	private void println (String line) {
		message += line + "\n";
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, message, 20, Gdx.graphics.getHeight() - 20);
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
	}

}
