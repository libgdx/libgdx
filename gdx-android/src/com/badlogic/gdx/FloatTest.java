package com.badlogic.gdx;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class FloatTest extends AndroidApplication
{
	public void onCreate( Bundle bundle )
	{
		super.onCreate(bundle);
		addRenderListener( new com.badlogic.gdx.samples.FloatTest() );
	}
}
