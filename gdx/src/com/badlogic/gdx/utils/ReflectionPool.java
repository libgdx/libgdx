
package com.badlogic.gdx.utils;

import java.lang.reflect.Constructor;

/** Pool that creates new instances of a type using reflection. The type must have a zero argument constructor.
 * {@link Constructor#setAccessible(boolean)} will be used if the class and/or constructor is not visible.
 * @author Nathan Sweet */
public class ReflectionPool<T> extends Pool<T> {
	private final Class<T> type;

	public ReflectionPool (Class<T> type) {
		this.type = type;
	}

	public ReflectionPool (Class<T> type, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.type = type;
	}

	public ReflectionPool (Class<T> type, int initialCapacity) {
		super(initialCapacity);
		this.type = type;
	}

	protected T newObject () {
		try {
			return type.newInstance();
		} catch (Exception ex) {
			Constructor ctor;
			try {
				ctor = type.getConstructor((Class[])null);
			} catch (Exception ex2) {
				try {
					ctor = type.getDeclaredConstructor((Class[])null);
					ctor.setAccessible(true);
				} catch (NoSuchMethodException ex3) {
					throw new RuntimeException("Class cannot be created (missing no-arg constructor): " + type.getName());
				}
			}
			try {
				return (T)ctor.newInstance();
			} catch (Exception ex3) {
				throw new GdxRuntimeException("Unable to create new instance: " + type.getName(), ex);
			}
		}
	}
}
