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
package aurelienribon.gdxsetupui.ui.panels;

import aurelienribon.gdxsetupui.LibraryDef;
import aurelienribon.gdxsetupui.ui.Ctx;
import aurelienribon.ui.css.Style;
import aurelienribon.utils.Res;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class PreviewPanel extends javax.swing.JPanel {
    public PreviewPanel() {
        initComponents();
		Style.registerCssClasses(headerPanel, ".header");
		Style.registerCssClasses(numberLabel, ".headerNumber");

		JTree tree = new ResultTree();
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		add(scrollPane, BorderLayout.CENTER);
    }

	// -------------------------------------------------------------------------
	// Tree
	// -------------------------------------------------------------------------

	public class ResultTree extends JTree {
		private final Map<String, DefaultMutableTreeNode> nodes = new TreeMap<String, DefaultMutableTreeNode>();
		private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

		public ResultTree() {
			setBorder(new EmptyBorder(5, 5, 5, 5));
			setRootVisible(false);
			setShowsRootHandles(true);
			setCellRenderer(treeCellRenderer);
			setOpaque(false);

			Ctx.listeners.add(new Ctx.Listener() {
				@Override public void cfgSetupChanged() {
					update();
				}
			});

			build();
			update();
		}

		private void build() {
			try {
				ZipInputStream zis = new ZipInputStream(Res.getStream("projects.zip"));
				ZipEntry entry;

				while ((entry = zis.getNextEntry()) != null) {
					String name = entry.getName();
					name = entry.isDirectory() ? "#DIR#" + name : name; // this makes name sorting easier :p
					name = entry.isDirectory() ? name.substring(0, name.length()-1) : name;

					DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
					nodes.put(name, node);
				}

				zis.close();

				for (String name : nodes.keySet()) {
					String pName = name.startsWith("#DIR#") ? name : "#DIR#" + name;
					pName = FilenameUtils.getPath(pName);
					pName = pName.endsWith("/") ? pName.substring(0, pName.length()-1) : pName;

					DefaultMutableTreeNode node = nodes.get(name);
					DefaultMutableTreeNode pNode = nodes.get(pName);

					if (pNode != null) pNode.add(node);
					else rootNode.add(node);
				}

			} catch (IOException ex) {
				assert false;
			}
		}

		private void update() {
			DefaultMutableTreeNode commonPrjNode = nodes.get("#DIR#prj-common");
			DefaultMutableTreeNode desktopPrjNode = nodes.get("#DIR#prj-desktop");
			DefaultMutableTreeNode androidPrjNode = nodes.get("#DIR#prj-android");
			DefaultMutableTreeNode htmlPrjNode = nodes.get("#DIR#prj-html");
			DefaultMutableTreeNode robovmPrjNode = nodes.get("#DIR#prj-robovm");

			rootNode.removeAllChildren();
			rootNode.add(commonPrjNode);
			if (Ctx.cfgSetup.isDesktopIncluded) rootNode.add(desktopPrjNode);
			if (Ctx.cfgSetup.isAndroidIncluded) rootNode.add(androidPrjNode);
			if (Ctx.cfgSetup.isHtmlIncluded) rootNode.add(htmlPrjNode);
         if (Ctx.cfgSetup.isIosIncluded) {
         	rootNode.add(robovmPrjNode);
         }

			updateSrc();
			updateLibs();

			setModel(new DefaultTreeModel(rootNode));
		}

		private void updateSrc() {
			DefaultMutableTreeNode previousNode;

			// common

			DefaultMutableTreeNode commonSrcNode = nodes.get("#DIR#prj-common/src");
			DefaultMutableTreeNode commonSrcAppNode = nodes.get("prj-common/src/MyGame.java");
			DefaultMutableTreeNode commonSrcAppGwtNode = nodes.get("prj-common/src/MyGame.gwt.xml");

			commonSrcNode.removeAllChildren();
			previousNode = commonSrcNode;

			if (!Ctx.cfgSetup.packageName.trim().equals("")) {
				String[] paths = Ctx.cfgSetup.packageName.split("\\.");
				for (String path : paths) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode("#DIR#prj-common/src/" + path);
					previousNode.add(node);
					previousNode = node;
				}
				previousNode.add(commonSrcAppNode);
				commonSrcNode.add(commonSrcAppGwtNode);
			} else {
				commonSrcNode.add(commonSrcAppNode);
				commonSrcNode.add(commonSrcAppGwtNode);
			}

			// desktop

			DefaultMutableTreeNode desktopSrcNode = nodes.get("#DIR#prj-desktop/src");
			DefaultMutableTreeNode desktopSrcMainNode = nodes.get("prj-desktop/src/Main.java");

			desktopSrcNode.removeAllChildren();
			previousNode = desktopSrcNode;

			if (!Ctx.cfgSetup.packageName.trim().equals("")) {
				String[] paths = Ctx.cfgSetup.packageName.split("\\.");
				for (String path : paths) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode("#DIR#prj-desktop/src/" + path);
					previousNode.add(node);
					previousNode = node;
				}
				previousNode.add(desktopSrcMainNode);
			} else {
				desktopSrcNode.add(desktopSrcMainNode);
			}

			// android

			DefaultMutableTreeNode androidSrcNode = nodes.get("#DIR#prj-android/src");
			DefaultMutableTreeNode androidSrcActivityNode = nodes.get("prj-android/src/MainActivity.java");

			androidSrcNode.removeAllChildren();
			previousNode = androidSrcNode;

			if (!Ctx.cfgSetup.packageName.trim().equals("")) {
				String[] paths = Ctx.cfgSetup.packageName.split("\\.");
				for (String path : paths) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode("#DIR#prj-android/src/" + path);
					previousNode.add(node);
					previousNode = node;
				}
				previousNode.add(androidSrcActivityNode);
			} else {
				androidSrcNode.add(androidSrcActivityNode);
			}

			// html

			DefaultMutableTreeNode htmlSrcNode = nodes.get("#DIR#prj-html/src");
			DefaultMutableTreeNode htmlSrcAppGwtNode = nodes.get("prj-html/src/GwtDefinition.gwt.xml");
			DefaultMutableTreeNode htmlSrcClientDirNode = nodes.get("#DIR#prj-html/src/client");

			htmlSrcNode.removeAllChildren();
			previousNode = htmlSrcNode;

			if (!Ctx.cfgSetup.packageName.trim().equals("")) {
				String[] paths = Ctx.cfgSetup.packageName.split("\\.");
				for (String path : paths) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode("#DIR#prj-html/src/" + path);
					previousNode.add(node);
					previousNode = node;
				}
				previousNode.add(htmlSrcClientDirNode);
				previousNode.add(htmlSrcAppGwtNode);
			} else {
				commonSrcNode.add(htmlSrcClientDirNode);
				commonSrcNode.add(htmlSrcAppGwtNode);
			}
		}

		private void updateLibs() {
			DefaultMutableTreeNode commonLibsNode = nodes.get("#DIR#prj-common/libs");
			DefaultMutableTreeNode desktopLibsNode = nodes.get("#DIR#prj-desktop/libs");
			DefaultMutableTreeNode androidLibsNode = nodes.get("#DIR#prj-android/libs");
			DefaultMutableTreeNode htmlLibsNode = nodes.get("#DIR#prj-html/war/WEB-INF/lib");
         DefaultMutableTreeNode robovmLibsNode = nodes.get("#DIR#prj-robovm/libs");
			DefaultMutableTreeNode dataNode = nodes.get("#DIR#prj-android/assets");

			commonLibsNode.removeAllChildren();
			desktopLibsNode.removeAllChildren();
			androidLibsNode.removeAllChildren();
			htmlLibsNode.removeAllChildren();
			robovmLibsNode.removeAllChildren();
			dataNode.removeAllChildren();

			for (String libraryName : Ctx.cfgSetup.libraries) {
				LibraryDef def = Ctx.libs.getDef(libraryName);
				for (String path : def.libsCommon) pathToNodes(path, commonLibsNode);
				for (String path : def.libsDesktop) pathToNodes(path, desktopLibsNode);
				for (String path : def.libsAndroid) pathToNodes(path, androidLibsNode);
				for (String path : def.libsHtml) pathToNodes(path, htmlLibsNode);
				for (String path : def.libsRobovm) pathToNodes(path, robovmLibsNode);
				for (String path : def.data) pathToNodes(path, dataNode);                              
			}
		}

		private void pathToNodes(String path, DefaultMutableTreeNode parentNode) {
			String parentPath = (String) parentNode.getUserObject();
			String[] names = path.split("/");

			for (int i=0; i<names.length; i++) {
				if (i == 0) names[i] = parentPath + "/" + names[i];
				else names[i] = names[i-1] + "/" + names[i];

				if (i == names.length-1) names[i] = names[i].replaceFirst("#DIR#", "");

				DefaultMutableTreeNode node = nodes.get(names[i]);
				if (node == null) {
					node = new DefaultMutableTreeNode(names[i]);
					nodes.put(names[i], node);
					if (i == 0) parentNode.add(node);
					else nodes.get(names[i-1]).add(node);
				} else if (i == 0) {
					parentNode.add(node);
				}
			}
		}

		private final TreeCellRenderer treeCellRenderer = new TreeCellRenderer() {
			private final JLabel label = new JLabel();

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof String) {
					String name = (String) node.getUserObject();
					boolean isDir = name.startsWith("#DIR#");

					name = name.replaceFirst("#DIR#", "");
					name = name.replace("MyGame", Ctx.cfgSetup.mainClassName);

					if (isDir && name.equals("prj-common")) name = Ctx.cfgSetup.projectName + Ctx.cfgSetup.suffixCommon;
					if (isDir && name.equals("prj-desktop")) name = Ctx.cfgSetup.projectName + Ctx.cfgSetup.suffixDesktop;
					if (isDir && name.equals("prj-android")) name = Ctx.cfgSetup.projectName + Ctx.cfgSetup.suffixAndroid;
					if (isDir && name.equals("prj-html")) name = Ctx.cfgSetup.projectName + Ctx.cfgSetup.suffixHtml;
					if (isDir && name.equals("prj-robovm")) name = Ctx.cfgSetup.projectName + Ctx.cfgSetup.suffixRobovm;

					label.setText(FilenameUtils.getName(name));
					label.setIcon(isDir ? Res.getImage("gfx/ic_folder.png") : Res.getImage("gfx/ic_file.png"));
				}

				return label;
			}
		};
	}

	// -------------------------------------------------------------------------
	// Generated stuff
	// -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        numberLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jLabel4.setText("<html> Virtual view of the file tree that will be generated.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        numberLabel.setText("3");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addComponent(numberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4)
            .addComponent(numberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(headerPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel numberLabel;
    // End of variables declaration//GEN-END:variables

}