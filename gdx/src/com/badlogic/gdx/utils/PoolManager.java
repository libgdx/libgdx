
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.DefaultPool.PoolSupplier;

/** A class that can be used to handle multiple pools together. Explicit pool registration is needed via the constructor or
 * {@link PoolManager#addPool(PoolSupplier)}/{@link PoolManager#addPool(Pool)}. */
@SuppressWarnings("unchecked")
public class PoolManager {

	private final ObjectMap<Class<?>, Pool<?>> typePools = new ObjectMap<>();

	public PoolManager () {

	}

	public PoolManager (PoolSupplier<?>... poolSuppliers) {
		for (PoolSupplier<?> poolSupplier : poolSuppliers) {
			addPool(poolSupplier);
		}
	}

	public PoolManager (Pool<?>... pools) {
		for (Pool<?> pool : pools) {
			addPool(pool);
		}
	}

	/** Registers a new pool with the given supplier. Will throw an exception, if a pool for the same class is already
	 * registered */
	public <T> void addPool (PoolSupplier<T> poolSupplier) {
		Class<T> clazz = (Class<T>)poolSupplier.get().getClass();
		if (typePools.containsKey(clazz)) {
			throw new GdxRuntimeException("Attempt to add pool with already existing class: " + clazz);
		}

		typePools.put(clazz, new DefaultPool<>(poolSupplier));
	}

	/** Registers the new pool. Will throw an exception, if a pool for the same class is already registered */
	public <T> void addPool (Pool<T> pool) {
		T object = pool.obtain();
		if (typePools.containsKey(object.getClass())) {
			throw new GdxRuntimeException("Attempt to add pool with already existing class: " + object.getClass());
		}

		typePools.put(object.getClass(), pool);
		pool.free(object);
	}

	/** Returns the pool registered for the class. Will throw an exception, if no pool for this class is registered */
	public <T> Pool<T> getPool (Class<T> clazz) {
		Pool<T> pool = (Pool<T>)typePools.get(clazz);
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to get pool with unknown class: " + clazz);
		}
		return pool;
	}

	/** Returns a new pooled object for the class. Will throw an exception, if no pool for this class is registered. Free with
	 * {@link PoolManager#free} */
	public <T> T obtain (Class<T> clazz) {
		Pool<T> pool = (Pool<T>)typePools.get(clazz);
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to get pooled object with unknown class: " + clazz);
		}
		return pool.obtain();
	}

	/** Frees a pooled object. Will throw an exception, if no pool for this class is registered. It is unchecked, whether the
	 * object was obtained by the registered pool. */
	public <T> void free (T object) {
		Pool<T> pool = (Pool<T>)typePools.get(object.getClass());
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to free pooled object with unknown class: " + object.getClass());
		}
		pool.free(object);
	}
}
