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

package com.badlogic.gdx.utils.reflect;

/** Provides information about, and access to, an annotation of a field, class or interface.
 * @author dludwig */
public final class Annotation {

	private java.lang.annotation.Annotation annotation;

	Annotation (java.lang.annotation.Annotation annotation) {
		this.annotation = annotation;
	}

	@SuppressWarnings("unchecked")
	public <T extends java.lang.annotation.Annotation> T getAnnotation (Class<T> annotationType) {
		if (annotation.annotationType().equals(annotationType)) {
			return (T) annotation;
		}
		return null;
	}

	public Class<? extends java.lang.annotation.Annotation> getAnnotationType () {
		return annotation.annotationType();
	}
}
