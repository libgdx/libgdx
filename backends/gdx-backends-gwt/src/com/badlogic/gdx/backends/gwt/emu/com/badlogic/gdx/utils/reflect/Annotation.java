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
