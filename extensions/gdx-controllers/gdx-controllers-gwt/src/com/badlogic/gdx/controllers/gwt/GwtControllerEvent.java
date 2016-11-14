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

package com.badlogic.gdx.controllers.gwt;

import com.badlogic.gdx.controllers.PovDirection;

public class GwtControllerEvent {
	
	public static final int BUTTON_DOWN = 0;
	public static final int BUTTON_UP = 1;
	public static final int AXIS = 2;
	public static final int POV = 3;
	public static final int CONNECTED = 4;
	public static final int DISCONNECTED = 5;

	/** the controller the event belongs to **/
	public GwtController controller;
	/** the event type, see constants above **/
	public int type;
	/** the code for the even source, e.g. button keycode, axis index **/
	public int code;
	/** the value if this is an #AXIS, a #BUTTON_DOWN, or a #BUTTON_UP event **/
	public float amount;
	/** the POV direction if this is a #POV event **/ 
	public PovDirection povDirection;
	
}