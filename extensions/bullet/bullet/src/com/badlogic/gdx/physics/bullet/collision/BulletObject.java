package com.badlogic.gdx.physics.bullet.collision;

import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.BufferUtils;

public class BulletObject {
	/*JNI

	#include <Bullet-C-Api.h>
	
	 */
	
	public static native void testJni (long addr, 
			ByteBuffer buffer, float[] floats, byte[] bytes, String ohgod); /*
		printf("%f %d %s\n", floats[0], bytes[0], ohgod);
	*/
	
	public static void main(String[] args) {
		BulletNativesLoader.load();
		BulletObject.testJni(0, BufferUtils.newByteBuffer(1), 
							new float[] { 1234.567f }, 
							new byte[] { 123 }, "Oh God it works");
	}
}
