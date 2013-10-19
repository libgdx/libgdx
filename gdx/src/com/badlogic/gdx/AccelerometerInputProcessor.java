/*******************************************************************************
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
 * 
 * @author <a href="www.sadafnoor.com">Md. Sadaf Noor</a>
 ******************************************************************************/

package com.badlogic.gdx;

import com.badlogic.gdx.Input.Buttons;

/** An AccelerometerInputProcessor is used to receive input events from the keyboard, the touch screen (mouse on the desktop) and the Accelerometer (of android device). For this it
 * has to be registered with the {@link Input#setInputProcessor(AccInputProcessor)} method. It will be called each frame before the
 * call to {@link ApplicationListener#render()}. Each method returns a boolean in case you want to use this with the {@link InputMultiplexer}
 * to chain input processors.
 * 
 * @author <a href="www.sadafnoor.com">Md. Sadaf Noor</a> */
public interface AccelerometerInputProcessor extends InputProcessor{
	/** Called when accelerometer tilted at x axis
	 * 
	 * @param x accelerometer tilted at x axis
	 * @return whether rotated or not */
	public boolean onTiltX(float x);
	/** Called when accelerometer tilted at y axis
	 * 
	 * @param x accelerometer tilted at y axis
	 * @return whether rotated or not */
	public boolean onTiltY(float y);
	
	/** Called when accelerometer tilted at z axis
	 * 
	 * @param x accelerometer tilted at z axis
	 * @return whether rotated or not */
	public boolean onTiltZ(float z);
	
}
