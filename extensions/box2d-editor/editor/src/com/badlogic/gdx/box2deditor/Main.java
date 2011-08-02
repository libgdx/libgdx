package com.badlogic.gdx.box2deditor;

import com.badlogic.gdx.box2deditor.ui.MainWindow;
import com.badlogic.gdx.box2deditor.renderpanel.App;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.SwingUtilities;

/**
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class Main {
    public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				parseArgs(args);

				LwjglCanvas glCanvas = new LwjglCanvas(App.instance(), false);

				MainWindow mw = new MainWindow(glCanvas);
				mw.setTitle("Box2D Editor");

				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				mw.setSize(
					Math.min(1200, screenSize.width - 100),
					Math.min(800, screenSize.height - 100)
				);
				mw.setLocationRelativeTo(null);
				mw.setVisible(true);
			}
		});
    }

	private static void parseArgs(String[] args) {
		for (int i=0; i<args.length; i++) {
			if (args[i].startsWith("--outputfile=")) {
				AppContext.instance().outputFile = new File(args[i].substring("--outputfile=".length()));
			}
		}
	}
}
