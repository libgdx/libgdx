package com.badlogic.gdx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.badlogic.gdx.utils.BufferUtils;

public class MicroBenchmarks extends Activity
{
	final int TRIES = 5;
	long start = 0;
	ScrollView sv;
	TextView tv;	
	Thread testThread = new Thread( new Runnable( ) {
		
		@Override
		public void run() 
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect( 1024*1024 * Float.SIZE / 8 );
			buffer.order(ByteOrder.nativeOrder());			
			FloatBuffer floatBuffer = buffer.asFloatBuffer();
			IntBuffer intBuffer = buffer.asIntBuffer();
			
			float[] floatArray = new float[1024*1024];
			int[] intArray = new int[1024*1024];
			
			// single put
			tic();
			for( int tries = 0; tries < TRIES; tries++ )
			{
				for( int i = 0; i < floatArray.length; i++ )
					floatBuffer.put( floatArray[i] );
				floatBuffer.clear();
			}
			toc( "single put" );

			// single indexed put
			tic();
			for( int tries = 0; tries < TRIES; tries++ )
			{
				for( int i = 0; i < floatArray.length; i++ )
					floatBuffer.put( i, floatArray[i] );
				floatBuffer.clear();
			}
			toc( "single indexed put" );
			
			// bulk put
			tic();
			for( int tries = 0; tries < TRIES; tries++ )
			{
				floatBuffer.put( floatArray );
				floatBuffer.clear();
			}
			toc( "vector put" );
			
			// convert bulk put
			tic();
			for( int tries = 0; tries < TRIES; tries++ )
			{
				for( int i = 0; i < floatArray.length; i++ )
					intArray[i] = Float.floatToIntBits(floatArray[i]);
				intBuffer.put(intArray);
				intBuffer.clear();
			}
			toc( "convert bulk put" );
			
			// jni bulk put
			tic();
			for( int tries = 0; tries < TRIES; tries++ )
			{
				BufferUtils.copy( floatArray, floatBuffer, floatArray.length, 0 );
				floatBuffer.clear();
			}
			toc( "jni bulk put" );			
		}
		
	});	
	
	public void onCreate( Bundle bundle )
	{		
		super.onCreate( bundle );	
		
		tv = new TextView( this );
		sv = new ScrollView( this );
		sv.addView( tv );
		setContentView( sv );

		testThread.start();
	}

	private void tic( )
	{
		start = System.nanoTime();
	}
	
	private void toc( final String info )
	{
		tv.post( new Runnable() {

			@Override
			public void run()
			{
				StringBuilder buff = new StringBuilder( tv.getText() );
				buff.append( info ).append( ", " ).append((System.nanoTime()-start)/1000000000.0f).append( " secs\n" );
				tv.setText( buff.toString() );
			}
		} );

		Log.d( "MicroBenchmarks", info + ", " + (System.nanoTime()-start)/1000000000.0f );
	}
}
