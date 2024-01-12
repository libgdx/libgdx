
package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/** A performance oriented implementation of the {@link AndroidAudio} interface.
 * 
 * Sounds are played on a separate thread. This avoids waiting for sound ids on methods that can potentially lock main thread for
 * considerable amount of time, especially when playing several sounds at the same time. The limitation of this approach is that
 * methods that require a sound id are not supported. */
public class AsynchronousAndroidAudio extends DefaultAndroidAudio {

	private final HandlerThread handlerThread;
	private final Handler handler;

	public AsynchronousAndroidAudio (Context context, AndroidApplicationConfiguration config) {
		super(context, config);
		handlerThread = new HandlerThread("libGDX Sound Management");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
	}

	@Override
	public void dispose () {
		super.dispose();
		if (handlerThread != null) {
			handlerThread.quit();
		}
	}

	@Override
	public Sound newSound (FileHandle file) {
		Sound sound = super.newSound(file);
		return new AsynchronousSound(sound, handler);
	}
}
