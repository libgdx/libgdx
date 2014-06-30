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

package com.badlogic.gdx.tools.pathological;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.concurrent.ExecutionException;

class About extends JFrame {

	private JPanel contentPane;
	private JLabel appNameLabel;
	private JTextPane aboutText;
	private JButton authorLink;
	private JButton btnLibgdx;
	private JButton btnNewButton;

	/** Create the frame. */
	About () {
		super("About Pathological");
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {223, 0, 0};
		gbl_contentPane.rowHeights = new int[] {0, 130, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		appNameLabel = new JLabel("Pathological");
		appNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		appNameLabel.setFont(appNameLabel.getFont().deriveFont(4));
		GridBagConstraints gbc_appNameLabel = new GridBagConstraints();
		gbc_appNameLabel.gridwidth = 2;
		gbc_appNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_appNameLabel.fill = GridBagConstraints.BOTH;
		gbc_appNameLabel.gridx = 0;
		gbc_appNameLabel.gridy = 0;
		contentPane.add(appNameLabel, gbc_appNameLabel);

		aboutText = new JTextPane();
		aboutText.setDisabledTextColor(UIManager.getColor("Button.foreground"));
		aboutText.setEnabled(false);
		aboutText.setEditable(false);
		aboutText.setBackground(UIManager.getColor("Button.background"));
		aboutText
			.setText("An all-purpose 2D Path editor for LibGDX.\n\nCarve your curves.  Sculpt your splines.  Pimp your paths.  Be the envy of all game designers!  For graphics, physics, AI, bear wrestling, and anything else your heart desires!  Make your players cry tears of joy when they see just how silky smooth you can make your game with this little tool.\n\nSource code is available under the terms of the Apache License 2.0 (the license LibGDX uses).");
		GridBagConstraints gbc_aboutText = new GridBagConstraints();
		gbc_aboutText.gridwidth = 2;
		gbc_aboutText.insets = new Insets(0, 0, 5, 0);
		gbc_aboutText.fill = GridBagConstraints.BOTH;
		gbc_aboutText.gridx = 0;
		gbc_aboutText.gridy = 1;
		contentPane.add(aboutText, gbc_aboutText);

		authorLink = new JButton("Made by JesseTG");
		authorLink.setDefaultCapable(false);
		authorLink.setFocusTraversalKeysEnabled(false);
		authorLink.setFocusPainted(false);
		authorLink.setFocusable(false);
		authorLink.setToolTipText("Check out the other things the author has done.");
		authorLink.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = java.awt.Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.BROWSE)) {
						new LinkRunner(URI.create("https://www.github.com/JesseTG")).execute();
					}
				}
			}
		});
		authorLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		authorLink.setForeground(Color.BLUE);
		authorLink.setContentAreaFilled(false);
		authorLink.setBorderPainted(false);
		authorLink.setBorder(null);
		GridBagConstraints gbc_authorLink = new GridBagConstraints();
		gbc_authorLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_authorLink.insets = new Insets(0, 0, 5, 5);
		gbc_authorLink.gridx = 0;
		gbc_authorLink.gridy = 2;
		contentPane.add(authorLink, gbc_authorLink);

		btnLibgdx = new JButton("Made for LibGDX");
		btnLibgdx.setFocusPainted(false);
		btnLibgdx.setFocusTraversalKeysEnabled(false);
		btnLibgdx.setFocusable(false);
		btnLibgdx.setDefaultCapable(false);
		btnLibgdx.setToolTipText("See what this awesome game dev library can do for you!");
		btnLibgdx.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLibgdx.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = java.awt.Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.BROWSE)) {
						new LinkRunner(URI.create("http://libgdx.badlogicgames.com/")).execute();
					}
				}
			}
		});
		btnLibgdx.setForeground(Color.BLUE);
		btnLibgdx.setContentAreaFilled(false);
		btnLibgdx.setBorderPainted(false);
		btnLibgdx.setBorder(null);
		GridBagConstraints gbc_btnLibgdx = new GridBagConstraints();
		gbc_btnLibgdx.insets = new Insets(0, 0, 5, 0);
		gbc_btnLibgdx.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLibgdx.gridx = 1;
		gbc_btnLibgdx.gridy = 2;
		contentPane.add(btnLibgdx, gbc_btnLibgdx);

		btnNewButton = new JButton("OK");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				About.this.setVisible(false);
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 3;
		contentPane.add(btnNewButton, gbc_btnNewButton);
	}

	private static class LinkRunner extends SwingWorker<Void, Void> {

		private final URI uri;

		private LinkRunner (URI u) {
			if (u == null) {
				throw new NullPointerException();
			}
			uri = u;
		}

		@Override
		protected Void doInBackground () throws Exception {
			Desktop desktop = java.awt.Desktop.getDesktop();
			desktop.browse(uri);
			return null;
		}

		@Override
		protected void done () {
			try {
				get();
			} catch (ExecutionException ee) {
				handleException(uri, ee);
			} catch (InterruptedException ie) {
				handleException(uri, ie);
			}
		}

		private static void handleException (URI u, Exception e) {
			JOptionPane.showMessageDialog(null,
				"Sorry, a problem occurred while trying to open this link in your system's standard browser.", "A problem occured",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
