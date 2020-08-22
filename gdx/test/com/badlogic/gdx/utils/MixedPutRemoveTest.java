package com.badlogic.gdx.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class MixedPutRemoveTest {
	@Test
	public void testLongMapPut() {
		LongMap<Integer> gdxMap = new LongMap<Integer>();
		HashMap<Long, Integer> jdkMap = new HashMap<Long, Integer>();
		long stateA = 0L, stateB = 1L;
		int gdxRepeats = 0, jdkRepeats = 0;
		long item;
		for (int i = 0; i < 0x100000; i++) { // a million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			item = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item &= item >>> 24; // causes 64-bit state to get crammed into 40 bits, with item biased toward low bit counts 
			if(gdxMap.put(item, i) != null) gdxRepeats++;
			if(jdkMap.put(item, i) != null) jdkRepeats++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRepeats, jdkRepeats);
	}
	@Test
	public void testLongMapMix() {
		LongMap<Integer> gdxMap = new LongMap<Integer>();
		HashMap<Long, Integer> jdkMap = new HashMap<Long, Integer>();
		long stateA = 0L, stateB = 1L;
		int gdxRemovals = 0, jdkRemovals = 0;
		long item;
		for (int i = 0; i < 0x100000; i++) { // 1 million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			item = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item &= item >>> 24; // causes 64-bit state to get crammed into 40 bits, with item biased toward low bit counts 
			if(gdxMap.remove(item) == null) gdxMap.put(item, i);
			else gdxRemovals++;
			if(jdkMap.remove(item) == null) jdkMap.put(item, i);
			else jdkRemovals++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRemovals, jdkRemovals);
	}
	@Test
	public void testIntMapPut() {
		IntMap<Integer> gdxMap = new IntMap<Integer>();
		HashMap<Integer, Integer> jdkMap = new HashMap<Integer, Integer>();
		long stateA = 0L, stateB = 1L, temp;
		int gdxRepeats = 0, jdkRepeats = 0;
		int item;
		for (int i = 0; i < 0x100000; i++) { // 1 million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			temp = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item = (int)(temp & temp >>> 24); // causes 64-bit state to get crammed into 32 bits, with item biased toward low bit counts 
			if(gdxMap.put(item, i) != null) gdxRepeats++;
			if(jdkMap.put(item, i) != null) jdkRepeats++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRepeats, jdkRepeats);
	}
	@Test
	public void testIntMapMix() {
		IntMap<Integer> gdxMap = new IntMap<Integer>();
		HashMap<Integer, Integer> jdkMap = new HashMap<Integer, Integer>();
		long stateA = 0L, stateB = 1L, temp;
		int gdxRemovals = 0, jdkRemovals = 0;
		int item;
		for (int i = 0; i < 0x100000; i++) { // 1 million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			temp = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item = (int)(temp & temp >>> 24); // causes 64-bit state to get crammed into 32 bits, with item biased toward low bit counts 
			if(gdxMap.remove(item) == null) gdxMap.put(item, i);
			else gdxRemovals++;
			if(jdkMap.remove(item) == null) jdkMap.put(item, i);
			else jdkRemovals++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRemovals, jdkRemovals);
	}
	@Test
	public void testObjectMapPut() {
		ObjectMap<Integer, Integer> gdxMap = new ObjectMap<Integer, Integer>();
		HashMap<Integer, Integer> jdkMap = new HashMap<Integer, Integer>();
		long stateA = 0L, stateB = 1L, temp;
		int gdxRepeats = 0, jdkRepeats = 0;
		int item;
		for (int i = 0; i < 0x100000; i++) { // 1 million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			temp = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item = (int)(temp & temp >>> 24); // causes 64-bit state to get crammed into 32 bits, with item biased toward low bit counts 
			if(gdxMap.put(item, i) != null) gdxRepeats++;
			if(jdkMap.put(item, i) != null) jdkRepeats++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRepeats, jdkRepeats);
	}
	@Test
	public void testObjectMapMix() {
		ObjectMap<Integer, Integer> gdxMap = new ObjectMap<Integer, Integer>();
		HashMap<Integer, Integer> jdkMap = new HashMap<Integer, Integer>();
		long stateA = 0L, stateB = 1L, temp;
		int gdxRemovals = 0, jdkRemovals = 0;
		int item;
		for (int i = 0; i < 0x100000; i++) { // 1 million should do
			// simple-ish RNG that repeats more than RandomXS128; we want repeats to test behavior
			stateA += 0xC6BC279692B5C323L;
			temp = (stateA ^ stateA >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
			item = (int)(temp & temp >>> 24); // causes 64-bit state to get crammed into 32 bits, with item biased toward low bit counts 
			if(gdxMap.remove(item) == null) gdxMap.put(item, i);
			else gdxRemovals++;
			if(jdkMap.remove(item) == null) jdkMap.put(item, i);
			else jdkRemovals++;
			Assert.assertEquals(gdxMap.size, jdkMap.size());
		}
		Assert.assertEquals(gdxRemovals, jdkRemovals);
	}
}
