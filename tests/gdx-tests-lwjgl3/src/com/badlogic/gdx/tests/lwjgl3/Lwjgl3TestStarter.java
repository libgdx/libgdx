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

package com.badlogic.gdx.tests.lwjgl3;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Lwjgl3TestStarter extends JFrame {
	public Lwjgl3TestStarter () throws HeadlessException {
		super("LWJGL 3 libgdx Tests");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(new TestList());
		pack();
		setSize(getWidth() + 20, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/** Runs the {@link GdxTest} with the given name.
	 * @param testName the name of a test class
	 * @return {@code true} if the test was found and run, {@code false} otherwise */
	public static Lwjgl3Application runTest (String testName) {
		final GdxTest test = GdxTests.newTest(testName);
		if (test == null) throw new GdxRuntimeException("Test not found: " + testName);

		final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.width = 640;
		config.height = 480;
		config.title = testName;
		return new Lwjgl3Application(test, config);
	}

	class TestList extends JPanel {
		public TestList () {
			setLayout(new BorderLayout());

			final JButton button = new JButton("Run Test");

			final JList list = new JList(GdxTests.getNames().toArray());
			JScrollPane pane = new JScrollPane(list);

			DefaultListSelectionModel m = new DefaultListSelectionModel();
			m.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			m.setLeadAnchorNotificationEnabled(false);
			list.setSelectionModel(m);

			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked (MouseEvent event) {
					if (event.getClickCount() == 2) button.doClick();
				}
			});

			list.addKeyListener(new KeyAdapter() {
				public void keyPressed (KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) button.doClick();
				}
			});

			final Preferences prefs = new Lwjgl3Preferences(new FileHandle(new Lwjgl3Files().getExternalStoragePath()
				+ ".prefs/jglfw-tests"));
			list.setSelectedValue(prefs.getString("last", null), true);

			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					String testName = (String)list.getSelectedValue();
					prefs.putString("last", testName);
					prefs.flush();
					Lwjgl3TestStarter.this.setVisible(false);
					runTest(testName).addLifecycleListener(new LifecycleListener() {
						public void resume () {
						}

						public void pause () {
						}

						public void dispose () {
							
							Lwjgl3TestStarter.this.setVisible(true);
						}
					});
				}
			});

			add(pane, BorderLayout.CENTER);
			add(button, BorderLayout.SOUTH);
		}
	}

	/** Runs a libgdx test.
	 * 
	 * If no arguments are provided on the command line, shows a list of tests to choose from. If an argument is present, the test
	 * with that name will immediately be run.
	 * 
	 * @param argv command line arguments */
	public static void main (String[] argv) throws Exception {
		if (argv.length > 0) {
			runTest(argv[0]);
			return;
		}
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new Lwjgl3TestStarter();
	}
}
