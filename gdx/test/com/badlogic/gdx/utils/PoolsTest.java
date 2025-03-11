
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.DefaultPool.PoolSupplier;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class PoolsTest {

	static private final ObjectMap<Class<?>, Pool<?>> supplierPoolsCache;

	static {
		try {
			Field field = Pools.class.getDeclaredField("supplierPoolsCache");
			field.setAccessible(true);
			supplierPoolsCache = (ObjectMap<Class<?>, Pool<?>>)field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setup () {
		Pools.set(Object.class, null);
	}

	@Test
	public void testDifferentLambdasSamePool () {
		PoolSupplier<Object> pool1 = Object::new;
		PoolSupplier<Object> pool2 = Object::new;

		assertNotEquals(pool1.getClass(), pool2.getClass());
		assertEquals(Pools.get(pool1), Pools.get(pool2));
		assertEquals(2, supplierPoolsCache.size);
		assertTrue(supplierPoolsCache.containsKey(pool1.getClass()));
		assertTrue(supplierPoolsCache.containsKey(pool2.getClass()));
	}

	@Test
	public void testPoolsSet () {
		PoolSupplier<Object> pool1 = Object::new;

		PoolSupplier<Object> pool2 = Object::new;

		assertNotEquals(pool1.getClass(), pool2.getClass());
		assertEquals(Pools.get(pool1), Pools.get(pool2));
		assertEquals(2, supplierPoolsCache.size);

		DefaultPool<Object> newPool = new DefaultPool<>(Object::new);
		Pools.set(Object.class, newPool);
		assertEquals(0, supplierPoolsCache.size);
		assertEquals(newPool, Pools.get(pool1));
		assertEquals(newPool, Pools.get(pool2));
		assertEquals(2, supplierPoolsCache.size);
	}

	@Test
	public void testPoolsReset () {
		PoolSupplier<Object> pool1 = Object::new;
		PoolSupplier<Object> pool2 = Object::new;

		assertNotEquals(pool1.getClass(), pool2.getClass());
		assertEquals(Pools.get(pool1), Pools.get(pool2));
		assertEquals(2, supplierPoolsCache.size);

		Pool<Object> oldPool = Pools.get(pool1);

		Pools.set(Object.class, null);
		assertEquals(0, supplierPoolsCache.size);
		assertEquals(Pools.get(pool1), Pools.get(pool2));
		assertNotEquals(oldPool, Pools.get(pool2));
		assertEquals(2, supplierPoolsCache.size);
	}
}
