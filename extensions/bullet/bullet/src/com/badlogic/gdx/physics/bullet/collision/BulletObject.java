package com.badlogic.gdx.physics.bullet.collision;

import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * All objects in the wrapper derrive from this class.
 * All BulletObject instances need to be manually disposed
 * off as they are allocated in native heap memory.
 * 
 * @author mzechner
 *
 */
public class BulletObject implements Disposable {
	/** the address of the object, stored as a 64-bit value **/
	protected long addr;
	
	public BulletObject(long addr) {
		this.addr = addr;
	}
	
	@Override
	public void dispose() {
		if(addr != 0) {
//			disposeJni(addr);
			addr = 0;
		}
	}
	
	public static native void disposeJni (long addr, ByteBuffer buffer, float[] floats, byte[] bytes, String ohgod); /*
		printf("Hello World %f %d %s\n", floats[0], bytes[0], ohgod);
	*/
	
	public static void main(String[] args) {
		BulletNativesLoader.load();
		BulletObject.disposeJni(0, BufferUtils.newByteBuffer(1), new float[] { 1234.567f }, new byte[] { 123 }, "Oh God it works");
	}
}
