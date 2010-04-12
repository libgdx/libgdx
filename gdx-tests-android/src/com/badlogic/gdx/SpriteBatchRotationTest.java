package com.badlogic.gdx;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class SpriteBatchRotationTest extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate( bundle );
		initialize(false);
		getGraphics().setRenderListener( new com.badlogic.gdx.tests.SpriteBatchRotationTest() );
	}
}
