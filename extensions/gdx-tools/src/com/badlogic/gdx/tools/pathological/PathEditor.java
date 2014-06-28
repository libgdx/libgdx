
package com.badlogic.gdx.tools.pathological;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.tools.pathological.PathModel.PathNode;
import com.badlogic.gdx.tools.pathological.util.PathSerializer;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.StreamUtils;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class PathEditor {
	private static final Color DEFAULT_TEXT_COLOR = new Color(51, 51, 51);

	/** Launch the application. */
	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run () {
				try {
					PathEditor window = new PathEditor();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	JPanel canvas;
	JScrollPane canvasPane;
	JComboBox<EditMode> editModeComboBox;
	JFileChooser exportFileChooser;
	JFrame frame;
	JFileChooser imageFileChooser;
	JFileChooser saveProjectChooser;
	JFileChooser openProjectChooser;
	BufferedImage image;
	private JCheckBox continuousCheckBox;
	private JComboBox<PathType> curveTypeComboBox;
	private JLabel curveTypeLabel;
	private JButton deletePathButton;
	private JLabel editModeLabel;
	private JPanel editorPane;
	private JMenu fileMenu;
	private JSpinner gridSnapSpinner;
	private JMenu helpMenu;
	private JMenuBar menuBar;
	private JMenu mnPath;
	private JMenuItem mntmAbout;
	private JMenuItem createPathMenuItem;
	private JMenuItem deleteSelectedNodesMenuItem;

	private JMenuItem exportPathMenuItem;
	private JMenuItem mntmNew;
	private JMenuItem mntmOpen;
	private JMenuItem mntmQuit;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JLabel mouseCoordsLabel;
	private JButton newPathButton;
	private JTextField pathNameField;
	private JLabel pathNameLabel;
	private JComboBox<PathModel> selectedPathComboBox;
	private JLabel selectedPathLabel;
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JCheckBox showGridCheckBox;
	private JCheckBox snapToGridCheckBox;
	private PathProject project;
	private File projectFile;
	private int current;

	private JSplitPane splitPane;
	private JMenuItem removePathMenuItem;
	private JLabel nodeCountLabel;
	private JMenu mnProject;
	private JMenuItem mntmSetImage;
	private JMenuItem mntmRemoveImage;

	private About about;

	/** Create the application. */
	private PathEditor () {
		initialize();
		updateUI();
	}

	private void initExportFileChooser () {
		this.exportFileChooser = new JFileChooser(".");
		this.exportFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.exportFileChooser.setMultiSelectionEnabled(false);
		this.exportFileChooser.setDialogTitle("Export the selected path to JSON");
		FileFilter jsonfilter = new FileNameExtensionFilter("JSON", "json");
		this.exportFileChooser.addChoosableFileFilter(jsonfilter);
	}

	private void updateUI () {
		PathModel model = project.getPathModel(current);
		boolean isvalid = (model != null);

		continuousCheckBox.setEnabled(isvalid);
		pathNameField.setEnabled(isvalid);
		selectedPathComboBox.setEnabled(isvalid);
		curveTypeComboBox.setEnabled(isvalid);
		deletePathButton.setEnabled(isvalid);
		editModeComboBox.setEnabled(isvalid);
		if (deleteSelectedNodesMenuItem != null) {
			deleteSelectedNodesMenuItem.setEnabled(isvalid);
		}

		if (removePathMenuItem != null) {
			removePathMenuItem.setEnabled(isvalid);
		}

		if (exportPathMenuItem != null) {
			exportPathMenuItem.setEnabled(isvalid);
		}

		if (model != null) {
			if (model.getType().supportsClosedPaths()) {
				continuousCheckBox.setSelected(model.isContinuous());
				continuousCheckBox.setEnabled(true);
			} else {
				continuousCheckBox.setEnabled(false);
			}
			pathNameField.setText(model.getName());
			curveTypeComboBox.setSelectedItem(model.getType());
			nodeCountLabel.setText(model.getNumberOfNodes() + " nodes");
			nodeCountLabel.setForeground(model.getType().acceptModel(model) ? DEFAULT_TEXT_COLOR : Color.RED);
			frame.setTitle(String.format("Pathological - %s%s", (projectFile == null) ? "New Project" : projectFile,
				project.hasChanged() ? "*" : ""));
		} else {
			nodeCountLabel.setForeground(DEFAULT_TEXT_COLOR);
			nodeCountLabel.setText("No path selected");
		}

		if (mntmRemoveImage != null) {
			mntmRemoveImage.setEnabled(image != null);
		}

		if (deleteSelectedNodesMenuItem != null) {
			deleteSelectedNodesMenuItem.setEnabled((project != null && model != null) ? model.anySelected() : false);
		}

		if (canvasPane != null) {
			canvasPane.validate();
		}

		if (canvas != null) {
			canvas.repaint();
		}
	}

	/** Initialize the contents of the frame. */
	private void initialize () {
		this.imageFileChooser = new JFileChooser(".");
		this.imageFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.imageFileChooser.setMultiSelectionEnabled(false);
		this.imageFileChooser.setDialogTitle("Choose a background");

		this.saveProjectChooser = new JFileChooser(".");
		this.saveProjectChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.saveProjectChooser.setMultiSelectionEnabled(false);
		this.saveProjectChooser.setDialogTitle("Save project");

		this.openProjectChooser = new JFileChooser(".");
		this.openProjectChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.openProjectChooser.setMultiSelectionEnabled(false);
		this.openProjectChooser.setDialogTitle("Open project");

		this.about = new About();
		this.project = new PathProject();
		this.current = 0;

		frame = new JFrame("Pathological");
		frame.setBounds(100, 100, 607, 407);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		editorPane = new JPanel();
		editorPane.setBorder(new EmptyBorder(4, 4, 4, 4));
		splitPane.setLeftComponent(editorPane);
		GridBagLayout gbl_editorPane = new GridBagLayout();
		gbl_editorPane.columnWidths = new int[] {116, 0, 0};
		gbl_editorPane.rowHeights = new int[] {0, 0, 0, 0, 25, 0, 0, 0, 0, 12, 0, 0};
		gbl_editorPane.columnWeights = new double[] {1.0, 1.0, Double.MIN_VALUE};
		gbl_editorPane.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		editorPane.setLayout(gbl_editorPane);

		snapToGridCheckBox = new JCheckBox("Snap to Grid");
		GridBagConstraints gbc_snapToGridCheckBox = new GridBagConstraints();
		gbc_snapToGridCheckBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_snapToGridCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_snapToGridCheckBox.gridx = 0;
		gbc_snapToGridCheckBox.gridy = 0;
		editorPane.add(snapToGridCheckBox, gbc_snapToGridCheckBox);
		snapToGridCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				boolean selected = snapToGridCheckBox.isSelected();
				gridSnapSpinner.setEnabled(selected);
				updateUI();
			}
		});

		gridSnapSpinner = new JSpinner();
		GridBagConstraints gbc_gridSnapSpinner = new GridBagConstraints();
		gbc_gridSnapSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_gridSnapSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_gridSnapSpinner.gridx = 1;
		gbc_gridSnapSpinner.gridy = 0;
		editorPane.add(gridSnapSpinner, gbc_gridSnapSpinner);
		gridSnapSpinner.setEnabled(false);
		gridSnapSpinner.setModel(new SpinnerNumberModel(new Integer(32), new Integer(2), null, new Integer(1)));

		showGridCheckBox = new JCheckBox("Show Grid");
		GridBagConstraints gbc_showGridCheckBox = new GridBagConstraints();
		gbc_showGridCheckBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_showGridCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_showGridCheckBox.gridx = 0;
		gbc_showGridCheckBox.gridy = 1;
		editorPane.add(showGridCheckBox, gbc_showGridCheckBox);

		editModeLabel = new JLabel("Edit Mode");
		GridBagConstraints gbc_editModeLabel = new GridBagConstraints();
		gbc_editModeLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_editModeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_editModeLabel.gridx = 0;
		gbc_editModeLabel.gridy = 2;
		editorPane.add(editModeLabel, gbc_editModeLabel);

		editModeComboBox = new JComboBox();
		editModeLabel.setLabelFor(editModeComboBox);
		GridBagConstraints gbc_editModeComboBox = new GridBagConstraints();
		gbc_editModeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_editModeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_editModeComboBox.gridx = 1;
		gbc_editModeComboBox.gridy = 2;
		editorPane.add(editModeComboBox, gbc_editModeComboBox);
		editModeComboBox.setModel(new DefaultComboBoxModel(EditMode.values()));

		selectedPathLabel = new JLabel("Selected");
		GridBagConstraints gbc_selectedPathLabel = new GridBagConstraints();
		gbc_selectedPathLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectedPathLabel.insets = new Insets(0, 0, 5, 5);
		gbc_selectedPathLabel.gridx = 0;
		gbc_selectedPathLabel.gridy = 3;
		editorPane.add(selectedPathLabel, gbc_selectedPathLabel);

		selectedPathComboBox = new JComboBox();
		selectedPathComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				PathModel model = project.getPathModel(current);
				if (model != null) {
					model.unselectAll();
				}
				current = selectedPathComboBox.getSelectedIndex();
				updateUI();

			}
		});
		selectedPathComboBox.setToolTipText("Select a Path to edit (you can have one active at a time)");
		selectedPathLabel.setLabelFor(selectedPathComboBox);
		GridBagConstraints gbc_selectedPathComboBox = new GridBagConstraints();
		gbc_selectedPathComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectedPathComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_selectedPathComboBox.gridx = 1;
		gbc_selectedPathComboBox.gridy = 3;
		editorPane.add(selectedPathComboBox, gbc_selectedPathComboBox);

		separator_2 = new JSeparator();
		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
		gbc_separator_2.gridwidth = 2;
		gbc_separator_2.insets = new Insets(0, 0, 5, 0);
		gbc_separator_2.gridx = 0;
		gbc_separator_2.gridy = 4;
		editorPane.add(separator_2, gbc_separator_2);

		newPathButton = new JButton("Create Path");
		newPathButton.setToolTipText("Create a new path for this project and start editing it");
		GridBagConstraints gbc_newPathButton = new GridBagConstraints();
		gbc_newPathButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_newPathButton.insets = new Insets(0, 0, 5, 5);
		gbc_newPathButton.gridx = 0;
		gbc_newPathButton.gridy = 5;
		editorPane.add(newPathButton, gbc_newPathButton);

		deletePathButton = new JButton("Delete Path");
		deletePathButton.setToolTipText("Remove the selected path from this project");
		GridBagConstraints gbc_deletePathButton = new GridBagConstraints();
		gbc_deletePathButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_deletePathButton.insets = new Insets(0, 0, 5, 0);
		gbc_deletePathButton.gridx = 1;
		gbc_deletePathButton.gridy = 5;
		editorPane.add(deletePathButton, gbc_deletePathButton);

		pathNameLabel = new JLabel("Name");
		GridBagConstraints gbc_pathNameLabel = new GridBagConstraints();
		gbc_pathNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_pathNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_pathNameLabel.gridx = 0;
		gbc_pathNameLabel.gridy = 6;
		editorPane.add(pathNameLabel, gbc_pathNameLabel);

		pathNameField = new JTextField();
		pathNameLabel.setLabelFor(pathNameField);
		GridBagConstraints gbc_pathNameField = new GridBagConstraints();
		gbc_pathNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_pathNameField.insets = new Insets(0, 0, 5, 0);
		gbc_pathNameField.gridx = 1;
		gbc_pathNameField.gridy = 6;
		editorPane.add(pathNameField, gbc_pathNameField);
		pathNameField.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				project.getPathModel(current).setName(pathNameField.getText());
				updateUI();
			}
		});
		pathNameField.setText(project.getPathModel(current).getName());
		pathNameField.setColumns(10);

		curveTypeLabel = new JLabel("Type");
		GridBagConstraints gbc_curveTypeLabel = new GridBagConstraints();
		gbc_curveTypeLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_curveTypeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_curveTypeLabel.gridx = 0;
		gbc_curveTypeLabel.gridy = 7;
		editorPane.add(curveTypeLabel, gbc_curveTypeLabel);

		curveTypeComboBox = new JComboBox();
		curveTypeComboBox.setToolTipText("Each available path type is quite distinct");
		curveTypeLabel.setLabelFor(curveTypeComboBox);
		GridBagConstraints gbc_curveTypeComboBox = new GridBagConstraints();
		gbc_curveTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_curveTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_curveTypeComboBox.gridx = 1;
		gbc_curveTypeComboBox.gridy = 7;
		editorPane.add(curveTypeComboBox, gbc_curveTypeComboBox);
		curveTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				PathType type = (PathType)curveTypeComboBox.getSelectedItem();
				project.getPathModel(current).setType(type);
				updateUI();
			}
		});
		curveTypeComboBox.setModel(new DefaultComboBoxModel(PathType.values()));

		continuousCheckBox = new JCheckBox("Continuous");
		continuousCheckBox.setToolTipText("If checked, the path will be a closed loop (if supported by the path type)");
		GridBagConstraints gbc_continuousCheckBox = new GridBagConstraints();
		gbc_continuousCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_continuousCheckBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_continuousCheckBox.gridx = 1;
		gbc_continuousCheckBox.gridy = 8;
		editorPane.add(continuousCheckBox, gbc_continuousCheckBox);

		separator_3 = new JSeparator();
		GridBagConstraints gbc_separator_3 = new GridBagConstraints();
		gbc_separator_3.insets = new Insets(0, 0, 5, 0);
		gbc_separator_3.gridwidth = 2;
		gbc_separator_3.gridx = 0;
		gbc_separator_3.gridy = 9;
		editorPane.add(separator_3, gbc_separator_3);

		mouseCoordsLabel = new JLabel("(0, 0)");
		GridBagConstraints gbc_mouseCoordsLabel = new GridBagConstraints();
		gbc_mouseCoordsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_mouseCoordsLabel.gridx = 0;
		gbc_mouseCoordsLabel.gridy = 10;
		editorPane.add(mouseCoordsLabel, gbc_mouseCoordsLabel);
		editorPane.add(separator_3, gbc_separator_3);

		nodeCountLabel = new JLabel("0 nodes");
		nodeCountLabel.setToolTipText("If this label is red, the current path is not well-formed");
		GridBagConstraints gbc_nodeCountLabel = new GridBagConstraints();
		gbc_nodeCountLabel.gridx = 1;
		gbc_nodeCountLabel.gridy = 10;
		editorPane.add(nodeCountLabel, gbc_nodeCountLabel);
		continuousCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				project.getPathModel(current).setContinuous(continuousCheckBox.isSelected());
				updateUI();
			}
		});
		deletePathButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				PathModel model = project.getPathModel(current);
				project.removePathModel(model);
				selectedPathComboBox.removeItem(model);
				current = selectedPathComboBox.getSelectedIndex();
				updateUI();
			}
		});
		newPathButton.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				PathModel path = project.getPathModel(current);
				if (path != null) {
					path.unselectAll();
				}

				PathModel pm = project.createPathModel();
				selectedPathComboBox.addItem(pm);
				selectedPathComboBox.setSelectedItem(pm);
				editModeComboBox.setSelectedItem(EditMode.ADD);
				current = selectedPathComboBox.getSelectedIndex();
				pm.setType((PathType)curveTypeComboBox.getSelectedItem());
				pm.setContinuous(continuousCheckBox.isSelected());
				updateUI();
			}
		});
		selectedPathComboBox.addItem(project.getPathModel(current));
		showGridCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				canvas.repaint();
			}
		});
		canvasPane = new JScrollPane();
		splitPane.setRightComponent(canvasPane);

		canvas = new JPanel() {
			@Override
			protected void paintComponent (Graphics g) {
				super.paintComponent(g);
				if (image != null) {
					g.drawImage(image, 0, 0, null);
				}
				Color old = g.getColor();
				if (showGridCheckBox.isSelected()) {
					g.setColor(Color.GREEN);
					int interval = (int)PathEditor.this.gridSnapSpinner.getValue();
					for (int y = 0; y < canvas.getHeight(); y += interval) {
						g.drawLine(0, y, canvas.getWidth(), y);
					}

					for (int x = 0; x < canvas.getWidth(); x += interval) {
						g.drawLine(x, 0, x, canvas.getHeight());
					}
				}

				PathModel model = project.getPathModel(current);
				if (model != null) {
					model.draw((Graphics2D)g);
				}

				g.setColor(old);
			}
		};
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			private Point lastPoint = new Point();
			private Point temp = new Point();

			@Override
			public void mouseDragged (MouseEvent e) {
				PathModel model = project.getPathModel(current);
				if (model != null) {

					switch ((EditMode)editModeComboBox.getSelectedItem()) {
					case SELECT: {
						if (model.getNumberOfNodes() > 0) {
							temp.setLocation(e.getX() - lastPoint.x, e.getY() - lastPoint.y);
							int snap = snapToGridCheckBox.isSelected() ? (int)gridSnapSpinner.getValue() : 0;
							model.moveSelectedNodesBy(temp.x, temp.y, snap);
							canvas.repaint();
						}
						this.lastPoint.setLocation(e.getX(), e.getY());
						break;
					}
					default:
						// nop
					}
				}
				mouseCoordsLabel.setText(String.format("(%d, %d)", e.getX(), e.getY()));
			}

			@Override
			public void mouseMoved (MouseEvent e) {
				this.lastPoint.setLocation(e.getX(), e.getY());
				mouseCoordsLabel.setText(String.format("(%d, %d)", e.getX(), e.getY()));
			}
		});
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped (KeyEvent e) {
				KeyStroke deleteKey = deleteSelectedNodesMenuItem.getAccelerator();
				if (e.getKeyCode() == deleteKey.getKeyCode()) {
					deleteSelectedNodesMenuItem.dispatchEvent(e);
				}
			}
		});
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed (MouseEvent e) {
				PathModel model = project.getPathModel(current);
				if (model != null) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						// If the user clicked the left mouse button...
						switch ((EditMode)editModeComboBox.getSelectedItem()) {
						case ADD: {
							model.addNode(e.getX(), e.getY());
							break;
						}
						case SELECT: {
							PathNode node = model.getNode(e.getX(), e.getY());
							if (node != null) {
								model.select(node);
							}
							break;
						}
						default:
							// nop
						}
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						model.unselectAll();
					} else if (e.getButton() == MouseEvent.BUTTON2) {

					}
				}
				updateUI();
			}
		});
		canvas.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained (FocusEvent e) {
				canvas.repaint();
			}
		});
		canvas.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden (ComponentEvent e) {
				this.doStuff(e);
			}

			@Override
			public void componentMoved (ComponentEvent e) {
				this.doStuff(e);
			}

			@Override
			public void componentResized (ComponentEvent e) {
				this.doStuff(e);
			}

			@Override
			public void componentShown (ComponentEvent e) {
				this.doStuff(e);
			}

			private void doStuff (ComponentEvent e) {
				canvas.paintComponents(canvas.getGraphics());
			}
		});
		canvas.setBackground(Color.BLACK);
		canvasPane.setViewportView(canvas);
		canvas.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (confirmIfChanged("You have unsaved work. If you create a new project before saving, it will be lost. Continue?")) {
					project = new PathProject();
					projectFile = null;
					image = null;
					canvasPane.doLayout();
					updateUI();
				}
			}
		});
		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		fileMenu.add(mntmNew);

		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			Json json = new Json();

			public void actionPerformed (ActionEvent e) {
				if (confirmIfChanged("You have unsaved work. If you open another project before saving, it will be lost. Continue?")) {

					File file = getOpenLocation(PathEditor.this.openProjectChooser);
					if (file != null) {
						try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
							project = (PathProject)in.readObject();
							current = 0;
							selectedPathComboBox.removeAllItems();
							for (int i = 0; i < project.getNumberOfPathModels(); ++i) {
								selectedPathComboBox.addItem(project.getPathModel(i));
							}
							projectFile = file;
							project.unsetChanged();
							updateUI();
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, "Not a valid Pathological project file", "Invalid project",
								JOptionPane.ERROR_MESSAGE);
							ex.printStackTrace();
						}
					}
				}
			}
		});
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(mntmOpen);

		mntmSave = new JMenuItem("Save project");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (projectFile == null) {
					// If this is a new project...
					mntmSaveAs.getActionListeners()[0].actionPerformed(e); // HACK
				} else {
					try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(projectFile))) {
						out.writeObject(project);
						project.unsetChanged();
						updateUI();
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null, ex, "Couldn't save file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu.add(mntmSave);

		mntmSaveAs = new JMenuItem("Save project as...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				File file = PathEditor.this.getSaveLocation(PathEditor.this.saveProjectChooser);
				if (file != null) {
					try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
						out.writeObject(project);
						projectFile = file;
						project.unsetChanged();
						updateUI();
					} catch (Exception ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null, ex, "Couldn't save file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		fileMenu.add(mntmSaveAs);

		exportPathMenuItem = new JMenuItem("Export this path");
		exportPathMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				PathModel model = project.getPathModel(current);
				if (model == null) {
					JOptionPane.showMessageDialog(null, "Please select a path to export", "No path selected",
						JOptionPane.ERROR_MESSAGE);
				} else {
					PathType type = model.getType();
					if (type.acceptModel(model)) {
						if (exportFileChooser == null) {
							initExportFileChooser();
						}
						File file = PathEditor.this.getSaveLocation(exportFileChooser);
						if (file != null) {
							try (FileWriter writer = new FileWriter(file)) {
								PathType.json.toJson(model.getPath(), writer);
							} catch (IOException ex) {
								JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					} else {
						JOptionPane.showMessageDialog(null, type.getInvalidPathMessage(), "Path is not valid",
							JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		fileMenu.add(exportPathMenuItem);

		separator = new JSeparator();
		fileMenu.add(separator);

		mntmQuit = new JMenuItem("Quit");
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				if (confirmIfChanged("You have unsaved changes. If you quit before saving, they will be lost. Exit?")) {
					frame.setVisible(false);
					System.exit(0);
				}
			}
		});
		fileMenu.add(mntmQuit);

		mnProject = new JMenu("Project");
		menuBar.add(mnProject);

		mntmSetImage = new JMenuItem("Set Image");
		mntmSetImage.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				imageFileChooser.showOpenDialog(null);
				File selected = imageFileChooser.getSelectedFile();
				if (selected != null) {
					try {
						project.setImageFile(selected.toString());
						image = ImageIO.read(selected);
						if (image == null) {
							JOptionPane.showMessageDialog(null, "Not a valid image", "Invalid image", JOptionPane.ERROR_MESSAGE);
						} else {
							canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
							canvasPane.doLayout();
							updateUI();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		mnProject.add(mntmSetImage);

		mntmRemoveImage = new JMenuItem("Remove Image");
		mntmRemoveImage.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				image = null;
				project.setImageFile(null);
				updateUI();
			}
		});
		mnProject.add(mntmRemoveImage);

		mnPath = new JMenu("Path");
		menuBar.add(mnPath);

		deleteSelectedNodesMenuItem = new JMenuItem("Delete Selected Nodes");
		deleteSelectedNodesMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				project.getPathModel(current).deleteSelected();
				updateUI();
			}
		});
		deleteSelectedNodesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		mnPath.add(deleteSelectedNodesMenuItem);

		separator_1 = new JSeparator();
		mnPath.add(separator_1);

		createPathMenuItem = new JMenuItem("Create Path");
		createPathMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				newPathButton.getActionListeners()[0].actionPerformed(e); // HACK
			}
		});
		mnPath.add(createPathMenuItem);

		removePathMenuItem = new JMenuItem("Remove This Path");
		removePathMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				deletePathButton.getActionListeners()[0].actionPerformed(e); // HACK
			}
		});
		mnPath.add(removePathMenuItem);

		helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				about.setVisible(true);
			}
		});
		helpMenu.add(mntmAbout);
	}

	private File getSaveLocation (JFileChooser chooser) {
		boolean done = false;
		File file = null;
		do {
			int result = chooser.showSaveDialog(null);
			file = chooser.getSelectedFile();

			if (result == JFileChooser.APPROVE_OPTION && file != null) {
				if (file.exists()) {
					int overwrite = JOptionPane.showConfirmDialog(null, file + " exists, are you sure you want to overwrite it?",
						"File exists!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (overwrite == JOptionPane.YES_OPTION) {
						return file;
					}
				} else {
					return file;
				}
			} else {
				return null;
			}
		} while (true);
	}

	private File getOpenLocation (JFileChooser chooser) {
		boolean done = false;
		File file = null;
		int result = chooser.showOpenDialog(null);
		file = chooser.getSelectedFile();

		if (result == JFileChooser.APPROVE_OPTION && file != null) {
			return file;
		} else {
			return null;
		}
	}

	private boolean confirmIfChanged (String message) {
		if (project.hasChanged()) {
			int result = JOptionPane.showConfirmDialog(null, message, "Don't forget to save!", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			return result == JOptionPane.YES_OPTION;
		} else {
			return true;
		}
	}
}
