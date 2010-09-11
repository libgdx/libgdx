package com.badlogic.gdx;

import android.os.Bundle;
import android.os.Debug;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class MD5Test extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		initialize( false );
		
		getGraphics().setRenderListener( new com.badlogic.gdx.tests.MD5Test() );
	}	
}
