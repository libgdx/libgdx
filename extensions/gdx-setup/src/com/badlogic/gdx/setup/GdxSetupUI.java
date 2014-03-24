
package com.badlogic.gdx.setup;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GdxSetupUI extends JFrame {
	UI ui = new UI();

	public GdxSetupUI () {
		setTitle("LibGDX Project Generator");
		setLayout(new BorderLayout());
		add(ui, BorderLayout.CENTER);
		setSize(620, 480);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	void generate () {
		final String name = ui.form.nameText.getText().trim();
		if (name.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a project name.");
			return;
		}

		final String pack = ui.form.packageText.getText().trim();
		if (pack.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a package name.");
			return;
		}

		final String clazz = ui.form.gameClassText.getText().trim();
		if (clazz.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a game class name.");
			return;
		}

		final String destination = ui.form.destinationText.getText().trim();
		if (destination.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a destination directory.");
			return;
		}
		final String sdkLocation = ui.form.sdkLocationText.getText().trim();
		if (sdkLocation.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter in your android sdk path");
			return;
		}

		new Thread("DoShit") {
			public void run () {
				log("Generating app in " + destination);
				new GdxSetup().build(destination, name, pack, clazz, sdkLocation);
				log("Done!");
				log("To import in Eclipse: File -> Import -> Gradle -> Gradle Project");
				log("To import to Intellij IDEA: File -> Import -> build.gradle");
			}
		}.start();
	}

	void log (final String text) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				ui.textArea.append(text + "\n");
				ui.textArea.setCaretPosition(ui.textArea.getDocument().getLength());
			}
		});
	}

	class UI extends JPanel {
		Form form = new Form();
		JButton generateButton = new JButton("Generate");
		JTextArea textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		JLabel logo;

		{
			try {
				BufferedImage img = ImageIO.read(GdxSetupUI.class.getResourceAsStream("/com/badlogic/gdx/setup/logo.png"));
				ImageIcon icon = new ImageIcon(img);
				logo = new JLabel(icon);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			textArea.setEditable(false);
			uiLayout();
			uiEvents();
		}

		private void uiLayout () {
			setLayout(new GridBagLayout());

			add(logo, new GridBagConstraints(0, 0, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 6, 12, 6), 0, 0));
			add(form, new GridBagConstraints(0, 1, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(6, 6, 12, 6), 0, 0));
			add(generateButton, new GridBagConstraints(0, 2, 1, 1, 1, 0, CENTER, NONE, new Insets(0, 6, 12, 6), 0, 0));
			add(scrollPane, new GridBagConstraints(0, 3, 1, 1, 1, 1, CENTER, BOTH, new Insets(0, 6, 6, 6), 0, 0));
		}

		private void uiEvents () {
			generateButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
					generate();
				}
			});
		}
	}

	class Form extends JPanel {
		JLabel nameLabel = new JLabel("Name:");
		JTextField nameText = new JTextField("my-gdx-game");
		JLabel packageLabel = new JLabel("Package:");
		JTextField packageText = new JTextField("com.mygdx.game");
		JLabel gameClassLabel = new JLabel("Game class:");
		JTextField gameClassText = new JTextField("MyGdxGame");
		JLabel destinationLabel = new JLabel("Destination:");
		JTextField destinationText = new JTextField(new File("test").getAbsolutePath());
		JButton destinationButton = new JButton("Browse");
		JLabel sdkLocationLabel = new JLabel("Android SDK");
		JTextField sdkLocationText = new JTextField("C:\\Path\\To\\Your\\Sdk");
		JButton sdkLocationButton = new JButton("Browse");

		{
			uiLayout();
			uiEvents();
		}

		private void uiLayout () {
			setLayout(new GridBagLayout());										

			add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(nameText, new GridBagConstraints(1, 0, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(packageLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(packageText, new GridBagConstraints(1, 1, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(gameClassLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 6, 6), 0, 0));
			add(gameClassText, new GridBagConstraints(1, 2, 2, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 6, 0), 0, 0));

			add(destinationLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 0, 6), 0, 0));
			add(destinationText, new GridBagConstraints(1, 3, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(destinationButton, new GridBagConstraints(2, 3, 1, 1, 0, 0, CENTER, NONE, new Insets(0, 6, 0, 0), 0, 0));

			add(sdkLocationLabel, new GridBagConstraints(0, 4, 1, 1, 0, 0, EAST, NONE, new Insets(0, 0, 0, 6), 0, 0));
			add(sdkLocationText, new GridBagConstraints(1, 4, 1, 1, 1, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(sdkLocationButton, new GridBagConstraints(2, 4, 1, 1, 0, 0, CENTER, NONE, new Insets(0, 6, 0, 0), 0, 0));
        }

		private void uiEvents () {
			destinationButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent e) {
//					System.setProperty("apple.awt.fileDialogForDirectories", "true");
//					FileDialog dialog = new FileDialog(GdxSetupUI.this, "Choose destination", FileDialog.LOAD);
//					dialog.setVisible(true);					
//					String name = dialog.getFile();
//					String dir = dialog.getDirectory();
//					if (name == null || dir == null) return;
					
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Chose destination");				
					int result = chooser.showOpenDialog(null);
					if(result == JFileChooser.APPROVE_OPTION) {
						File dir = chooser.getSelectedFile();
						if(dir == null) return;
						if(dir.getAbsolutePath().trim().length() == 0) return;
						destinationText.setText(dir.getAbsolutePath());
					}
					
				}
			});
			sdkLocationButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setDialogTitle("Chose destination");
					int result = chooser.showOpenDialog(null);
					if(result == JFileChooser.APPROVE_OPTION) {
						File dir = chooser.getSelectedFile();
						if(dir == null) return;
						if(dir.getAbsolutePath().trim().length() == 0) return;
						sdkLocationText.setText(dir.getAbsolutePath());
					}
				}
			});
		}
	}

	public static void main (String[] args) throws Exception {
		new GdxSetupUI();
	}
}
