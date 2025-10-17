
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.DefaultPool.PoolSupplier;

/** A class that can be used to handle multiple pools together. Explicit pool registration is needed via the constructor or
 * {@link PoolManager#addPool(PoolSupplier)}/{@link PoolManager#addPool(Pool)}. */
@SuppressWarnings("unchecked")
public class PoolManager {

	private final ObjectMap<Class<?>, Pool<?>> typePools = new ObjectMap<>();

	public PoolManager () {

	}

	/** Example: `new PoolManager(MyClass1::new, MyClass2::new); */
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

	/** Registers a new pool with the given supplier. Will throw an exception, if a pool for the same class is already registered.
	 * This can be used like `PoolManager#addPoll(MyClass::new);` */
	public <T> void addPool (PoolSupplier<T> poolSupplier) {
		addPool(new DefaultPool<>(poolSupplier));
	}

	/** Registers the new pool. Will throw an exception, if a pool for the same class is already registered */
	public <T> void addPool (Pool<T> pool) {
		T object = pool.obtain();
		Class<T> clazz = (Class<T>)object.getClass();
		pool.free(object);

		Pool<?> oldPool = typePools.put(clazz, pool);
		if (oldPool != null) {
			throw new GdxRuntimeException("Attempt to add pool with already existing class: " + clazz
				+ ", register using PoolManager#addPool(" + clazz.getSimpleName() + "::new)");
		}
	}

	/** Returns the pool registered for the class. Will throw an exception, if no pool for this class is registered */
	public <T> Pool<T> getPool (Class<T> clazz) {
		Pool<T> pool = (Pool<T>)typePools.get(clazz);
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to get pool with unknown class: " + clazz
				+ ", register using PoolManager#addPool(" + clazz.getSimpleName() + "::new)");
		}
		return pool;
	}

	/** Returns a new pooled object for the class. Will throw an exception, if no pool for this class is registered. Free with
	 * {@link PoolManager#free} */
	public <T> T obtain (Class<T> clazz) {
		Pool<T> pool = (Pool<T>)typePools.get(clazz);
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to get pooled object with unknown class: " + clazz
				+ ", register using PoolManager#addPool(" + clazz.getSimpleName() + "::new)");
		}
		return pool.obtain();
	}

	/** Frees a pooled object. Will throw an exception, if no pool for this class is registered. It is unchecked, whether the
	 * object was obtained by the registered pool. */
	public <T> void free (T object) {
		Pool<T> pool = (Pool<T>)typePools.get(object.getClass());
		if (pool == null) {
			throw new GdxRuntimeException("Attempt to free pooled object with unknown class: " + object.getClass()
				+ ", register using PoolManager#addPool(" + object.getClass().getSimpleName() + "::new)");
		}
		pool.free(object);
	}

	/** Clears all contents of the managed pools */
	public void clear () {
		for (Pool<?> pool : typePools.values()) {
			pool.clear();
		}
	}
}
