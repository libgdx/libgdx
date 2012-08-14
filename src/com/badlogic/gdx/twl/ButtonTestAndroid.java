package com.badlogic.gdx.twl;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class ButtonTestAndroid extends AndroidApplication
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initialize(new com.badlogic.gdx.twl.tests.ButtonTest(), false);
	}
}