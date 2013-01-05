
package com.badlogic.gdx.controllers.desktop.ois;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.controllers.DesktopControllersBuild;
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
				if(ois.getJoysticks().size() > 0) {
					ois.getJoysticks().get(0).setListener(new OisListener() {
						
						@Override
						public void sliderMoved (OisJoystick joystick, int slider, boolean x, boolean y) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void povMoved (OisJoystick joystick, int pov, OisPov direction) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void buttonReleased (OisJoystick joystick, int button) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void buttonPressed (OisJoystick joystick, int button) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void axisMoved (OisJoystick joystick, int axis, float value) {
							// TODO Auto-generated method stub
							
						}
					});
				}
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
