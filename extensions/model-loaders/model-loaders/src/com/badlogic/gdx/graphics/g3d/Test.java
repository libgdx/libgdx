package com.badlogic.gdx.graphics.g3d;

public class Test {
	public static void main(String[] argv) {
		float accel = 10; 
		float time = 0.1f;				
		for(int i = 0; i < 10; i++) {
			 accel = accel - (accel * 0.1f) * time;
		}
		System.out.println(accel);
		
		
//		float acc30 = decay(0.1f, 10);
//		float acc60 = decay(60, 10);
//		System.out.println("decay@30: " + acc30 + ", decay@60: " + acc60);
	}
	
	public static float decay(float fps, int seconds) {			
		float acc = 100;
		float decayRate = 0.1f;
		
		for(float i = 0; i < fps *  seconds; i++) {
			float decay = acc * decayRate;
			acc = acc - decay * (1.0f / fps);
			System.out.println("frame #" + i + ": " + acc);
		}
		return acc;
	}
}
