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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.GridBagConstraints.*;

/**
 * Created by azakhary on 2/9/2015.
 */
public class ExternalExtensionsDialog extends JDialog {

	 private JPanel contentPane;
	 private JButton buttonOK;
	 private JButton buttonCancel;
	 private JPanel topPanel;
	 private JPanel content;
	 private JPanel bottomPanel;
	 private JPanel buttonPanel;
	 private JScrollPane scrollPane;

	 private JLabel warningNotice;

	 private HashMap<String, ExtensionRowCheckbox> checkBoxesMap = new HashMap<String, ExtensionRowCheckbox>();

	 private List<Dependency> mainDependenciesSnapshot = new ArrayList<Dependency>();
	 private List<Dependency> mainDependencies;

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
				@Override public void actionPerformed (ActionEvent e) {
					 onCancel();
				}
		  });

		  setTitle("Third party external extensions");
		  setSize(400, 240);
		  setLocationRelativeTo(null);
	 }

	 public void showDialog () {
		  takeSnapshot();
		  setVisible(true);
	 }

	 private void uiLayout () {

		  topPanel = new JPanel(new GridBagLayout());
		  topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		  content = new JPanel(new GridBagLayout());
		  content.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 5));

		  scrollPane = new JScrollPane(content);

		  bottomPanel = new JPanel(new GridBagLayout());

		  buttonPanel = new JPanel(new GridBagLayout());
		  buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		  buttonOK = new JButton("Save");
		  buttonCancel = new JButton("Cancel");
		  buttonPanel.add(buttonOK, new GridBagConstraints(0, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		  buttonPanel.add(buttonCancel, new GridBagConstraints(1, 0, 1, 1, 0, 0, CENTER, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		  bottomPanel.add(buttonPanel, new GridBagConstraints(3, 0, 1, 1, 1, 1, SOUTHEAST, NONE, new Insets(0, 0, 0, 0), 0, 0));

		  contentPane.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		  contentPane.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 1, 1, NORTH, BOTH, new Insets(0, 0, 0, 0), 0, 0));
		  contentPane.add(bottomPanel, new GridBagConstraints(0, 2, 4, 1, 1, 1, SOUTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		  warningNotice = new JLabel("List of third party extensions, not maintained by libGDX team");
		  warningNotice.setHorizontalAlignment(JLabel.CENTER);
		  topPanel.add(warningNotice, new GridBagConstraints(0, 0, 1, 1, 1, 0, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		  JSeparator separator = new JSeparator();
		  separator.setForeground(new Color(85, 85, 85));
		  separator.setBackground(new Color(85, 85, 85));

		  topPanel.add(separator, new GridBagConstraints(0, 1, 4, 1, 1, 1, NORTH, HORIZONTAL, new Insets(10, 0, 0, 0), 0, 0));

		  try {
				initData();
		  } catch (Exception e) {
				e.printStackTrace();
		  }

	 }

	 private void initData () throws ParserConfigurationException, IOException, SAXException {
		  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder builder = dbFactory.newDocumentBuilder();
		  Document doc = builder
			  .parse(ExternalExtensionsDialog.class.getResourceAsStream("/com/badlogic/gdx/setup/data/extensions.xml"));

		  doc.getDocumentElement().normalize();

		  NodeList nList = doc.getElementsByTagName("extension");

		  for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					 Element eElement = (Element)nNode;
					 String name = eElement.getElementsByTagName("name").item(0).getTextContent();
					 String description = eElement.getElementsByTagName("description").item(0).getTextContent();
					 String version = eElement.getElementsByTagName("version").item(0).getTextContent();

					 String rowText = name + " - " + description + " (v" + version + ")";

					 final HashMap<String, List<String>> dependencies = new HashMap<String, List<String>>();

					 addToDependencyMapFromXML(dependencies, eElement, "core");
					 addToDependencyMapFromXML(dependencies, eElement, "desktop");
					 addToDependencyMapFromXML(dependencies, eElement, "android");
					 addToDependencyMapFromXML(dependencies, eElement, "ios");
					 addToDependencyMapFromXML(dependencies, eElement, "html");

					 ExtensionRowCheckbox checkBox = new ExtensionRowCheckbox(rowText);
					 checkBoxesMap.put(name, checkBox);

					 content.add(checkBox, new GridBagConstraints(0, i, 1, 1, 1, 1, NORTH, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

					 final ExternalExtension extension = new ExternalExtension(name, description, version);
					 extension.setDependencies(dependencies);

					 checkBox.addItemListener(new ItemListener() {
						  @Override public void itemStateChanged (ItemEvent e) {
								ExtensionRowCheckbox box = (ExtensionRowCheckbox)e.getSource();
								Dependency dep = extension.generateDependency();
								if (box.isSelected()) {
									 if(!mainDependencies.contains(dep)) {
										  mainDependencies.add(dep);
									 }
								} else {
									 mainDependencies.remove(dep);
								}
						  }
					 });
				}
		  }
	 }

	 private void uiStyle () {
		  contentPane.setBackground(new Color(36, 36, 36));

		  topPanel.setBackground(new Color(36, 36, 36));
		  topPanel.setForeground(new Color(255, 255, 255));
		  content.setBackground(new Color(36, 36, 36));
		  content.setForeground(new Color(255, 255, 255));
		  bottomPanel.setBackground(new Color(36, 36, 36));
		  bottomPanel.setForeground(new Color(255, 255, 255));
		  buttonPanel.setBackground(new Color(36, 36, 36));
		  buttonPanel.setForeground(new Color(255, 255, 255));

		  scrollPane.setBorder(BorderFactory.createEmptyBorder());

		  warningNotice.setForeground(new Color(200, 20, 20));
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
		  for (ExtensionRowCheckbox checkBox : checkBoxesMap.values()) {
				checkBox.setSelected(false);
		  }
		  for (int i = 0; i < mainDependenciesSnapshot.size(); i++) {
				mainDependencies.add(mainDependenciesSnapshot.get(i));
				String extensionName = mainDependenciesSnapshot.get(i).getName();
				if(checkBoxesMap.containsKey(extensionName)) {
					 checkBoxesMap.get(extensionName).setSelected(true);
				}
		  }
	 }

	 private void addToDependencyMapFromXML (Map<String, List<String>> dependencies, Element eElement, String platform) {
		  if (eElement.getElementsByTagName(platform).item(0) != null) {
				Element project = (Element)eElement.getElementsByTagName(platform).item(0);

				ArrayList<String> deps = new ArrayList<String>();

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
								deps.add(dependencyNode.getTextContent());
						  }

					 }
				}

				dependencies.put(platform, deps);
		  }
	 }

	 class ExtensionRowCheckbox extends JCheckBox {

		  ExtensionRowCheckbox (String selectName) {
				super(selectName);
				setOpaque(false);
				setBackground(new Color(0, 0, 0));
				setForeground(new Color(255, 255, 255));
				setFocusPainted(false);
		  }

	 }
}
