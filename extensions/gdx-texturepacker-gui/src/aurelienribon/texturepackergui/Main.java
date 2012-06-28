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
package aurelienribon.texturepackergui;

import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.io.FileUtils;

public class Main {
    public static void main(String[] args) {
		Parameters params = new Parameters(args);
		Project project = params.project;

		if (project == null) {
			String str = "";
			str += "input=" + params.input + "\n";
			str += "output=" + params.output + "\n";
			str += params.settings;
			project = Project.fromString(str);
		}

		if (params.silent) {
			if (project.input.equals("") || project.output.equals("")) {
				System.err.println("Input and output directories have to be set");
			} else  {
				try {
					project.pack();
				} catch (GdxRuntimeException ex) {
					System.err.println("Packing unsuccessful. " + ex.getMessage());
				}
			}
			return;
		}

		final Project prj = project;
		SwingUtilities.invokeLater(new Runnable() {@Override public void run() {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException ex) {
			} catch (InstantiationException ex) {
			} catch (IllegalAccessException ex) {
			} catch (UnsupportedLookAndFeelException ex) {
			}

			Canvas canvas = new Canvas();
			LwjglCanvas glCanvas = new LwjglCanvas(canvas, true);

			MainWindow mw = new MainWindow(prj, canvas, glCanvas.getCanvas());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			mw.setSize(
				Math.min(1100, screenSize.width - 100),
				Math.min(670, screenSize.height - 100)
			);
			mw.setLocationRelativeTo(null);
			mw.setVisible(true);
		}});
    }

	private static class Parameters {
		public boolean silent = false;
		public Project project = null;
		public String input;
		public String output;
		public String packName;
		public String settings;

		public Parameters(String[] args) {
			input = get(args, "-input", "");
			output = get(args, "-output", "");
			packName = get(args, "-packName", "pack");
			silent = check(args, "-silent");

			try {
				String projectPath = get(args, "-project", null);
				if (projectPath != null) project = Project.fromFile(new File(projectPath));
			} catch (IOException ex) {
				System.err.println("Cannot read the specified project file.");
			}

			try {
				String settingsPath = get(args, "-settings", null);
				if (settingsPath != null) settings = FileUtils.readFileToString(new File(settingsPath));
				else settings = "";
			} catch (IOException ex) {
				System.err.println("Cannot read the specified settings file.");
			}
		}

		private String get(String[] args, String arg, String defaultValue) {
			for (int i=0; i<args.length-1; i++) {
				if (args[i].equals(arg)) return args[i+1];
			}
			return defaultValue;
		}

		private boolean check(String[] args, String arg) {
			for (int i=0; i<args.length; i++) {
				if (args[i].equals(arg)) return true;
			}
			return false;
		}
	}
}