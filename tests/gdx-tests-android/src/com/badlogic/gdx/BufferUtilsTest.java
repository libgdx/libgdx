package com.badlogic.gdx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.BufferUtils;

import android.app.Activity;
import android.os.Bundle;

public class BufferUtilsTest extends Activity 
{
	static
	{
		System.loadLibrary( "gdx" );
	}
	
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
	
		ByteBuffer buffer = ByteBuffer.allocateDirect( 6 * 5000 * (2 + 4 + 2) );
		buffer.order(ByteOrder.nativeOrder());
		float[] test = new float[4000];
		
		BufferUtils.copy( test, buffer, 4000, 0 );
		
	}
}
