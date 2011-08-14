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