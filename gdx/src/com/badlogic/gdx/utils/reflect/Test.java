package com.badlogic.gdx.utils.reflect;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;

public class Test {

	public static class TestClass {
		
		public int field1 = 10065;

		public TestClass() {
			
		}
		
		public TestClass(int field1) {
			this.field1 = field1;
		}
		
		public void mulField1 (int value) {
			this.field1 *= value;
		}
		
	}
	
	public static void main(String[] args) {
		try {
			Constructor[] constructors = Reflection.getConstructors(TestClass.class);
			Method[] methods = Reflection.getMethods(TestClass.class);
			Field[] fields = Reflection.getFields(TestClass.class);
			
			for (Method method : methods) {
				System.out.println(method.getName());
			}
			
			TestClass testClass = (TestClass) constructors[1].newInstance();
			Method setField1 = Reflection.getDeclaredMethod(TestClass.class, "mulField1", int.class);
			setField1.invoke(testClass, 2);
			System.out.println(testClass.field1);
		} catch (ReflectionException e) {
			e.printStackTrace();
		}

	}
	
}
