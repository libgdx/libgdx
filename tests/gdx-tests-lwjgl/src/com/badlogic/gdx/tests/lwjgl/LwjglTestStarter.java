/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests.lwjgl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTests;

public class LwjglTestStarter {
	static class TestList extends JPanel {
		public TestList () {
			setLayout(new BorderLayout());

			final JList list = new JList(GdxTests.getNames());
			final JButton button = new JButton("Run Test");
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

			button.addActionListener(new ActionListener() {
				@Override public void actionPerformed (ActionEvent e) {
					String testName = (String)list.getSelectedValue();
					GdxTest test = GdxTests.newTest(testName);
					new LwjglApplication(test, testName, 480, 320, test.needsGL20());
				}
			});

			add(pane, BorderLayout.CENTER);
			add(button, BorderLayout.SOUTH);		
		}
	}

	public static void main (String[] argv) {
		JFrame frame = new JFrame("GDX - LWJGL Test Launcher");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new TestList());
		frame.pack();
		frame.setSize(frame.getWidth(), 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
