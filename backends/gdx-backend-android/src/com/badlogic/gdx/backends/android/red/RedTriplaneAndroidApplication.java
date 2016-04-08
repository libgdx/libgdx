
package com.badlogic.gdx.backends.android.red;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.jfixby.android.api.AndroidComponent;
import com.jfixby.cmns.api.log.L;

import android.app.ActivityManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public abstract class RedTriplaneAndroidApplication extends AndroidApplication implements AndroidComponent {

	private ApplicationListener gdxListener;
	private AndroidApplicationConfiguration androidConfig;

	static boolean deployed = false;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		synchronized (this) {
			if (deployed) {
				return;
			}
			deployed = true;
			RedTriplaneAndroidApplicationConfig config = doGdxDeploy(this);
			this.gdxListener = config.getGdxListener();
			this.androidConfig = config.getAndroidApplicationConfig();
		}
		initialize(gdxListener, androidConfig);
	}

	public abstract RedTriplaneAndroidApplicationConfig doGdxDeploy (RedTriplaneAndroidApplication redTriplaneAndroidApplication);

	public void printMemoryClass () {
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		L.d("onCreate", "memoryClass:" + Integer.toString(memoryClass));
	}

	@Override
	public long getRecommendedHeapSize () {
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		return memoryClass;
	}

	@Override
	public long getMaxHeapSize () {
		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory() / (1024 * 1024);
		// Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));
		return maxMemory;
	}

	@Override
	public String getApplicationPrivateDirPath () {
		String java_path = getApplication().getApplicationContext().getFilesDir().getAbsolutePath();
		return java_path;
	}
}
