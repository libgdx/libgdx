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
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

import net.miginfocom.swing.MigLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import javax.swing.JSplitPane;

public class TexturePackerUI extends JFrame {
	private static final String TITLE ="Gdx Texture Packer";
	
	private Pack previrePack = null;
	
	private JCheckBox uiPot;
	private PotSpinner uiMinWidth;
	private PotSpinner uiMinHeight;
	private PotSpinner uiPaddingX;
	private PotSpinner uiPaddingY;
	private JCheckBox uiDuplicatePadding;
	private JCheckBox uiSquare;
	private JCheckBox uiStripWhitespaceX;
	private JCheckBox uiStripWhitespaceY;
	private PotSpinner uiAlphaThreshold;
	private JComboBox<TextureFilter> uiFilterMin;
	private JComboBox<TextureFilter> uiFilterMag;
	private JComboBox<TextureWrap> uiWrapX;
	private JComboBox<TextureWrap> uiWrapY;
	private JComboBox<Pixmap.Format> uiFormat;
	private JComboBox<String> uiOutputFormat;
	private PotSpinner uiJpegQuality;
	private JCheckBox uiAlias;
	private JCheckBox uiIgnoreBlankImages;
	private JCheckBox uiRotation;
	private JCheckBox uiCombineSubdirectories;
	private JCheckBox uiFast;
	private JCheckBox uiDebug;
	private PotSpinner uiMaxWidth;
	private PotSpinner uiMaxHeight;
	private JCheckBox uiEdgePadding;
	private JTextField uiInput;
	private JTextField uiOutput;
	private JTextField uiAtlasExtension;
	private JCheckBox uiFlattenPaths;
	private JCheckBox uiPremultiplyAlpha;
	private JCheckBox uiUseIndexes;
	private JCheckBox uiBleed;
	private JCheckBox uiLimitMemory;
	private JCheckBox uiGrid;
	
	private DefaultListModel<Pack> uiPackModel;
	private JList<Pack> uiPack;
	private ScaleTable uiScale;
	private JButton uiSelectInput;
	private JButton uiSelectOutput;
	private JButton uiPackSelected;
	private JButton uiPackAll;
	private JButton uiNew;
	private JButton uiDelete;
	private JButton uiRename;
	private JButton uiOpen;
	private JButton uiSave;
	private LogOut logOut;
	private JTextArea uiLog;
	
