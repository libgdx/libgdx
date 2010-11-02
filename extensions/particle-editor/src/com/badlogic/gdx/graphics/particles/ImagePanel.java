
package com.badlogic.gdx.graphics.particles;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

class ImagePanel extends EditorPanel {
	JLabel imageLabel;
	JLabel widthLabel;
	JLabel heightLabel;
	String lastDir;

	public ImagePanel (final ParticleEditor editor) {
		super("Image", null);
		JPanel contentPanel = getContentPanel();
		{
			JButton openButton = new JButton("Open");
			contentPanel.add(openButton, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 6, 6), 0, 0));
			openButton.addActionListener(new ActionListener() {
				public void actionPerformed (ActionEvent event) {
					FileDialog dialog = new FileDialog(editor, "Open Image", FileDialog.LOAD);
					if (lastDir != null) dialog.setDirectory(lastDir);
					dialog.setVisible(true);
					final String file = dialog.getFile();
					final String dir = dialog.getDirectory();
					if (dir == null || file == null || file.trim().length() == 0) return;
					lastDir = dir;
					try {
						ImageIcon icon = new ImageIcon(new File(dir, file).toURI().toURL());
						final ParticleEmitter emitter = editor.getEmitter();
						editor.setIcon(emitter, icon);
						imageLabel.setIcon(icon);
						widthLabel.setText("Width: " + icon.getIconWidth());
						heightLabel.setText("Height: " + icon.getIconHeight());
						revalidate();
						emitter.setImagePath(new File(dir, file).getAbsolutePath());
						emitter.setTexture(null);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		{
			widthLabel = new JLabel();
			contentPanel.add(widthLabel, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 6, 6), 0, 0));
		}
		{
			heightLabel = new JLabel();
			contentPanel.add(heightLabel, new GridBagConstraints(2, 3, 1, 1, 0, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 6), 0, 0));
		}
		{
			imageLabel = new JLabel();
			contentPanel.add(imageLabel, new GridBagConstraints(3, 1, 1, 3, 1, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
		}
		ImageIcon icon = editor.getIcon(editor.getEmitter());
		if (icon != null) {
			imageLabel.setIcon(icon);
			widthLabel.setText("Width: " + icon.getIconWidth());
			heightLabel.setText("Height: " + icon.getIconHeight());
		}
	}
}
