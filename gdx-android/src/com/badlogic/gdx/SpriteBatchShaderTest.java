package com.badlogic.gdx;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class SpriteBatchShaderTest extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		initialize( true );
		getGraphics().setRenderListener( new com.badlogic.gdx.tests.SpriteBatchShaderTest() );
	}
}
