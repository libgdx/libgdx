package com.badlogic.gdx.utils;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

/**
 * This test class tests the changes to the class Array submitted by the
 * author.
 * @author Javier
 *
 *

 */
public class TestArray {
	
	Array<SonOfA> arrayOfSonOfA ;
	Array<A> arrayOfA;

	@Before
	public void setUp() {
		arrayOfSonOfA = getTestArrayOfSonOfAElements();
		arrayOfA = getTestArrayOfAElements();
	}

	@Test
	public void testConstructorUsingAnArray() {
		Array<SonOfA> arrayOfSonOfA = getTestArrayOfSonOfAElements();
		
		Array<A> arrayOfA = new Array<A>(arrayOfSonOfA);
		
		assertEquals(arrayOfA.size, arrayOfSonOfA.size);
	}

	@Test
	public void testAddAllAceptDerivedTypes() {
		
		int expectedSize = arrayOfA.size + arrayOfSonOfA.size;
		
		arrayOfA.addAll(arrayOfSonOfA);
		
		assertEquals(expectedSize, arrayOfA.size);
	}
	
	@Test
	public void testAddAceptDerivedTypes() {
		SonOfA sonOfA = new SonOfA();
		int expectedSize = arrayOfA.size + 1;
		
		arrayOfA.add(sonOfA);
		
		assertEquals(expectedSize, arrayOfA.size);		
	}


	@Test
	public void testRemoveAll_UsingEquality() {
		Array<A> arrayOfA02 = getTestArrayOfAElements();
		
		boolean b = arrayOfA.removeAll(arrayOfA02, false);
		
		assertEquals(0, arrayOfA.size);
		assertTrue(b);
	}

	@Test
	public void testRemoveAllByEquality() {
		Array<A> arrayOfA02 = getTestArrayOfAElements();
		
		boolean b = arrayOfA.removeAllByEquality(arrayOfA02);
		
		assertEquals(0, arrayOfA.size);
		assertTrue(b);
	}

	@Test
	public void testRemoveAllByIdentity() {
		Array<SonOfA> arrayOfA01 = getTestArrayOfSonOfAElements();
		Array<SonOfA> arrayOfA02 = copy(arrayOfA01);
		
		boolean b = arrayOfA01.removeAllByIdentity(arrayOfA02);
		
		assertEquals(0, arrayOfA01.size);
		assertTrue(b);
	}

	
	@Test
	public void testRemoveAll_UsingByIdentity() {
		Array<SonOfA> arrayOfA01 = this.getTestArrayOfSonOfAElements();
		Array<SonOfA> arrayOfA02 = copy(arrayOfA01);
		
		arrayOfA01.removeAll(arrayOfA02, true);
		
		assertEquals(0, arrayOfA01.size);		
	}
	
	
	
	@Test
	public void testContainsByIdentity_ElementIsInTheArray() {
		Array<B> array = this.getTestArrayOfBElements();	
		boolean expt = array.contains(array.get(0), true);
		assertTrue(expt);
		
		expt = array.contains(new B(), true);
		assertFalse(expt);
	}


	@Test
	public void testContainsByEquality_ElementIsInTheArray() {
		boolean expt = arrayOfA.contains(arrayOfA.get(0), false);
		assertTrue(expt);
		
		Array<B> arrayB = this.getTestArrayOfBElements();
		expt = arrayB.contains(arrayB.get(0), false);
		assertFalse(expt);	
	}
	
	//----------------------------------------------------------
	
	private Array<SonOfA> copy(Array<SonOfA> arrayOfA01) {
		return new Array<SonOfA>(arrayOfA01);
	}


	class A {
		public boolean equals(Object o) { return true; }
	};
	class SonOfA extends A {};
	class B {
		public boolean equals(Object o) { return false; }
	};

	private Array<A> getTestArrayOfAElements() {
		Array<A> arrayOfA = new Array<>();
		
		for (int i = 0; i < 4; i++) {
			arrayOfA.add(new A());
		}
		return arrayOfA;
	}
	
	private Array<SonOfA> getTestArrayOfSonOfAElements() {
		Array<SonOfA> arrayOfSonOfA = new Array<>();
		
		for (int i = 0; i < 4; i++) {
			arrayOfSonOfA.add(new SonOfA());
		}
		return arrayOfSonOfA;
	}
	
	private Array<B> getTestArrayOfBElements() {
		Array<B> array = new Array<>();
		
		for (int i = 0; i < 4; i++) {
			array.add(new B());
		}
		return array;
	}

}
