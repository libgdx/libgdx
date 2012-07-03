package aurelienribon.libgdx.ui;

import aurelienribon.libgdx.LibraryDef;
import java.awt.Component;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import org.apache.commons.io.FilenameUtils;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
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
			@Override public void configChanged() {
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

		rootNode.removeAllChildren();
		rootNode.add(commonPrjNode);
		if (Ctx.cfg.isDesktopIncluded) rootNode.add(desktopPrjNode);
		if (Ctx.cfg.isAndroidIncluded) rootNode.add(androidPrjNode);
		if (Ctx.cfg.isHtmlIncluded) rootNode.add(htmlPrjNode);

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

		if (!Ctx.cfg.packageName.trim().equals("")) {
			String[] paths = Ctx.cfg.packageName.split("\\.");
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

		if (!Ctx.cfg.packageName.trim().equals("")) {
			String[] paths = Ctx.cfg.packageName.split("\\.");
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

		if (!Ctx.cfg.packageName.trim().equals("")) {
			String[] paths = Ctx.cfg.packageName.split("\\.");
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

		if (!Ctx.cfg.packageName.trim().equals("")) {
			String[] paths = Ctx.cfg.packageName.split("\\.");
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

		commonLibsNode.removeAllChildren();
		desktopLibsNode.removeAllChildren();
		androidLibsNode.removeAllChildren();
		htmlLibsNode.removeAllChildren();

		for (String libraryName : Ctx.cfg.libs.getNames()) {
			if (Ctx.cfg.libs.isUsed(libraryName)) {
				LibraryDef def = Ctx.cfg.libs.getDef(libraryName);
				for (String path : def.libsCommon) pathToNodes(path, commonLibsNode);
				for (String path : def.libsDesktop) pathToNodes(path, desktopLibsNode);
				for (String path : def.libsAndroid) pathToNodes(path, androidLibsNode);
				for (String path : def.libsHtml) pathToNodes(path, htmlLibsNode);
			}
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
				name = name.replace("MyGame", Ctx.cfg.mainClassName);

				if (isDir && name.equals("prj-common")) name = Ctx.cfg.projectName + Ctx.cfg.commonSuffix;
				if (isDir && name.equals("prj-desktop")) name = Ctx.cfg.projectName + Ctx.cfg.desktopSuffix;
				if (isDir && name.equals("prj-android")) name = Ctx.cfg.projectName + Ctx.cfg.androidSuffix;
				if (isDir && name.equals("prj-html")) name = Ctx.cfg.projectName + Ctx.cfg.htmlSuffix;

				label.setText(FilenameUtils.getName(name));
				label.setIcon(isDir ? Res.getImage("gfx/ic_folder.png") : Res.getImage("gfx/ic_file.png"));
			}

			return label;
		}
	};
}
