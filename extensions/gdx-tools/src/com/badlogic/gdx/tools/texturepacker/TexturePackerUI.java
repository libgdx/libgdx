/*******************************************************************************
 * Copyright 2015 Lynk Lin
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
package com.badlogic.gdx.tools.texturepacker;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import java.awt.Toolkit;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class TexturePackerUI extends JFrame {
	private static final String TITLE ="Gdx Texture Packer";
	
	private JCheckBox uiPot;
	private JSpinner uiMinWidth;
	private JSpinner uiMinHeight;
	private JSpinner uiPaddingX;
	private JSpinner uiPaddingY;
	private JCheckBox uiDuplicatePadding;
	private JCheckBox uiSquare;
	private JCheckBox uiStripWhitespaceX;
	private JCheckBox uiStripWhitespaceY;
	private JSpinner uiAlphaThreshold;
	private JComboBox<TextureFilter> uiFilterMin;
	private JComboBox<TextureFilter> uiFilterMag;
	private JComboBox<TextureWrap> uiWrapX;
	private JComboBox<TextureWrap> uiWrapY;
	private JComboBox<Pixmap.Format> uiFormat;
	private JComboBox<String> uiOutputFormat;
	private JSpinner uiJpegQuality;
	private JCheckBox uiAlias;
	private JCheckBox uiIgnoreBlankImages;
	private JCheckBox uiRotation;
	private JCheckBox uiCombineSubdirectories;
	private JCheckBox uiFast;
	private JCheckBox uiDebug;
	private JSpinner uiMaxWidth;
	private JSpinner uiMaxHeight;
	private JCheckBox uiEdgePadding;
	private JFileChooser uiFileChooser;
	private JTextField uiPackName;
	private JTextField uiInput;
	private JTextField uiOutput;

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				try {
					TexturePackerUI frame = new TexturePackerUI();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TexturePackerUI () {
		initComponents();
		initListeners();
		settingsToUi(null);
	}
	
	private void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(726, 610);
		setTitle(TITLE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			getContentPane().add(toolBar, BorderLayout.NORTH);
			{
				JButton uiNew = new JButton("New");
				uiNew.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/add.png")));
				uiNew.setFocusable(false);
				toolBar.add(uiNew);
			}
			toolBar.addSeparator();
			{
				JButton uiOpen = new JButton("Open");
				uiOpen.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/open.png")));
				uiOpen.setFocusable(false);
				toolBar.add(uiOpen);
			}
			toolBar.addSeparator();
			{
				JButton uiSave = new JButton("Save");
				uiSave.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/save.png")));
				uiSave.setFocusable(false);
				toolBar.add(uiSave);
			}
			toolBar.addSeparator();
			{
				JButton uiRename = new JButton("Rename Selected");
				uiRename.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/rename.png")));
				uiRename.setFocusable(false);
				toolBar.add(uiRename);
			}
		}
		{
			JPanel centerPanel = new JPanel();
			getContentPane().add(centerPanel, BorderLayout.CENTER);
			centerPanel.setLayout(new MigLayout("", "[grow]", "[]"));
			{
				JPanel panel = new JPanel();
				panel.setBorder(new LineBorder(new Color(0, 0, 0)));
				centerPanel.add(panel, "cell 0 0,grow");
				panel.setLayout(new MigLayout("", "[][grow][]", "[][][][][]"));
				{
					JLabel label = new JLabel("Pack Name");
					panel.add(label, "cell 0 0");
				}
				{
					uiPackName = new JTextField();
					panel.add(uiPackName, "cell 1 0 2 1,growx");
				}
				{
					JLabel label = new JLabel("Input Directory");
					panel.add(label, "cell 0 1");
				}
				{
					uiInput = new JTextField();
					panel.add(uiInput, "cell 1 1,growx");
				}
				{
					JButton uiSelectInput = new JButton("...");
					uiSelectInput.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							uiSelectInputActionPerformed(e);
						}
					});
					panel.add(uiSelectInput, "cell 2 1");
				}
				{
					JLabel label = new JLabel("Output Directory");
					panel.add(label, "cell 0 2");
				}
				{
					uiOutput = new JTextField();
					panel.add(uiOutput, "cell 1 2,growx");
				}
				{
					JButton uiSelectOutput = new JButton("...");
					uiSelectOutput.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							uiSelectOutputActionPerformed(e);
						}
					});
					panel.add(uiSelectOutput, "cell 2 2");
				}
				{
					JButton uiPack = new JButton("Pack");
					uiPack.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							uiPackActionPerformed(e);
						}
					});
					panel.add(uiPack, "cell 0 3 3 1,alignx right");
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				centerPanel.add(panel, "cell 0 1,grow");
				panel.setLayout(new MigLayout("", "[][][100px:n]50[][100px:n]", "[][]20[][][][]20[][]20[]"));
				{
					JPanel checkPanel = new JPanel();
					panel.add(checkPanel, "cell 0 0 1 2,grow");
					checkPanel.setLayout(new MigLayout("insets 0", "[]", "[][]"));
					{
						uiPot = new JCheckBox("PoT");
						uiPot.setToolTipText("Power of Two");
						checkPanel.add(uiPot, "cell 0 0");
					}
					{
						uiSquare = new JCheckBox("Square");
						checkPanel.add(uiSquare, "cell 0 1");
					}
				}
				{
					JLabel label = new JLabel("Max Width");
					panel.add(label, "cell 1 0,alignx right");
				}
				{
					uiMaxWidth = new JSpinner(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
					panel.add(uiMaxWidth, "cell 2 0,growx");
				}
				{
					JLabel label = new JLabel("Min Width");
					panel.add(label, "cell 3 0,alignx right");
				}
				{
					uiMinWidth = new JSpinner(new SpinnerNumberModel(new Integer(16), new Integer(0), null, new Integer(1)));
					panel.add(uiMinWidth, "cell 4 0,growx");
				}
				{
					JLabel label = new JLabel("Max Height");
					panel.add(label, "cell 1 1,alignx right");
				}
				{
					uiMaxHeight = new JSpinner(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
					panel.add(uiMaxHeight, "cell 2 1,growx");
				}
				{
					JLabel label = new JLabel("Min Height");
					panel.add(label, "cell 3 1,alignx right");
				}
				{
					uiMinHeight = new JSpinner(new SpinnerNumberModel(new Integer(16), new Integer(0), null, new Integer(1)));
					panel.add(uiMinHeight, "cell 4 1,growx");
				}
				
				{
					JPanel checkPanel = new JPanel();
					panel.add(checkPanel, "cell 0 2 1 4,grow");
					checkPanel.setLayout(new MigLayout("insets 0", "[]", "[][][][]"));
					{
						uiAlias = new JCheckBox("Use alias");
						checkPanel.add(uiAlias, "cell 0 0");
					}
					{
						uiRotation = new JCheckBox("Allow Rotations");
						checkPanel.add(uiRotation, "cell 0 1");
					}
					{
						uiIgnoreBlankImages = new JCheckBox("Ignore Blank Images");
						checkPanel.add(uiIgnoreBlankImages, "cell 0 2");
					}
					{
						uiCombineSubdirectories = new JCheckBox("Combine Subdirectories");
						checkPanel.add(uiCombineSubdirectories, "cell 0 3");
					}
				}
				{
					JLabel label = new JLabel("Format");
					panel.add(label, "cell 1 2,alignx right");
				}
				{
					uiFormat = new JComboBox<Pixmap.Format>(new DefaultComboBoxModel(Pixmap.Format.values()));
					panel.add(uiFormat, "cell 2 2,growx");
				}
				{
					JLabel label = new JLabel("Output Format");
					panel.add(label, "cell 1 3,alignx right");
				}
				{
					uiOutputFormat = new JComboBox<String>(new DefaultComboBoxModel(new String[]{"png", "jpg"}));
					panel.add(uiOutputFormat, "cell 2 3,growx");
				}
				{
					JLabel label = new JLabel("jpg Quality");
					panel.add(label, "cell 1 4,alignx right");
				}
				{
					JLabel label = new JLabel("Min Filter");
					panel.add(label, "cell 3 2,alignx right");
				}
				{
					uiFilterMin = new JComboBox<TextureFilter>(new DefaultComboBoxModel(TextureFilter.values()));
					panel.add(uiFilterMin, "cell 4 2,growx");
				}
				{
					uiJpegQuality = new JSpinner();
					uiJpegQuality.setModel(new SpinnerNumberModel(new Float(0.9), new Float(0), new Float(1), new Float(1)));
					panel.add(uiJpegQuality, "cell 2 4,growx");
				}
				{
					JLabel label = new JLabel("Wrap X");
					panel.add(label, "cell 3 4,alignx right");
				}
				{
					uiWrapX = new JComboBox<TextureWrap>(new DefaultComboBoxModel(TextureWrap.values()));
					panel.add(uiWrapX, "cell 4 4,growx");
				}
				{
					JLabel label = new JLabel("Mag Filter");
					panel.add(label, "cell 3 3,alignx right");
				}
				{
					uiFilterMag = new JComboBox<TextureFilter>(new DefaultComboBoxModel(TextureFilter.values()));
					panel.add(uiFilterMag, "cell 4 3,growx");
				}
				{
					JLabel label = new JLabel("Wrap Y");
					panel.add(label, "cell 3 5,alignx right");
				}
				{
					uiWrapY = new JComboBox<TextureWrap>(new DefaultComboBoxModel(TextureWrap.values()));
					panel.add(uiWrapY, "cell 4 5,growx");
				}
				
				{
					JPanel checkPanel = new JPanel();
					panel.add(checkPanel, "cell 0 6 1 2,grow");
					checkPanel.setLayout(new MigLayout("insets 0", "[]", "[][]"));
					{
						uiEdgePadding = new JCheckBox("Padding Edge");
						checkPanel.add(uiEdgePadding, "cell 0 0");
					}
					{
						uiDuplicatePadding = new JCheckBox("Duplicate Padding");
						checkPanel.add(uiDuplicatePadding, "cell 0 1");
					}
				}
				{
					JLabel label = new JLabel("Padding X");
					panel.add(label, "cell 1 6,alignx right");
				}
				{
					uiPaddingX = new JSpinner(new SpinnerNumberModel(new Integer(2), new Integer(0), null, new Integer(1)));
					panel.add(uiPaddingX, "cell 2 6,growx");
				}
				{
					JLabel label = new JLabel("Padding Y");
					panel.add(label, "cell 1 7,alignx right");
				}
				{
					uiPaddingY = new JSpinner(new SpinnerNumberModel(new Integer(2), new Integer(0), null, new Integer(1)));
					panel.add(uiPaddingY, "cell 2 7,growx");
				}
				
				{
					JPanel checkPanel = new JPanel();
					panel.add(checkPanel, "cell 0 8,grow");
					checkPanel.setLayout(new MigLayout("insets 0", "[]", "[][]"));
					{
						uiStripWhitespaceX = new JCheckBox("Strip Whitespace X");
						checkPanel.add(uiStripWhitespaceX, "cell 0 0");
					}
					{
						uiStripWhitespaceY = new JCheckBox("Strip Whitespace Y");
						checkPanel.add(uiStripWhitespaceY, "cell 0 1");
					}
				}
				{
					JLabel label = new JLabel("Alpha Threshold");
					panel.add(label, "cell 1 8,alignx right");
				}
				{
					uiAlphaThreshold = new JSpinner();
					uiAlphaThreshold.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
					panel.add(uiAlphaThreshold, "cell 2 8,growx");
				}
				
				{
					JPanel checkPanel = new JPanel();
					checkPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
					panel.add(checkPanel, "cell 3 6 2 3,grow");
					checkPanel.setLayout(new MigLayout("insets 0", "[]", "[][]"));
					{
						uiFast = new JCheckBox("Fast Pack");
						uiFast.setToolTipText("Use fast algorithm");
						checkPanel.add(uiFast, "cell 0 0");
					}
					{
						uiDebug = new JCheckBox("Debug");
						uiDebug.setToolTipText("Add a rect around image");
						checkPanel.add(uiDebug, "cell 0 1");
					}
				}
			}
			
		}
		{
			uiFileChooser = new JFileChooser();
			uiFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private void initListeners() {
		uiSquare.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					uiMaxHeight.setEnabled(false);
					int width = new BigDecimal(uiMaxWidth.getValue().toString()).intValue();
					int height = new BigDecimal(uiMaxHeight.getValue().toString()).intValue();
					int max = Math.max(width, height);
					uiMaxWidth.setValue(max);
					uiMaxHeight.setValue(max);
				} else {
					uiMaxHeight.setEnabled(true);
				}
			}
		});
		uiMaxWidth.addChangeListener(new ChangeListener() {
			
			
			@Override
			public void stateChanged(ChangeEvent evt) {
				if (uiPot.isSelected()) {
					int previewValue = new BigDecimal(uiMaxWidth.getPreviousValue().toString()).intValue();
					int value = new BigDecimal(uiMaxWidth.getValue().toString()).intValue();
					if (previewValue > value) {
						value = value / 2;
					}
					if (value != MathUtils.nextPowerOfTwo(value)) {
						uiMaxWidth.setValue(MathUtils.nextPowerOfTwo(value));
					}
				}
				if (uiSquare.isSelected()) {
					uiMaxHeight.setValue(uiMaxWidth.getValue());
				}
			}
		});
		uiMaxHeight.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent evt) {
				if (uiPot.isSelected()) {
					int previewValue = new BigDecimal(uiMaxHeight.getPreviousValue().toString()).intValue();
					int value = new BigDecimal(uiMaxHeight.getValue().toString()).intValue();
					if (previewValue > value) {
						value = value / 2;
					}
					if (value != MathUtils.nextPowerOfTwo(value)) {
						uiMaxHeight.setValue(MathUtils.nextPowerOfTwo(value));
					}
				}
			}
		});
		uiOutputFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					if ("png".equals(uiOutputFormat.getSelectedItem().toString())) {
						uiJpegQuality.setEnabled(false);
					} else {
						uiJpegQuality.setEnabled(true);
					}
				}
			}
		});
	}
	
	private Settings uiToSettings() {
		Settings settings = new Settings();
		settings.pot = uiPot.isSelected();
		settings.square = uiSquare.isSelected();
		settings.maxWidth = new BigDecimal(uiMaxWidth.getValue().toString()).intValue();
		settings.maxHeight = new BigDecimal(uiMaxHeight.getValue().toString()).intValue();
		settings.minWidth = new BigDecimal(uiMinWidth.getValue().toString()).intValue();
		settings.minHeight = new BigDecimal(uiMinHeight.getValue().toString()).intValue();
		settings.alias = uiAlias.isSelected();
		settings.rotation = uiRotation.isSelected();
		settings.ignoreBlankImages = uiIgnoreBlankImages.isSelected();
		settings.combineSubdirectories = uiCombineSubdirectories.isSelected();
		settings.format = (Format)uiFormat.getSelectedItem();
		settings.outputFormat = (String)uiOutputFormat.getSelectedItem();
		settings.jpegQuality = new BigDecimal(uiJpegQuality.getValue().toString()).floatValue();
		settings.filterMin = (TextureFilter)uiFilterMin.getSelectedItem();
		settings.filterMag = (TextureFilter)uiFilterMag.getSelectedItem();
		settings.wrapX = (TextureWrap)uiWrapX.getSelectedItem();
		settings.wrapY = (TextureWrap)uiWrapY.getSelectedItem();
		settings.edgePadding = uiEdgePadding.isSelected();
		settings.duplicatePadding = uiDuplicatePadding.isSelected();
		settings.paddingX = new BigDecimal(uiPaddingX.getValue().toString()).intValue();
		settings.paddingY = new BigDecimal(uiPaddingY.getValue().toString()).intValue();
		settings.stripWhitespaceX = uiStripWhitespaceX.isSelected();
		settings.stripWhitespaceY = uiStripWhitespaceY.isSelected();
		settings.alphaThreshold = new BigDecimal(uiAlphaThreshold.getValue().toString()).intValue();
		settings.fast = uiFast.isSelected();
		settings.debug = uiDebug.isSelected();

		return settings;
	}
	
	private void settingsToUi(Settings settings) {
		if (settings == null) {
			settings = new Settings();
		}
		uiPot.setSelected(settings.pot);
		uiSquare.setSelected(settings.square);
		uiMaxWidth.setValue(settings.maxWidth);
		uiMaxHeight.setValue(settings.maxHeight);
		uiMinWidth.setValue(settings.minWidth);
		uiMinHeight.setValue(settings.minHeight);
		uiAlias.setSelected(settings.alias);
		uiRotation.setSelected(settings.rotation);
		uiIgnoreBlankImages.setSelected(settings.ignoreBlankImages);
		uiCombineSubdirectories.setSelected(settings.combineSubdirectories);
		uiFormat.setSelectedItem(settings.format);
		uiOutputFormat.setSelectedItem(settings.outputFormat);
		uiJpegQuality.setValue(settings.jpegQuality);
		uiFilterMin.setSelectedItem(settings.filterMin);
		uiFilterMag.setSelectedItem(settings.filterMag);
		uiWrapX.setSelectedItem(settings.wrapX);
		uiWrapY.setSelectedItem(settings.wrapY);
		uiEdgePadding.setSelected(settings.edgePadding);
		uiDuplicatePadding.setSelected(settings.duplicatePadding);
		uiPaddingX.setValue(settings.paddingX);
		uiPaddingY.setValue(settings.paddingY);
		uiStripWhitespaceX.setSelected(settings.stripWhitespaceX);
		uiStripWhitespaceY.setSelected(settings.stripWhitespaceY);
		uiAlphaThreshold.setValue(settings.alphaThreshold);
		uiFast.setSelected(settings.fast);
		uiDebug.setSelected(settings.debug);
	}
	
	protected void uiSelectInputActionPerformed(ActionEvent evt) {
		uiFileChooser.setDialogTitle("Choose Output");
		int op = uiFileChooser.showOpenDialog(this);
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiFileChooser.getSelectedFile();
			uiInput.setText(file.getAbsolutePath());
		}
	}
	
	protected void uiSelectOutputActionPerformed(ActionEvent evt) {
		uiFileChooser.setDialogTitle("Choose Iutput");
		int op = uiFileChooser.showOpenDialog(this);
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiFileChooser.getSelectedFile();
			uiOutput.setText(file.getAbsolutePath());
		}
	}
	
	protected void uiPackActionPerformed(ActionEvent evt) {
		if (uiPackName.getText().length() == 0) {
			showErrorMsg("Pack name is empty!");
			return;
		}
		if (uiInput.getText().length() == 0) {
			showErrorMsg("Input directory is empty!");
			return;
		}
		if (uiOutput.getText().length() == 0) {
			uiOutput.setText(uiInput.getText() + File.separator + "pack");
		}
		TexturePacker.process(uiToSettings(), uiInput.getText(), uiOutput.getText(), uiPackName.getText());
	}

	public void showErrorMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, TITLE, JOptionPane.ERROR_MESSAGE);
	}
	
	public void showErrorMsg(Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), TITLE, JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfoMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, TITLE, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public class PotSpinnerNumberModel extends SpinnerNumberModel {
		
	}
}
