package com.badlogic.b2c.test;

public class SimpleBean {
	int i;
	float f;
	byte b;
	short[] sa;
	
	public SimpleBean() {
		i = 0;
		f = 0;
		b = 0;
		sa = new short[4];
	}
	
	public int getI() {
		return i;
	}
	
	public short[] getSa() {
		return sa;
	}
	
	public float addAll() {
		float sum = 0;
		sum += i;
		sum += f;
		sum += b;
		for(short s: sa) sum += s;
		return sum;
	}
}
