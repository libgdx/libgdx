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
import com.badlogic.gdx.utils.GdxNativesLoader;

/** Class defining the configuration of an {@link AndroidApplication}. Allows you to disable the use of the accelerometer to save
 * battery among other things.
 * @author mzechner */
public class AndroidApplicationConfiguration {
	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;

	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;

	/** number of samples for CSAA/MSAA, 2 is a good value **/
	public int numSamples = 0;

	/** whether coverage sampling anti-aliasing is used. in that case you have to clear the coverage buffer as well! */
	public boolean coverageSampling = false;

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

	/** whether to keep the screen on and at full brightness or not while running the application. default: false. Uses
	 * FLAG_KEEP_SCREEN_ON under the hood. */
	public boolean useWakelock = false;

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
	public boolean useImmersiveMode = true;

	/** Sets which OpenGL ES version. If the set version is not supported on the device, it will fall back to the OpenGL ES version
	 * that the device supports. The OpenGL ES 3.2 requires at least Android 7.0 (API level 24). */
	public GLES gles = GLES.GLES20;

	/** The maximum number of threads to use for network requests. Default is {@link Integer#MAX_VALUE}. */
	public int maxNetThreads = Integer.MAX_VALUE;

	/** set this to true to render under the display cutout. Use the Graphics::getSafeInsetXX to get the safe render space */
	public boolean renderUnderCutout = false;

	/** The loader used to load native libraries. Override this to use a different loading strategy. */
	public GdxNativeLoader nativeLoader = new GdxNativeLoader() {
		@Override
		public void load () {
			GdxNativesLoader.load();
		}
	};
}
