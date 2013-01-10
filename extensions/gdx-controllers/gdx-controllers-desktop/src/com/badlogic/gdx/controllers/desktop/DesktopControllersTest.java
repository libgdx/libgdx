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
package com.badlogic.gdx.controllers.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglFrame;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class DesktopControllersTest {
	public static void main (String[] args) throws Exception {
		DesktopControllersBuild.main(null);
		new SharedLibraryLoader("libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");

		ApplicationAdapter app = new ApplicationAdapter() {
			public void create () {
				Controllers.addListener(new ControllerListener() {
					public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value) {
						System.out.println("ySliderMoved: " + sliderIndex + ", " + value);
						return true;
					}

					public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value) {
						System.out.println("xSliderMoved: " + sliderIndex + ", " + value);
						return true;
					}

					public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
						System.out.println("povMoved: " + povIndex + ", " + value);
						return true;
					}

					public boolean buttonUp (Controller controller, int buttonIndex) {
						System.out.println("buttonUp: " + buttonIndex);
						return true;
					}

					public boolean buttonDown (Controller controller, int buttonIndex) {
						System.out.println("buttonDown: " + buttonIndex);
						return true;
					}

					public boolean axisMoved (Controller controller, int axisIndex, float value) {
						System.out.println("axisMoved: " + axisIndex + ", " + value);
						return true;
					}

					public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
						System.out.println("accelerometerMoved: " + accelerometerIndex + ", " + value);
						return true;
					}

					@Override
					public void connected (Controller controller) {
					}

					@Override
					public void disconnected (Controller controller) {
					}
				});
			}

			public void render () {
				for (Controller controller : Controllers.getControllers())
					if (controller.getButton(0)) System.out.println("button 0 pressed");
			}
		};

		// new LwjglApplication(app);
		new LwjglFrame(app, "Controllers", 200, 200, false);
	}
}