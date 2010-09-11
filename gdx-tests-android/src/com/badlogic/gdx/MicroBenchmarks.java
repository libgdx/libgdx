package com.badlogic.gdx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.utils.BufferUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MicroBenchmarks extends Activity
{
	Thread t;
	long start = 0;
	
	public void onCreate( Bundle bundle )
	{		
		super.onCreate( bundle );
	
		if( t == null )
		{
			t = new Thread( new Runnable( ) {
	
				@Override
				public void run() 
				{
					ByteBuffer byteBuffer = ByteBuffer.allocateDirect( 1024*1024 * Float.SIZE / 8 );
					byteBuffer.order( ByteOrder.nativeOrder() );
					FloatBuffer buffer = byteBuffer.asFloatBuffer();
					float[] array = new float[1024*1024];
					
					// single put
					tic();
					for( int tries = 0; tries < 25; tries++ )
					{
						for( int i = 0; i < array.length; i++ )
							buffer.put( array[i] );
						buffer.clear();
					}
					toc( "single put" );
	
					// single indexed put
					tic();
					for( int tries = 0; tries < 25; tries++ )
					{
						for( int i = 0; i < array.length; i++ )
							buffer.put( i, array[i] );
						buffer.clear();
					}
					toc( "single indexed put" );
					
					// bulk put
					tic();
					for( int tries = 0; tries < 25; tries++ )
					{
						buffer.put( array );
						buffer.clear();
					}
					toc( "vector put" );
					
					// jni bulk put
					tic();
					for( int tries = 0; tries < 25; tries++ )
					{
						BufferUtils.copy( array, buffer, array.length, 0 );
						buffer.clear();
					}
					toc( "jni put" );
					
					// jni test
					byteBuffer = ByteBuffer.allocateDirect( 4 * Float.SIZE / 8 );
					byteBuffer.order(ByteOrder.nativeOrder());
					buffer = byteBuffer.asFloatBuffer();					
					array = new float[] { 0, 1, 2, 3 };
					BufferUtils.copy( array, buffer, 4, 0 );
					System.out.println( buffer.get(0));
					System.out.println( buffer.get(1));
					System.out.println( buffer.get(2));
					System.out.println( buffer.get(3));
				}
				
			});
			t.start();
		}
	}
	
	private void tic( )
	{
		start = System.nanoTime();
	}
	
	private void toc( String info )
	{
		Log.d( "MicroBenchmarks", info + ", " + (System.nanoTime()-start)/1000000000.0f );
	}
}
