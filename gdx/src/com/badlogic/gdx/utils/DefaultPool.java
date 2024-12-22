
package com.badlogic.gdx.utils;

/** Default Pool implementation that creates a new instances of a type based on a supplier. */
public class DefaultPool<T> extends Pool<T> {

	private final PoolSupplier<T> poolTypeSupplier;

	public DefaultPool (PoolSupplier<T> supplier) {
		this(supplier, 16, Integer.MAX_VALUE);
	}

	public DefaultPool (PoolSupplier<T> supplier, int initialCapacity) {
		this(supplier, initialCapacity, Integer.MAX_VALUE);
	}

	public DefaultPool (PoolSupplier<T> supplier, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.poolTypeSupplier = supplier;
	}

	@Override
	protected T newObject () {
		return poolTypeSupplier.get();
	}

	public interface PoolSupplier<T> {
		T get ();
	}
}
