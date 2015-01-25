/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import android.media.SoundPool;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

/** Class defining the configuration of an {@link AndroidApplication}. Allows you to disable the use of the accelerometer to save
 * battery among other things.
 * @author mzechner */
public class AndroidApplicationConfiguration {
	/** number of bits per color channel **/
	public int r = 5, g = 6, b = 5, a = 0;

	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;

	/** number of samples for CSAA/MSAA, 2 is a good value **/
	public int numSamples = 0;

	/** whether to use the accelerometer. default: true **/
	public boolean useAccelerometer = true;

	/** whether to use the compass. default: true **/
	public boolean useCompass = true;

	/** the time in milliseconds to sleep after each event in the touch handler, set this to 16ms to get rid of touch flooding on
	 * pre Android 2.0 devices. default: 0 **/
	public int touchSleepTime = 0;

	/** whether to keep the screen on and at full brightness or not while running the application. default: false */
	public boolean useWakelock = false;

	/** hide status bar buttons on Android 4.x and higher (API 14+). Doesn't work if "android:targetSdkVersion" less 11 or if API
	 * less 14. default: false **/
	public boolean hideStatusBar = false;

	/** whether to disable Android audio support. default: false */
	public boolean disableAudio = false;

	/** the maximum number of {@link Sound} instances that can be played simultaneously, sets the corresponding {@link SoundPool}
	 * constructor argument. */
	public int maxSimultaneousSounds = 16;

	/** the {@link ResolutionStrategy}. default: {@link FillResolutionStrategy} **/
	public ResolutionStrategy resolutionStrategy = new FillResolutionStrategy();

	/** if the app is a livewallpaper, whether it should get full touch events **/
	public boolean getTouchEventsForLiveWallpaper = false;

	/** set this to true to enable Android 4.4 KitKat's 'Immersive mode' **/
	public boolean useImmersiveMode = false;

	/** whether to use {@link com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18} in place of the classic
	 * {@link com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20} on Android API 10 and lower.
	 * In case this is true {@link com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18} will be used.
	 * This implementation properly supports attach to and detach from window. default: false */
	public boolean useGLSurfaceView20API18 = false;
}