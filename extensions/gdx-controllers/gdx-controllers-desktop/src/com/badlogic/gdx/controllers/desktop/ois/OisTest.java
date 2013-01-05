
package com.badlogic.gdx.controllers.desktop.ois;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.controllers.desktop.DesktopControllersBuild;
import com.badlogic.gdx.controllers.desktop.ois.OisJoystick.OisPov;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class OisTest {
	public static void main (String[] args) throws Exception {
		DesktopControllersBuild.main(null);
		new SharedLibraryLoader("libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");

		ApplicationAdapter app = new ApplicationAdapter() {
			Ois ois;

			@Override
			public void create () {
				this.ois = new Ois();
				System.out.println(ois.getVersionNumber());
				System.out.println(ois.getVersionName());
				System.out.println(ois.getInputSystemName());
				ois.getJoysticks().get(0).setListener(new OisListener() {
					@Override
					public void sliderMoved (OisJoystick joystick, int slider, boolean x, boolean y) {
						System.out.println("slider " + slider + " moved, " + x + ", " + y);
					}

					@Override
					public void povMoved (OisJoystick joystick, int pov, OisPov direction) {
						System.out.println("pov " + pov + " moved, " + direction);
					}

					@Override
					public void buttonReleased (OisJoystick joystick, int button) {
						System.out.println("button " + button + " released");
					}

					@Override
					public void buttonPressed (OisJoystick joystick, int button) {
						System.out.println("button " + button + " pressed");
					}

					@Override
					public void axisMoved (OisJoystick joystick, int axis, float value) {
						System.out.println("axis " + axis + " moved, " + value);
					}
				});
			}

			public void render () {
				ois.update();
			}
		};

		new LwjglApplication(app);
// new LwjglFrame(app, "Controllers", 200, 200, false);

// final JFrame frame = new JFrame("FrameDemo");
// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// frame.pack();
// frame.setVisible(true);
// SwingUtilities.invokeLater(new Runnable() {
// @Override
// public void run () {
// OisWrapper.initialize(getWindowId(frame));
// }
// });
	}
}
