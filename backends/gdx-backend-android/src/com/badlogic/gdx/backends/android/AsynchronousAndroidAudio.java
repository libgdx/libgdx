package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.badlogic.gdx.audio.Sound;

public class AsynchronousAndroidAudio extends DefaultAndroidAudio {

	private final HandlerThread handlerThread;
	private final Handler handler;

	public AsynchronousAndroidAudio (Context context, AndroidApplicationConfiguration config) {
		super(context, config);
		if (!config.disableAudio) {
			handlerThread = new HandlerThread("libGDX Sound Management");
			handlerThread.start();
			handler = new Handler(handlerThread.getLooper());
		} else {
			handler = null;
			handlerThread = null;
		}
	}

	@Override
	public void dispose () {
		super.dispose();
		if (handlerThread != null) {
			handlerThread.quit();
		}
	}

	@Override
	protected Sound postProcessSound (AndroidSound sound) {
		return new AsynchronousSound(sound, handler);
	}
}
