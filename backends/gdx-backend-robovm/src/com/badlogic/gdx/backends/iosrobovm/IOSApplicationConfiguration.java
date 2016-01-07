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

package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.apple.glkit.GLKViewDrawableColorFormat;
import org.robovm.apple.glkit.GLKViewDrawableDepthFormat;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat;

public class IOSApplicationConfiguration {
	/** whether to enable screen dimming. */
	public boolean preventScreenDimming = true;
	/** whether or not portrait orientation is supported. */
	public boolean orientationPortrait = true;
	/** whether or not landscape orientation is supported. */
	public boolean orientationLandscape = true;

	/** the color format, RGB565 is the default **/
	public GLKViewDrawableColorFormat colorFormat = GLKViewDrawableColorFormat.RGB565;

	/** the depth buffer format, Format16 is default **/
	public GLKViewDrawableDepthFormat depthFormat = GLKViewDrawableDepthFormat._16;

	/** the stencil buffer format, None is default **/
	public GLKViewDrawableStencilFormat stencilFormat = GLKViewDrawableStencilFormat.None;

	/** the multisample format, None is default **/
	public GLKViewDrawableMultisample multisample = GLKViewDrawableMultisample.None;

	/** number of frames per second, 60 is default **/
	public int preferredFramesPerSecond = 60;

	/** whether to use the accelerometer, default true **/
	public boolean useAccelerometer = true;
	/** the update interval to poll the accelerometer with, in seconds **/
	public float accelerometerUpdate = 0.05f;
	/** the update interval to poll the magnetometer with, in seconds **/
	public float magnetometerUpdate = 0.05f;

	/** whether to use the compass, default true **/
	public boolean useCompass = true;

	/** whether or not to allow background music from iPod **/
	public boolean allowIpod = false;
	
	/** whether or not the onScreenKeyboard should be closed on return key **/
	public boolean keyboardCloseOnReturn = true;

	/** Experimental, whether to enable OpenGL ES 3 if supported. If not supported it will fall-back to OpenGL ES 2.0.
	 *  When GL ES 3 is enabled, {@link com.badlogic.gdx.Gdx#gl30} can be used to access it's functionality.
	 * @deprecated this option is currently experimental and not yet fully supported, expect issues. */
	@Deprecated public boolean useGL30 = false;
}
