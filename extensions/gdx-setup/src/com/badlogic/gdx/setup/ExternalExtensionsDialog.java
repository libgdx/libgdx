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

package com.badlogic.gdx.setup;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.SOUTHEAST;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.badlogic.gdx.setup.GdxSetupUI.SetupButton;

public class ExternalExtensionsDialog extends JDialog implements TableModelListener {

	private JPanel contentPane;
	private SetupButton buttonOK;
	private SetupButton buttonCancel;
	private JPanel topPanel;
	private ExtensionTableModel tableModel;
	JTable table;
	private JPanel bottomPanel;
	private JPanel buttonPanel;
	private JScrollPane scrollPane;

	private JLabel warningNotice;
	private JLabel warningNotice2;

	private List<Dependency> mainDependenciesSnapshot = new ArrayList<Dependency>();
	List<Dependency> mainDependencies;

	public ExternalExtensionsDialog (List<Dependency> mainDependencies) {
		this.mainDependencies = mainDependencies;

		contentPane = new JPanel(new GridBagLayout());
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		uiLayout();
		uiStyle();

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				onOK();
			}
		});
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				onCancel();
			}
		});

		setTitle("Third party external extensions");
		setSize(600, 300);
		setLocationRelativeTo(null);
	}

	public void showDialog () {
		takeSnapshot();
		setVisible(true);
	}

	private void uiLayout () {

		topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		warningNotice = new JLabel("List of third party extensions for LibGDX");
		warningNotice2 = new JLabel("These are not maintained by the LibGDX team, please see the support links for info and help");
		warningNotice.setHorizontalAlignment(JLabel.CENTER);
		warningNotice2.setHorizontalAlignment(JLabel.CENTER);

		topPanel.add(warningNotice, new GridBagConstraints(0, 0, 1, 1, 1, 0, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(warningNotice2, new GridBagConstraints(0, 1, 1, 1, 1, 0, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		JSeparator separator = new JSeparator();
		separator.setForeground(new Color(85, 85, 85));
		separator.setBackground(new Color(85, 85, 85));

		topPanel.add(separator, new GridBagConstraints(0, 2, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		tableModel = new ExtensionTableModel();
		table = new JTable(tableModel) {
			@Override
			public String getToolTipText (MouseEvent e) {
				return ((ExtensionTableModel)getModel()).getToolTip(e);
			}
		};
		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(20);
		table.getColumnModel().getColumn(4).setPreferredWidth(30);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		table.getTableHeader().setReorderingAllowed(false);
		table.getModel().addTableModelListener(this);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked (MouseEvent e) {
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();
				if (column == 5) {
					URI uri = ((ExtensionTableModel)table.getModel()).getURI(row, column);
					if (uri != null) {
						try {
							Desktop.getDesktop().browse(uri);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		scrollPane = new JScrollPane(table);

		bottomPanel = new JPanel(new GridBagLayout());

		buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonOK = new SetupButton("Save");
		buttonCancel = new SetupButton("Cancel");
		buttonPanel.add(buttonOK, new GridBagConstraints(0, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		buttonPanel.add(buttonCancel, new GridBagConstraints(1, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(buttonPanel, new GridBagConstraints(3, 0, 1, 1, 1, 1, SOUTHEAST, NONE, new Insets(0, 0, 0, 0), 0, 0));

		contentPane.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0.1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		contentPane.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		contentPane.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 1, 0, SOUTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		try {
			initData();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initData () throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		Document doc = builder.parse(ExternalExtensionsDialog.class
			.getResourceAsStream("/com/badlogic/gdx/setup/data/extensions.xml"));

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("extension");

		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element)nNode;
				String name = eElement.getElementsByTagName("name").item(0).getTextContent();
				String description = eElement.getElementsByTagName("description").item(0).getTextContent();
				String version = eElement.getElementsByTagName("version").item(0).getTextContent();
				String compatibility = eElement.getElementsByTagName("compatibility").item(0).getTextContent();
				String url = eElement.getElementsByTagName("website").item(0).getTextContent();

				String[] gwtInherits = null;
				NodeList inheritsNode = eElement.getElementsByTagName("inherit");
				gwtInherits = new String[inheritsNode.getLength()];

				for (int j = 0; j < inheritsNode.getLength(); j++)
					gwtInherits[j] = inheritsNode.item(j).getTextContent();

				final HashMap<String, List<ExternalExtensionDependency>> dependencies = new HashMap<String, List<ExternalExtensionDependency>>();

				addToDependencyMapFromXML(dependencies, eElement, "core");
				addToDependencyMapFromXML(dependencies, eElement, "desktop");
				addToDependencyMapFromXML(dependencies, eElement, "android");
				addToDependencyMapFromXML(dependencies, eElement, "ios");
				addToDependencyMapFromXML(dependencies, eElement, "ios-moe");
				addToDependencyMapFromXML(dependencies, eElement, "html");

				URI uri = null;
				try {
					uri = new URI(url);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				if (uri != null) {
					final ExternalExtension extension = new ExternalExtension(name, gwtInherits, description, version);
					extension.setDependencies(dependencies);
					tableModel.addExtension(extension, false, name, description, version, compatibility, uri);
				}
			}
		}
	}

	private void uiStyle () {
		contentPane.setBackground(new Color(36, 36, 36));

		topPanel.setBackground(new Color(36, 36, 36));
		topPanel.setForeground(new Color(255, 255, 255));
		table.setBackground(new Color(46, 46, 46));
		table.setForeground(new Color(255, 255, 255));
		bottomPanel.setBackground(new Color(36, 36, 36));
		bottomPanel.setForeground(new Color(255, 255, 255));
		buttonPanel.setBackground(new Color(36, 36, 36));
		buttonPanel.setForeground(new Color(255, 255, 255));

		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBackground(new Color(36, 36, 36));
		scrollPane.getViewport().setBackground(new Color(36, 36, 36));

		warningNotice.setForeground(new Color(255, 20, 20));
		warningNotice2.setForeground(new Color(255, 20, 20));
	}

	void onOK () {
		setVisible(false);
	}

	void onCancel () {
		setVisible(false);
		restore();
	}

	private void takeSnapshot () {
		mainDependenciesSnapshot.clear();
		for (int i = 0; i < mainDependencies.size(); i++) {
			mainDependenciesSnapshot.add(mainDependencies.get(i));
		}
	}

	private void restore () {
		mainDependencies.clear();
		((ExtensionTableModel)table.getModel()).unselectAll();
		for (int i = 0; i < mainDependenciesSnapshot.size(); i++) {
			mainDependencies.add(mainDependenciesSnapshot.get(i));
			String extensionName = mainDependenciesSnapshot.get(i).getName();
			if (((ExtensionTableModel)table.getModel()).hasExtension(extensionName)) {
				((ExtensionTableModel)table.getModel()).setSelected(extensionName, true);
			} else {
			}
		}
	}

	private void addToDependencyMapFromXML (Map<String, List<ExternalExtensionDependency>> dependencies, Element eElement, String platform) {
		if (eElement.getElementsByTagName(platform).item(0) != null) {
			Element project = (Element)eElement.getElementsByTagName(platform).item(0);

			ArrayList<ExternalExtensionDependency> deps = new ArrayList<ExternalExtensionDependency>();

			if (project.getTextContent().trim().equals("")) {
				// No dependencies required
			} else if (project.getTextContent().trim().equals("null")) {
				// Not supported
				deps = null;
			} else {
				NodeList nList = project.getElementsByTagName("dependency");

				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element dependencyNode = (Element)nNode;
						boolean external = Boolean.parseBoolean(dependencyNode.getAttribute("external"));
						deps.add(new ExternalExtensionDependency(dependencyNode.getTextContent(), external));
					}

				}
			}

			dependencies.put(platform, deps);
		}
	}

	class ExtensionTableModel extends DefaultTableModel {

		private HashMap<Integer, ExternalExtension> extensions = new HashMap<Integer, ExternalExtension>();
		private int rowCount = 0;

		public ExtensionTableModel () {
			addColumn("Use");
			addColumn("Extension");
			addColumn("Description");
			addColumn("Version");
			addColumn("Compatibility");
			addColumn("Support");
		}

		public ExternalExtension getExtension (int row) {
			return extensions.get(row);
		}

		public URI getURI (int row, int column) {
			if (column != 5) return null;
			return (URI)getValueAt(row, column);
		}

		@Override
		public Class getColumnClass (int column) {
			if (column == 0) return Boolean.class;
			if (column == 5) return URI.class;
			return super.getColumnClass(column);
		}

		@Override
		public boolean isCellEditable (int x, int y) {
			return y == 0;
		}

		public String getToolTip (MouseEvent e) {
			int row = table.rowAtPoint(e.getPoint());
			int column = table.columnAtPoint(e.getPoint());

			if (column == 5) {
				return "Click me!";
			} else if (column != 0) {
				return getValueAt(row, column).toString();
			} else {
				return "Select if you want to use this extension!";
			}
		}

		public void unselectAll () {
			for (int row : extensions.keySet()) {
				table.setValueAt(false, row, 0);
			}
		}

		public boolean hasExtension (String extensionName) {
			for (ExternalExtension extension : extensions.values()) {
				if (extension.getName().equals(extensionName)) return true;
			}
			return false;
		}

		public void setSelected (String extensionName, boolean selected) {
			int row = -1;
			for (int i : extensions.keySet()) {
				if (extensions.get(i).getName().equals(extensionName)) {
					row = i;
					break;
				}
			}
			if (row != -1) table.setValueAt(selected, row, 0);
		}

		public void addExtension (ExternalExtension extension, Boolean checkbox, String name, String description, String version,
			String compatibility, URI support) {
			addRow(new Object[] {checkbox, name, description, version, compatibility, support});
			extensions.put(rowCount++, extension);
		}

	}
	
	@Override
	public void tableChanged (TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();

		if (column == 0) {
			ExternalExtension extension = ((ExtensionTableModel)table.getModel()).getExtension(row);
			Dependency dep = extension.generateDependency();
			boolean selected = (Boolean)table.getModel().getValueAt(row, 0);
			if (selected) {
				if (!mainDependencies.contains(dep)) {
					mainDependencies.add(dep);
				}
			} else {
				mainDependencies.remove(dep);
			}
		}
	}

}
