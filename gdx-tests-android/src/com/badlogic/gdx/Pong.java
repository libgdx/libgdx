package com.badlogic.gdx;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class Pong extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		initialize( false );
		getGraphics().setRenderListener( new com.badlogic.gdx.tests.Pong() );
	}
}
