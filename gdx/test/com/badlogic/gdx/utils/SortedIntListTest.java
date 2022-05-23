
package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

public class SortedIntListTest {

	@Test
	public void testIteratorWithAllocation () {
		Collections.allocateIterators = true;
		try {
			SortedIntList<String> list = new SortedIntList<String>();
			list.insert(0, "hello");
			Assert.assertEquals(1, list.size);
			Assert.assertEquals("hello", list.get(0));
			Assert.assertEquals("hello", list.iterator().next().value);
		} finally {
			Collections.allocateIterators = false;
		}
	}
}
