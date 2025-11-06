
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.DefaultPool.PoolSupplier;

/** A class that can be used to handle multiple pools together. Explicit pool registration is needed via
 * {@link PoolManager#addPool(Class, PoolSupplier)}/{@link PoolManager#addPool(Class, Pool)}. */
@SuppressWarnings("unchecked")
public class PoolManager {

	private final ObjectMap<Class<?>, Pool<?>> typePools = new ObjectMap<>();

	/** Registers a new pool with the given supplier. Will throw an exception, if a pool for the same class is already registered.
	 * This can be used like `PoolManager#addPoll(MyClass::new);` */
	public <T> void addPool (Class<T> poolClass, PoolSupplier<T> poolSupplier) {
		addPool(poolClass, new DefaultPool<>(poolSupplier));
	}

	/** Registers the new pool. Will throw an exception, if a pool for the same class is already registered */
	public <T> void addPool (Class<T> poolClass, Pool<T> pool) {
		Pool<?> oldPool = typePools.put(poolClass, pool);
		if (oldPool != null) {
			throw new GdxRuntimeException("Attempt to add pool with already existing class: " + poolClass
				+ ", register using PoolManager#addPool(" + poolClass.getSimpleName() + ", " + poolClass.getSimpleName() + "::new)");
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

	/** Returns the pool registered for the class. Will return null, if no pool for this class is registered */
	public <T> Pool<T> getPoolOrNull (Class<T> clazz) {
		return (Pool<T>)typePools.get(clazz);
	}

	/** Whether a pool for this class is already registered */
	public boolean hasPool (Class<?> clazz) {
		return typePools.containsKey(clazz);
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

	/** Returns a new pooled object for the class. Will return null, if no pool for this class is registered. Free with
	 * {@link PoolManager#free} */
	public <T> T obtainOrNull (Class<T> clazz) {
		Pool<T> pool = (Pool<T>)typePools.get(clazz);
		if (pool == null) {
			return null;
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
