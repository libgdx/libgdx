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

package com.badlogic.gdx.controllers.desktop.ois;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.controllers.desktop.DesktopControllersBuild;
import com.badlogic.gdx.controllers.desktop.OisControllers;
import com.badlogic.gdx.controllers.desktop.ois.OisJoystick.OisPov;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class OisTest {
	public static void main (String[] args) throws Exception {
		DesktopControllersBuild.main(null);
		new SharedLibraryLoader("libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");

		ApplicationAdapter app = new ApplicationAdapter() {
			Ois ois;

			public void create () {
				ois = new Ois(OisControllers.getWindowHandle());
				if (ois.getJoysticks().size() > 0) {
					ois.getJoysticks().get(0).setListener(new OisListener() {
						@Override
						public void xSliderMoved (OisJoystick joystick, int slider, boolean value) {
							System.out.println("xSliderMoved: " + slider + ", " + value);
						}

						@Override
						public void ySliderMoved (OisJoystick joystick, int slider, boolean value) {
							System.out.println("ySliderMoved: " + slider + ", " + value);
						}

						@Override
						public void povMoved (OisJoystick joystick, int pov, OisPov value) {
							System.out.println("povMoved: " + pov + ", " + value);

						}

						@Override
						public void buttonReleased (OisJoystick joystick, int button) {
							System.out.println("buttonReleased: " + button);
						}

						@Override
						public void buttonPressed (OisJoystick joystick, int button) {
							System.out.println("buttonPressed: " + button);
						}

						@Override
						public void axisMoved (OisJoystick joystick, int axis, float value) {
							System.out.println("axisMoved: " + axis + ", " + value);
						}
					});
				}
			}

			public void render () {
				ois.update();
			}
		};

		// new LwjglApplication(app);
// new LwjglFrame(app, "Controllers", 200, 200, false);
	}
}