	private JFileChooser uiFolderChooser;
	private JFileChooser uiPackChooser;
	private JButton uiAddScale;
	private JButton uiDeleteScale;

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		setIconImage(Toolkit.getDefaultToolkit().getImage(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/pack.png")));
		initComponents();
		initListeners();
		updateUiState();
	}
	
	private void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 500);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setTitle(TITLE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			getContentPane().add(toolBar, BorderLayout.NORTH);
			{
				uiNew = new JButton("New Pack");
				uiNew.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uiNewActionPerformed(e);
					}
				});
				uiNew.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/add.png")));
				uiNew.setFocusable(false);
				toolBar.add(uiNew);
			}
			toolBar.addSeparator();
			{
				uiDelete = new JButton("Delete Selected");
				uiDelete.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uiDeleteActionPerformed(e);
					}
				});
				uiDelete.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/delete.png")));
				uiDelete.setFocusable(false);
				toolBar.add(uiDelete);
			}
			toolBar.addSeparator();
			{
				uiRename = new JButton("Rename Selected");
				uiRename.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uiRenameActionPerformed(e);
					}
				});
				uiRename.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/rename.png")));
				uiRename.setFocusable(false);
				toolBar.add(uiRename);
			}
			toolBar.addSeparator();
			{
				uiOpen = new JButton("Open");
				uiOpen.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uiOpenActionPerformed(e);
					}
				});
				uiOpen.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/open.png")));
				uiOpen.setFocusable(false);
				toolBar.add(uiOpen);
			}
			toolBar.addSeparator();
			{
				uiSave = new JButton("Save Selected");
				uiSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						uiSaveActionPerformed(e);
					}
				});
				uiSave.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/save.png")));
				uiSave.setFocusable(false);
				toolBar.add(uiSave);
			}
		}
		{
			JSplitPane centerPanel = new JSplitPane();
			getContentPane().add(centerPanel, BorderLayout.CENTER);
			{
				JPanel panel = new JPanel();
				panel.setBorder(new LineBorder(new Color(0, 0, 0)));
				centerPanel.setLeftComponent(panel);
				panel.setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));
				{
					JPanel buttonPane = new JPanel();
					buttonPane.setLayout(new MigLayout("", "[grow]", "[][]"));
					panel.add(buttonPane, "cell 0 0,grow");
					{
						uiPackSelected = new JButton("Pack Selected");
						uiPackSelected.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/pack.png")));
						buttonPane.add(uiPackSelected, "cell 0 0,growx");
						uiPackSelected.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								uiPackSelectedActionPerformed(e);
							}
						});
					}
					{
						uiPackAll = new JButton("Pack All");
						uiPackAll.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/pack.png")));
						buttonPane.add(uiPackAll, "cell 0 1,growx");
						uiPackAll.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								uiPackAllActionPerformed(e);
							}
						});
					}
					{
						JSplitPane splitPane = new JSplitPane();
						splitPane.setResizeWeight(0.5);
						splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
						panel.add(splitPane, "cell 0 1,grow");
						{
							JScrollPane scrollPane = new JScrollPane();
							splitPane.setLeftComponent(scrollPane);
							uiPackModel = new DefaultListModel<Pack>();
							uiPack = new JList<Pack>(uiPackModel);
							uiPack.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							uiPack.addListSelectionListener(new ListSelectionListener() {
								public void valueChanged(ListSelectionEvent e) {
									uiPackValueChanged(e);
								}
							});
							uiPack.setValueIsAdjusting(true);
							scrollPane.setViewportView(uiPack);
						}
						{
							JPanel scalePanel = new JPanel();
							scalePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Scale", TitledBorder.LEADING, TitledBorder.TOP, null, null));
							splitPane.setRightComponent(scalePanel);
							scalePanel.setLayout(new BorderLayout(0, 0));
							{
								JToolBar toolBar = new JToolBar();
								toolBar.setFloatable(false);
								scalePanel.add(toolBar, BorderLayout.NORTH);
								{
									uiAddScale = new JButton("Add Scale");
									uiAddScale.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											uiAddScaleActionPerformed(e);
										}
									});
									uiAddScale.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/add.png")));
									toolBar.add(uiAddScale);
								}
								toolBar.addSeparator();
								{
									uiDeleteScale = new JButton("Delete Scale");
									uiDeleteScale.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											uiDeleteScaleActionPerformed(e);
										}
									});
									uiDeleteScale.setIcon(new ImageIcon(TexturePackerUI.class.getResource("/com/badlogic/gdx/tools/texturepacker/icon/delete.png")));
									toolBar.add(uiDeleteScale);
								}
							}
							{
								JScrollPane scrollPane = new JScrollPane();
								scalePanel.add(scrollPane, BorderLayout.CENTER);
								uiScale = new ScaleTable();
								scrollPane.setViewportView(uiScale);
							}
						}
					}
				}
			}
			{
				JPanel rightPanel = new JPanel();
				rightPanel.setLayout(new MigLayout("insets 0", "[grow]", "[][][grow]"));
				centerPanel.setRightComponent(rightPanel);
				{
					JPanel panel = new JPanel();
					panel.setBorder(new LineBorder(new Color(0, 0, 0)));
					rightPanel.add(panel, "cell 0 0,grow");
					panel.setLayout(new MigLayout("", "[][grow][]", "[][]"));
					{
						JLabel label = new JLabel("Input Directory");
						panel.add(label, "cell 0 0");
					}
					{
						uiInput = new JTextField();
						panel.add(uiInput, "cell 1 0,growx");
					}
					{
						uiSelectInput = new JButton("...");
						uiSelectInput.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								uiSelectInputActionPerformed(e);
							}
						});
						panel.add(uiSelectInput, "cell 2 0,grow");
					}
					{
						JLabel label = new JLabel("Output Directory");
						panel.add(label, "cell 0 1");
					}
					{
						uiOutput = new JTextField();
						panel.add(uiOutput, "cell 1 1,growx");
					}
					{
						uiSelectOutput = new JButton("...");
						uiSelectOutput.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								uiSelectOutputActionPerformed(e);
							}
						});
						panel.add(uiSelectOutput, "cell 2 1,grow");
					}
				}
				{
					JScrollPane scrollPane = new JScrollPane();
					rightPanel.add(scrollPane, "cell 0 1,grow");
					{
						JPanel panel = new JPanel();
						scrollPane.setViewportView(panel);
						panel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
						panel.setLayout(new MigLayout("", "[]50[][grow]50[][grow]", "[][]20[][][][]20[][]20[]"));
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
							uiMaxWidth = new PotSpinner(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
							panel.add(uiMaxWidth, "cell 2 0,growx");
						}
						{
							JLabel label = new JLabel("Min Width");
							panel.add(label, "cell 3 0,alignx right");
						}
						{
							uiMinWidth = new PotSpinner(new SpinnerNumberModel(new Integer(16), new Integer(0), null, new Integer(1)));
							panel.add(uiMinWidth, "cell 4 0,growx");
						}
						{
							JLabel label = new JLabel("Max Height");
							panel.add(label, "cell 1 1,alignx right");
						}
						{
							uiMaxHeight = new PotSpinner(new SpinnerNumberModel(new Integer(1024), new Integer(0), null, new Integer(1)));
							panel.add(uiMaxHeight, "cell 2 1,growx");
						}
						{
							JLabel label = new JLabel("Min Height");
							panel.add(label, "cell 3 1,alignx right");
						}
						{
							uiMinHeight = new PotSpinner(new SpinnerNumberModel(new Integer(16), new Integer(0), null, new Integer(1)));
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
							uiJpegQuality = new PotSpinner();
							uiJpegQuality.setEnabled(false);
							uiJpegQuality.setModel(new SpinnerNumberModel(new Float(0.9), new Float(0), new Float(1), new Float(1)));
							panel.add(uiJpegQuality, "cell 2 4,growx");
						}
						
						{
							JLabel label = new JLabel("Atlas Extension");
							panel.add(label, "cell 1 5,alignx right");
						}
						{
							uiAtlasExtension = new JTextField();
							panel.add(uiAtlasExtension, "cell 2 5,growx");
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
							uiPaddingX = new PotSpinner(new SpinnerNumberModel(new Integer(2), new Integer(0), null, new Integer(1)));
							panel.add(uiPaddingX, "cell 2 6,growx");
						}
						{
							JLabel label = new JLabel("Padding Y");
							panel.add(label, "cell 1 7,alignx right");
						}
						{
							uiPaddingY = new PotSpinner(new SpinnerNumberModel(new Integer(2), new Integer(0), null, new Integer(1)));
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
							uiAlphaThreshold = new PotSpinner();
							uiAlphaThreshold.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
							panel.add(uiAlphaThreshold, "cell 2 8,growx");
						}
						
						{
							JPanel checkPanel = new JPanel();
							checkPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
							panel.add(checkPanel, "cell 3 6 2 3,grow");
							checkPanel.setLayout(new MigLayout("insets 0", "[][]", "[][][][]"));
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
							{
								uiFlattenPaths = new JCheckBox("Flatten Paths");
								checkPanel.add(uiFlattenPaths, "cell 0 2");
							}
							{
								uiPremultiplyAlpha = new JCheckBox("Premultiply Alpha");
								checkPanel.add(uiPremultiplyAlpha, "cell 0 3");
							}
							{
								uiUseIndexes = new JCheckBox("Use Indexes");
								checkPanel.add(uiUseIndexes, "cell 1 0");
							}
							{
								uiBleed = new JCheckBox("Bleed");
								checkPanel.add(uiBleed, "cell 1 1");
							}
							{
								uiLimitMemory = new JCheckBox("LimitMemory");
								checkPanel.add(uiLimitMemory, "cell 1 2");
							}
							{
								uiGrid = new JCheckBox("Grid");
								checkPanel.add(uiGrid, "cell 1 3");
							}
						}
					}
				}
				{
					JScrollPane scrollPane = new JScrollPane();
					rightPanel.add(scrollPane, "cell 0 2,grow");
					{
						uiLog = new JTextArea();
						scrollPane.setViewportView(uiLog);
						logOut = new LogOut(System.out, uiLog);
						System.setOut(logOut);
						System.setErr(logOut);
					}
				}
			}
			
		}
		{
			uiFolderChooser = new JFileChooser();
			uiFolderChooser.setMultiSelectionEnabled(false);
			uiFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		{
			uiPackChooser = new JFileChooser();
			uiPackChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			uiPackChooser.setMultiSelectionEnabled(false);
			uiPackChooser.setFileFilter(new FileFilter() {
				
				@Override
				public String getDescription () {
					return "gdx texture packer";
				}
				
				@Override
				public boolean accept (File f) {
					if (f.isDirectory()) {
						return true;
					} else {
						if (f.getAbsolutePath().toLowerCase().endsWith(".gtp")) {
							return true;
						}
					}
					return false;
				}
			});
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private void initListeners() {
		uiPot.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					int value = new BigDecimal(uiMaxWidth.getValue().toString()).intValue();
					if (value != MathUtils.nextPowerOfTwo(value)) {
						uiMaxWidth.setValue(MathUtils.nextPowerOfTwo(value));
					}
					if (!uiSquare.isSelected()) {
						int valueHeight = new BigDecimal(uiMaxHeight.getValue().toString()).intValue();
						if (valueHeight != MathUtils.nextPowerOfTwo(valueHeight)) {
							uiMaxHeight.setValue(MathUtils.nextPowerOfTwo(valueHeight));
						}
					}
				}
			}
		});
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
					int previewValue = new BigDecimal(uiMaxWidth.getValueBefore().toString()).intValue();
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
					int previewValue = new BigDecimal(uiMaxHeight.getValueBefore().toString()).intValue();
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
	
	private void updateUiState() {
		uiDelete.setEnabled(uiPack.getSelectedIndex() > -1);
		uiRename.setEnabled(uiPack.getSelectedIndex() > -1);
		uiSave.setEnabled(uiPack.getSelectedIndex() > -1);
		
		uiPackSelected.setEnabled(uiPack.getSelectedIndex() > -1);
		uiPackAll.setEnabled(uiPackModel.getSize() > 0);
		
		uiInput.setEnabled(uiPack.getSelectedIndex() > -1);
		uiSelectInput.setEnabled(uiPack.getSelectedIndex() > -1);
		uiOutput.setEnabled(uiPack.getSelectedIndex() > -1);
		uiSelectOutput.setEnabled(uiPack.getSelectedIndex() > -1);
		
		uiPot.setEnabled(uiPack.getSelectedIndex() > -1);
		uiSquare.setEnabled(uiPack.getSelectedIndex() > -1);
		uiMaxWidth.setEnabled(uiPack.getSelectedIndex() > -1);
		if (uiPack.getSelectedIndex() > -1 && !uiSquare.isSelected()) {
			uiMaxHeight.setEnabled(true);
		} else {
			uiMaxHeight.setEnabled(false);
		}
		uiMinWidth.setEnabled(uiPack.getSelectedIndex() > -1);
		uiMinHeight.setEnabled(uiPack.getSelectedIndex() > -1);
		uiAlias.setEnabled(uiPack.getSelectedIndex() > -1);
		uiRotation.setEnabled(uiPack.getSelectedIndex() > -1);
		uiIgnoreBlankImages.setEnabled(uiPack.getSelectedIndex() > -1);
		uiCombineSubdirectories.setEnabled(uiPack.getSelectedIndex() > -1);
		uiFormat.setEnabled(uiPack.getSelectedIndex() > -1);
		uiOutputFormat.setEnabled(uiPack.getSelectedIndex() > -1);
		if (uiPack.getSelectedIndex() > -1 && "jpg".equals(uiOutputFormat.getSelectedItem().toString())) {
			uiJpegQuality.setEnabled(true);
		} else {
			uiJpegQuality.setEnabled(false);
		}
		uiFilterMin.setEnabled(uiPack.getSelectedIndex() > -1);
		uiFilterMag.setEnabled(uiPack.getSelectedIndex() > -1);
		uiWrapX.setEnabled(uiPack.getSelectedIndex() > -1);
		uiWrapY.setEnabled(uiPack.getSelectedIndex() > -1);
		uiEdgePadding.setEnabled(uiPack.getSelectedIndex() > -1);
		uiDuplicatePadding.setEnabled(uiPack.getSelectedIndex() > -1);
		uiPaddingX.setEnabled(uiPack.getSelectedIndex() > -1);
		uiPaddingY.setEnabled(uiPack.getSelectedIndex() > -1);
		uiStripWhitespaceX.setEnabled(uiPack.getSelectedIndex() > -1);
		uiStripWhitespaceY.setEnabled(uiPack.getSelectedIndex() > -1);
		uiAlphaThreshold.setEnabled(uiPack.getSelectedIndex() > -1);
		uiFast.setEnabled(uiPack.getSelectedIndex() > -1);
		uiDebug.setEnabled(uiPack.getSelectedIndex() > -1);
		uiAtlasExtension.setEnabled(uiPack.getSelectedIndex() > -1);
		uiFlattenPaths.setEnabled(uiPack.getSelectedIndex() > -1);
		uiPremultiplyAlpha.setEnabled(uiPack.getSelectedIndex() > -1);
		uiUseIndexes.setEnabled(uiPack.getSelectedIndex() > -1);
		uiBleed.setEnabled(uiPack.getSelectedIndex() > -1);
		uiLimitMemory.setEnabled(uiPack.getSelectedIndex() > -1);
		uiGrid.setEnabled(uiPack.getSelectedIndex() > -1);
		uiAddScale.setEnabled(uiPack.getSelectedIndex() > -1);
		uiDeleteScale.setEnabled(uiPack.getSelectedIndex() > -1);
		if (uiPack.getSelectedIndex() == -1) {
			uiScale.clear();
		}
	}
	
	private Settings UpdatePack(Pack pack) {
		pack.setInput(uiInput.getText());
		pack.setOutput(uiOutput.getText());
		
		Settings settings = pack.getSettings();
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
		settings.atlasExtension = uiAtlasExtension.getText();
		settings.flattenPaths = uiFlattenPaths.isSelected();
		settings.premultiplyAlpha = uiPremultiplyAlpha.isSelected();
		settings.useIndexes = uiUseIndexes.isSelected();
		settings.bleed = uiBleed.isSelected();
		settings.limitMemory = uiLimitMemory.isSelected();
		settings.grid = uiGrid.isSelected();
		settings.scale = uiScale.getScale();
		settings.scaleSuffix = uiScale.getScaleSuffix();
		return settings;
	}
	
	private void packToUi(Pack pack) {
		uiInput.setText(pack.getInput());
		uiOutput.setText(pack.getOutput());
		
		Settings settings = pack.getSettings();
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
		uiAtlasExtension.setText(settings.atlasExtension);
		uiFlattenPaths.setSelected(settings.flattenPaths);
		uiPremultiplyAlpha.setSelected(settings.premultiplyAlpha);
		uiUseIndexes.setSelected(settings.useIndexes);
		uiBleed.setSelected(settings.bleed);
		uiLimitMemory.setSelected(settings.limitMemory);
		uiGrid.setSelected(settings.grid);
		uiScale.setDatas(settings.scale, settings.scaleSuffix);
	}
	
	private Pack packFromFile(File file) {
		Pack pack = new Pack();
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			prop.load(is);
			pack.setPackName(prop.getProperty("packName", ""));
			pack.setOutput(prop.getProperty("output", ""));
			pack.setInput(prop.getProperty("input", ""));
			
			Settings settings = new Settings();
			pack.setSettings(settings);
			settings.pot = Boolean.parseBoolean(prop.getProperty("pot", "true"));
			settings.square = Boolean.parseBoolean(prop.getProperty("square", "false"));
			settings.maxWidth = Integer.parseInt(prop.getProperty("maxWidth", "1024"));
			settings.maxHeight = Integer.parseInt(prop.getProperty("maxHeight", "1024"));
			settings.minWidth = Integer.parseInt(prop.getProperty("minWidth", "16"));
			settings.minHeight = Integer.parseInt(prop.getProperty("minHeight", "16"));
			settings.alias = Boolean.parseBoolean(prop.getProperty("alias", "true"));
			settings.rotation = Boolean.parseBoolean(prop.getProperty("rotation", "false"));
			settings.ignoreBlankImages = Boolean.parseBoolean(prop.getProperty("ignoreBlankImages", "true"));
			settings.combineSubdirectories = Boolean.parseBoolean(prop.getProperty("combineSubdirectories", "false"));
			settings.format = Pixmap.Format.valueOf(prop.getProperty("format", "RGBA8888"));
			settings.outputFormat = prop.getProperty("outputFormat", "png");
			settings.jpegQuality = Float.parseFloat(prop.getProperty("jpegQuality", "0.9"));
			settings.filterMin = TextureFilter.valueOf(prop.getProperty("filterMin", "Nearest"));
			settings.filterMag = TextureFilter.valueOf(prop.getProperty("filterMag", "Nearest"));
			settings.wrapX = TextureWrap.valueOf(prop.getProperty("wrapX", "ClampToEdge"));
			settings.wrapY = TextureWrap.valueOf(prop.getProperty("wrapY", "ClampToEdge"));
			settings.edgePadding = Boolean.parseBoolean(prop.getProperty("edgePadding", "true"));
			settings.duplicatePadding = Boolean.parseBoolean(prop.getProperty("duplicatePadding", "false"));
			settings.paddingX = Integer.parseInt(prop.getProperty("paddingX", "2"));
			settings.paddingY = Integer.parseInt(prop.getProperty("paddingY", "2"));
			settings.stripWhitespaceX = Boolean.parseBoolean(prop.getProperty("stripWhitespaceX", ""));
			settings.stripWhitespaceY = Boolean.parseBoolean(prop.getProperty("stripWhitespaceY", ""));
			settings.alphaThreshold = Integer.parseInt(prop.getProperty("alphaThreshold", "0"));
			settings.fast = Boolean.parseBoolean(prop.getProperty("fast", "false"));
			settings.debug = Boolean.parseBoolean(prop.getProperty("debug", "false"));
			settings.atlasExtension = prop.getProperty("atlasExtension", ".atlas");
			settings.flattenPaths = Boolean.parseBoolean(prop.getProperty("flattenPaths", "false"));
			settings.premultiplyAlpha = Boolean.parseBoolean(prop.getProperty("premultiplyAlpha", "false"));
			settings.useIndexes = Boolean.parseBoolean(prop.getProperty("useIndexes", "true"));
			settings.bleed = Boolean.parseBoolean(prop.getProperty("bleed", "false"));
			settings.limitMemory = Boolean.parseBoolean(prop.getProperty("limitMemory", "true"));
			settings.grid = Boolean.parseBoolean(prop.getProperty("grid", "false"));
			settings.scale = stringToFloatArray(prop.getProperty("scale", "1"));
			settings.scaleSuffix = stringToStringArray(prop.getProperty("scaleSuffix", ""));
			is.close();
		} catch (Exception e) {
			showErrorMsg(e);
		}
		return pack;
	}
	
	private void packToFile(File file, Pack pack) {
		Properties prop = new Properties();
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(file);
			prop.load(is);
			is.close();
			
			os = new FileOutputStream(file);
			
			prop.setProperty("packName", pack.getPackName());
			prop.setProperty("output", pack.getOutput());
			prop.setProperty("input", pack.getInput());
			
			Settings settings = pack.getSettings();
			prop.setProperty("pot", Boolean.toString(settings.pot));
			prop.setProperty("square", Boolean.toString(settings.square));
			prop.setProperty("maxWidth", Integer.toString(settings.maxWidth));
			prop.setProperty("maxHeight", Integer.toString(settings.maxHeight));
			prop.setProperty("minWidth", Integer.toString(settings.minWidth));
			prop.setProperty("minHeight", Integer.toString(settings.minHeight));
			prop.setProperty("alias", Boolean.toString(settings.alias));
			prop.setProperty("rotation", Boolean.toString(settings.rotation));
			prop.setProperty("ignoreBlankImages", Boolean.toString(settings.ignoreBlankImages));
			prop.setProperty("combineSubdirectories", Boolean.toString(settings.combineSubdirectories));
			prop.setProperty("format", settings.format.toString());
			prop.setProperty("outputFormat", settings.outputFormat);
			prop.setProperty("jpegQuality", Float.toString(settings.jpegQuality));
			prop.setProperty("filterMin", settings.filterMin.toString());
			prop.setProperty("filterMag", settings.filterMag.toString());
			prop.setProperty("wrapX", settings.wrapX.toString());
			prop.setProperty("wrapY", settings.wrapY.toString());
			prop.setProperty("edgePadding", Boolean.toString(settings.edgePadding));
			prop.setProperty("duplicatePadding", Boolean.toString(settings.duplicatePadding));
			prop.setProperty("paddingX", Integer.toString(settings.paddingX));
			prop.setProperty("paddingY", Integer.toString(settings.paddingY));
			prop.setProperty("stripWhitespaceX", Boolean.toString(settings.stripWhitespaceX));
			prop.setProperty("stripWhitespaceY", Boolean.toString(settings.stripWhitespaceY));
			prop.setProperty("alphaThreshold", Integer.toString(settings.alphaThreshold));
			prop.setProperty("fast", Boolean.toString(settings.fast));
			prop.setProperty("debug", Boolean.toString(settings.debug));
			prop.setProperty("atlasExtension", settings.atlasExtension);
			prop.setProperty("flattenPaths", Boolean.toString(settings.flattenPaths));
			prop.setProperty("premultiplyAlpha", Boolean.toString(settings.premultiplyAlpha));
			prop.setProperty("useIndexes", Boolean.toString(settings.useIndexes));
			prop.setProperty("bleed", Boolean.toString(settings.bleed));
			prop.setProperty("limitMemory", Boolean.toString(settings.limitMemory));
			prop.setProperty("grid", Boolean.toString(settings.grid));
			prop.setProperty("scale", floatArrayToString(settings.scale));
			prop.setProperty("scaleSuffix", stringArrayToString(settings.scaleSuffix));
			
			prop.store(os, null);
			
			os.close();
			is.close();
		} catch (Exception e) {
			showErrorMsg(e);
		}
	}
	
	protected void uiRenameActionPerformed(ActionEvent evt) {
		Pack pack = uiPack.getSelectedValue();
		if (pack == null) {
			showErrorMsg("No Pack selected!");
			return;
		}
		String packName = showInputDialog(pack.getPackName());
		if (packName != null && packName.length() > 0) {
			pack.setPackName(packName);
		}
	}
	
	protected void uiNewActionPerformed(ActionEvent evt) {
		String packName = showInputDialog(null);
		if (packName != null && packName.length() != 0) {
			Pack pack = new Pack(packName);
			uiPackModel.addElement(pack);
			uiPack.setSelectedIndex(uiPackModel.getSize() - 1);
			updateUiState();
			packToUi(pack);
		}
	}
	
	protected void uiOpenActionPerformed(ActionEvent evt) {
		int op = uiPackChooser.showOpenDialog(this);	
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiPackChooser.getSelectedFile();
			Pack pack = packFromFile(file);
			
			uiPackModel.addElement(pack);
			uiPack.setSelectedIndex(uiPackModel.size() - 1);
			updateUiState();
			packToUi(pack);
		}
	}
	
	protected void uiSaveActionPerformed(ActionEvent evt) {
		Pack pack = uiPack.getSelectedValue();
		if (pack == null) {
			showErrorMsg("No Pack selected!");
			return;
		}
		UpdatePack(pack);
		
		int op = uiPackChooser.showSaveDialog(this);
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiPackChooser.getSelectedFile();
			if (!file.getAbsolutePath().toLowerCase().endsWith(".gtp")) {
				file = new File(file.getAbsolutePath() + ".gtp");
			}
			try {
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				packToFile(file, pack);
			} catch (Exception e) {
				showErrorMsg(e);
			}
			
		}
	}
	
	protected void uiDeleteActionPerformed(ActionEvent evt) {
		Pack pack = uiPack.getSelectedValue();
		if (pack == null) {
			showErrorMsg("No Pack selected!");
			return;
		}
		uiPackModel.removeElement(pack);
		if (!uiPackModel.isEmpty()) {
			uiPack.setSelectedIndex(0);
			packToUi(uiPack.getSelectedValue());
		}
		updateUiState();
	}
	
	protected void uiPackValueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting() && uiPack.getSelectedIndex() != -1) {
			if (previrePack != null) {
				UpdatePack(previrePack);
			}
			updateUiState();
			Pack pack = uiPack.getSelectedValue();
			packToUi(pack);
			previrePack = pack;
		}
	}
	
	protected void uiSelectInputActionPerformed(ActionEvent evt) {
		uiFolderChooser.setDialogTitle("Choose Output");
		int op = uiFolderChooser.showOpenDialog(this);
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiFolderChooser.getSelectedFile();
			uiInput.setText(file.getAbsolutePath());
		}
	}
	
	protected void uiSelectOutputActionPerformed(ActionEvent evt) {
		uiFolderChooser.setDialogTitle("Choose Iutput");
		int op = uiFolderChooser.showOpenDialog(this);
		if (op == JFileChooser.APPROVE_OPTION) {
			File file = uiFolderChooser.getSelectedFile();
			uiOutput.setText(file.getAbsolutePath());
		}
	}
	
	protected void uiAddScaleActionPerformed(ActionEvent evt) {
		uiScale.addData();
	}
	
	protected void uiDeleteScaleActionPerformed(ActionEvent evt) {
		int rowIndexView = uiScale.getSelectedRow();
		if (rowIndexView != -1) {
			uiScale.deleteData(rowIndexView);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	protected void uiPackSelectedActionPerformed(ActionEvent evt) {
		if (uiPack.getSelectedIndex() == -1) {
			showErrorMsg("No Pack selected!");
			return;
		}
		UpdatePack(uiPack.getSelectedValue());
		
		new SwingWorker<String, String>() {
			
			@Override
			protected String doInBackground () throws Exception {
				publish("==========START==========");
				Pack pack = uiPack.getSelectedValue();
				publish("----->" + pack.getPackName() + "<-----");
				if (pack.getInput().length() == 0) {
					publish(pack.getPackName() + " have no input directory!");
				} else {
					if (pack.getOutput().length() == 0) {
						pack.setOutput(pack.getInput() + File.separator + "pack");
						uiOutput.setText(pack.getOutput());
					}
					TexturePacker.process(pack.getSettings(), pack.getInput(), pack.getOutput(), pack.getPackName());
				}
				return null;
			}

			@Override
			protected void process (List<String> chunks) {
				for (String chunk: chunks) {
					uiLog.setText(uiLog.getText() + chunk + "\n");
				}
			}

			@Override
			protected void done () {
				try {
					get();
				} catch (Exception e) {
					uiLog.setText(uiLog.getText() + e.getMessage() + "\n");
					showErrorMsg(e);
				} finally {
					uiLog.setText(uiLog.getText() + "==========FINISH==========\n\n");
				}
			}
		}.execute();
	}
	
	@SuppressWarnings("synthetic-access")
	protected void uiPackAllActionPerformed(ActionEvent evt) {
		if(uiPackModel.isEmpty()) {
			showErrorMsg("No Pack!");
			return;
		}
		if (uiPack.getSelectedIndex() > -1) {
			UpdatePack(uiPack.getSelectedValue());
		}
		new SwingWorker<String, String>() {
			
			@Override
			protected String doInBackground () throws Exception {
				publish("==========START==========");
				for (int i = 0; i < uiPackModel.size(); i++) {
					Pack pack = uiPackModel.get(i);
					publish("----->" + pack.getPackName() + "<-----");
					if (pack.getInput().length() == 0) {
						publish(pack.getPackName() + " have no input directory!");
						continue;
					}
					if (pack.getOutput().length() == 0) {
						pack.setOutput(pack.getInput() + File.separator + "pack");
						if (pack.equals(uiPack.getSelectedValue())) {
							uiOutput.setText(pack.getOutput());
						}
					}
					TexturePacker.process(pack.getSettings(), pack.getInput(), pack.getOutput(), pack.getPackName());
				}
				return null;
			}

			@Override
			protected void process (List<String> chunks) {
				for (String chunk: chunks) {
					uiLog.setText(uiLog.getText() + chunk + "\n");
				}
			}

			@Override
			protected void done () {
				try {
					get();
				} catch (Exception e) {
					uiLog.setText(uiLog.getText() + e.getMessage() + "\n");
					showErrorMsg(e);
				} finally {
					uiLog.setText(uiLog.getText() + "==========FINISH==========\n\n");
				}
			}
		}.execute();
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
	
	public String showInputDialog(String packName) {
		return (String) JOptionPane.showInputDialog(this, "Pack Name", TITLE, JOptionPane.QUESTION_MESSAGE, null, null, packName);
	}
	
	public String floatArrayToString(float[] objs) {
		String s = "";
		if (objs != null && objs.length > 0) {
			for (float obj: objs) {
				s = s + Float.toString(obj) + ",";
			}
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	public String stringArrayToString(String[] objs) {
		String s = "";
		if (objs != null && objs.length > 0) {
			for (String obj: objs) {
				if (obj.isEmpty()) {
					obj = "null";
				}
				s = s + obj + ",";
			}
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	public float[] stringToFloatArray(String s) {
		if (s != null && s.length() > 0) {
			String[] ss = s.split(",");
			float[] f = new float[ss.length];
			for (int i = 0; i < ss.length; i++) {
				f[i] = Float.parseFloat(ss[i]);
			}
			return f;
		}
		return null;
	}
	
	public String[] stringToStringArray(String s) {
		if (s != null && s.length() > 0) {
			String[] ss = s.split(",");
			for (int i = 0; i < ss.length; i++) {
				if (ss[i].equals("null")) {
					ss[i] = "";
				}
			}
			return ss;
		}
		return null;
	}
	
	class Pack {
		private String packName;
		private String output;
		private String input;
		private Settings settings;

		public Pack () {
		}

		public Pack (String packName) {
			this.packName = packName;
			output = "";
			input = "";
			settings = new Settings();
		}

		public String getPackName () {
			return packName;
		}

		public void setPackName (String packName) {
			this.packName = packName;
		}

		public String getOutput () {
			return output;
		}

		public void setOutput (String output) {
			this.output = output;
		}

		public String getInput () {
			return input;
		}

		public void setInput (String input) {
			this.input = input;
		}

		public Settings getSettings () {
			return settings;
		}

		public void setSettings (Settings settings) {
			this.settings = settings;
		}

		@Override
		public String toString () {
			return packName;
		}
	}
	
	class LogOut extends PrintStream {
		private JTextComponent uiTextComponent;
		private StringBuffer sb = new StringBuffer();
		
		public LogOut (OutputStream out, JTextComponent textComponent) {
			super(out);
			this.uiTextComponent = textComponent;
		}
		
		@Override
		public void write (byte[] buf, int off, int len) {
			final String message = new String(buf, off, len);
			sb.append(message);
			uiTextComponent.setText(sb.toString());
		}
		
	}
}
