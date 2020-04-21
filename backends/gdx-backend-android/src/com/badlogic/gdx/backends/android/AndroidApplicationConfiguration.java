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

import android.hardware.SensorManager;
import android.media.SoundPool;

import com.badlogic.gdx.Input;
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
	
	/** whether to use the gyroscope. default: false **/
	public boolean useGyroscope = false;

	/** Whether to use the compass. The compass enables {@link Input#getRotationMatrix(float[])}, {@link Input#getAzimuth()},
	 * {@link Input#getPitch()}, and {@link Input#getRoll()} if {@link #useAccelerometer} is also true.
	 * <p>
	 * If {@link #useRotationVectorSensor} is true and the rotation vector sensor is available, the compass will not be used.
	 * <p>
	 * Default: true **/
	public boolean useCompass = true;

	/** Whether to use Android's rotation vector software sensor, which provides cleaner data than that of {@link #useCompass} for
	 * {@link Input#getRotationMatrix(float[])}, {@link Input#getAzimuth()}, {@link Input#getPitch()}, and {@link Input#getRoll()}.
	 * The rotation vector sensor uses a combination of physical sensors, and it pre-filters and smoothes the data. If true,
	 * {@link #useAccelerometer} is not required to enable rotation data.
	 * <p>
	 * If true and the rotation vector sensor is available, the compass will not be used, regardless of {@link #useCompass}.
	 * <p>
	 * Default: false */
	public boolean useRotationVectorSensor = false;
	
	/** The requested sensor sampling rate in microseconds or one of the {@code SENSOR_DELAY_*} constants in {@link SensorManager}. 
	 * <p>
	 * Default: {@link SensorManager#SENSOR_DELAY_GAME} (20 ms updates). */
	public int sensorDelay = SensorManager.SENSOR_DELAY_GAME;

	/** the time in milliseconds to sleep after each event in the touch handler, set this to 16ms to get rid of touch flooding on
	 * pre Android 2.0 devices. default: 0 **/
	public int touchSleepTime = 0;

	/** whether to keep the screen on and at full brightness or not while running the application. default: false. Uses FLAG_KEEP_SCREEN_ON under the hood. */
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

	/** Experimental, whether to enable OpenGL ES 3 if supported. If not supported it will fall-back to OpenGL ES 2.0.
	 *  When GL ES 3* is enabled, {@link com.badlogic.gdx.Gdx#gl30} can be used to access its functionality. Requires at least Android 4.3 (API level 18).
  	 * @deprecated this option is currently experimental and not yet fully supported, expect issues. */
	@Deprecated public boolean useGL30 = false;

	/** The maximum number of threads to use for network requests. Default is {@link Integer#MAX_VALUE}. */
	public int maxNetThreads = Integer.MAX_VALUE;
}
