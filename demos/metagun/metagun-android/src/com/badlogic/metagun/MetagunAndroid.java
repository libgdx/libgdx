
package com.badlogic.metagun;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mojang.metagun.Metagun;

public class MetagunAndroid extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new Metagun(), false);
	}
}
