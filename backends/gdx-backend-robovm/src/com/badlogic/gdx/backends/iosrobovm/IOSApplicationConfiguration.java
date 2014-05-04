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

	/** Scale factor to use on large screens with retina display, i.e. iPad 3+ (has no effect on non-retina screens).
	 * <ul>
	 * <li>1.0 = no scaling (everything is in pixels)
	 * <li>0.5 = LibGDX will behave as you would only have half the pixels. I.e. instead of 2048x1536 you will work in 1024x768.
	 * This looks pixel perfect and will save you the trouble to create bigger graphics for the retina display.
	 * <li>any other value: scales the screens according to your scale factor. A scale factor oof 0.75, 0.8, 1.2, 1.5 etc. works
	 * very well without any artifacts!
	 * </ul> */
	public float displayScaleLargeScreenIfRetina = 1.0f;
	/** Scale factor to use on small screens with retina display, i.e. iPhone 4+, iPod 4+ (has no effect on non-retina screens).
	 * <ul>
	 * <li>1.0 = no scaling (everything is in pixels)
	 * <li>0.5 = LibGDX will behave as you would only have half the pixels. I.e. instead of 960x640 you will work in 480x320. This
	 * looks pixel perfect and will save you the trouble to create bigger graphics for the retina display.
	 * <li>any other value: scales the screens according to your scale factor. A scale factor of 0.75, 0.8, 1.2, 1.5 etc. works
	 * very well without any artifacts!
	 * </ul> */
	public float displayScaleSmallScreenIfRetina = 1.0f;
	/** Scale factor to use on large screens without retina display, i.e. iPad 1+2 (has no effect on retina screens).
	 * <ul>
	 * <li>1.0 = no scaling (everything is in pixels)
	 * <li>any other value: scales the screens according to your scale factor. A scale factor of 0.75, 0.8, 1.2, 1.5 etc. works
	 * very well without any artifacts!
	 * </ul> */
	public float displayScaleLargeScreenIfNonRetina = 1.0f;
	/** Scale factor to use on small screens without retina display, i.e. iPhone 1-3, iPod 1-3 (has no effect on retina screens).
	 * <ul>
	 * <li>1.0 = no scaling (everything is in pixels)
	 * <li>any other value: scales the screens according to your scale factor. A scale factor of 0.75, 0.8, 1.2, 1.5 etc. works
	 * very well without any artifacts!
	 * </ul> */
	public float displayScaleSmallScreenIfNonRetina = 1.0f;

	/** whether to use the accelerometer, default true **/
	public boolean useAccelerometer = true;
	/** the update interval to poll the accelerometer with, in seconds **/
	public float accelerometerUpdate = 0.05f;

	/** whether to use the compass, default true **/
	public boolean useCompass = true;

	/** whether or not to allow background music from iPod **/
	public boolean allowIpod = false;
}
