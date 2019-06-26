package com.badlogic.gdx.utils;

public class Collections {

	private static boolean allocateIterators;

	public static boolean isAllocateIterators() {
		return allocateIterators;
	}

	/**
	 * AllocateIterators determines whether {@link Iterable#iterator()} in collection classes have to allocate a new iterator for each invocation or not. When
	 * true, a new iterator is created per invocation, when false, the iterator is reused and nested use will throw an exception. Default is false.
	 * @param allocateIterators the allocateIterators value to set
	 */
	public static void setAllocateIterators(boolean allocateIterators) {
		Collections.allocateIterators = allocateIterators;
	}
}
