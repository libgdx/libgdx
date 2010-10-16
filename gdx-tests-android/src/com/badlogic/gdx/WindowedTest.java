package com.badlogic.gdx;

import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Color;

public class WindowedTest extends AndroidApplication implements RenderListener
{
	Color color = new Color( 1, 1, 1, 1 );
	
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		
		Button b1 = new Button( this );
		b1.setText( "Change Color" );
		b1.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
		Button b2 = new Button( this );
		b2.setText( "New Window" );
		b2.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ) );
		
		LinearLayout layout = new LinearLayout( this );
		layout.setOrientation( LinearLayout.VERTICAL );
		layout.addView( b1 );
		layout.addView( b2 );
		
		initialize( false, 16, layout, new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
		getGraphics().setRenderListener(this);
		setContentView( layout );
		
		b1.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				color.set( (float)Math.random(), (float)Math.random(), (float)Math.random(), 1 );
			}
			
		});

		b2.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent( WindowedTest.this, WindowedTest.class );
				WindowedTest.this.startActivity( intent );
			}		
		});
	}
	
	public void onPause( )
	{
		super.onPause( );
	}
	
	@Override
	public void onDestroy( )
	{
		super.onDestroy();
		Log.w( "WindowedTest", "destroying" );
	}

	@Override
	public void surfaceCreated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceChanged(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() 
	{
		Gdx.graphics.getGL10().glClearColor( color.r, color.g, color.g, color.a );
		Gdx.graphics.getGL10().glClear( GL10.GL_COLOR_BUFFER_BIT );
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
