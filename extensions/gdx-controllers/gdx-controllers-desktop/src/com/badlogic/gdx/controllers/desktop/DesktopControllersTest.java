
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
