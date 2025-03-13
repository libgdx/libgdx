
package com.badlogic.gdx.utils;

/** An interface that is used to create arrays. Even tho not annotated with "FunctionalInterface", it can act as one. <br>
 * You can use a constructor reference or lambda as a ArraySupplier, such as with {@code MyClass[]::new} or
 * {@code (size) -> new MyClass[size]}. */
public interface ArraySupplier<T> {
	/** A default array supplier that creates an Object[]. */
	ArraySupplier<?> OBJECT_ARRAY_SUPPLIER = Object[]::new;

	T get (int size);
}
