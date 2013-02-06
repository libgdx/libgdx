package aurelienribon.gdxsetupui;

import aurelienribon.gdxsetupui.Helper.ClasspathEntry;
import aurelienribon.gdxsetupui.Helper.GwtModule;
import aurelienribon.utils.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Standalone class used to update an existing libgdx project.
 * Uses a ProjectConfiguration instance as parameter, and provides several
 * methods to update the sub-projects step-by-step.
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectUpdate {
	private final ProjectUpdateConfiguration cfg;
	private final LibraryManager libs;

	public ProjectUpdate(ProjectUpdateConfiguration cfg, LibraryManager libs) {
		this.cfg = cfg;
		this.libs = libs;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Selected libraries are inflated from their zip files, and put in the
	 * correct folders of the projects.
	 * @throws IOException
	 */
	public void inflateLibraries() throws IOException {
		File commonPrjLibsDir = new File(Helper.getCorePrjPath(cfg) + "libs");
		File desktopPrjLibsDir = new File(Helper.getDesktopPrjPath(cfg) + "libs");
		File androidPrjLibsDir = new File(Helper.getAndroidPrjPath(cfg) + "libs");
		File htmlPrjLibsDir = new File(Helper.getHtmlPrjPath(cfg) + "war/WEB-INF/lib");
		File dataDir = new File(Helper.getAndroidPrjPath(cfg) + "assets");

		for (String library : cfg.libraries) {
			InputStream is = new FileInputStream(cfg.librariesZipPaths.get(library));
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;

			LibraryDef def = libs.getDef(library);

			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) continue;
				String entryName = entry.getName();

				for (String elemName : def.libsCommon)
					if (entryName.endsWith(elemName)) copyEntry(zis, elemName, commonPrjLibsDir);

				if (cfg.isDesktopIncluded) {
					for (String elemName : def.libsDesktop)
						if (entryName.endsWith(elemName)) copyEntry(zis, elemName, desktopPrjLibsDir);
				}

				if (cfg.isAndroidIncluded) {
					for (String elemName : def.libsAndroid)
						if (entryName.endsWith(elemName)) copyEntry(zis, elemName, androidPrjLibsDir);
					for (String elemName : def.data)
						if (entryName.endsWith(elemName)) copyEntry(zis, elemName, dataDir);
				}

				if (cfg.isHtmlIncluded) {
					for (String elemName : def.libsHtml)
						if (entryName.endsWith(elemName)) copyEntry(zis, elemName, htmlPrjLibsDir);
				}
			}

			zis.close();
		}
	}

	/**
	 * Classpaths are configurated according to the selected libraries.
	 * @throws IOException
	 */
	public void editClasspaths() throws IOException, TransformerException {
		writeClasspath(new File(Helper.getCorePrjPath(cfg), ".classpath"), cfg.coreClasspath);

		if (cfg.isAndroidIncluded) {
			writeClasspath(new File(Helper.getAndroidPrjPath(cfg), ".classpath"), cfg.androidClasspath);
		}

		if (cfg.isDesktopIncluded) {
			writeClasspath(new File(Helper.getDesktopPrjPath(cfg), ".classpath"), cfg.desktopClasspath);
		}

		if (cfg.isHtmlIncluded) {
			File htmlDir = new File(Helper.getHtmlPrjPath(cfg));
			writeClasspath(new File(htmlDir, ".classpath"), cfg.htmlClasspath);
			for (File file : FileUtils.listFiles(htmlDir, new String[] {"gwt.xml"}, true)) {
				if (file.getName().equals("GwtDefinition.gwt.xml"))
					writeGwtDefinition(file, cfg.gwtModules);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void copyEntry(ZipInputStream zis, String name, File dst) throws IOException {
		File file = new File(dst, name);
		file.getParentFile().mkdirs();

		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(zis, os);
		os.close();
	}

	private void writeClasspath(File classpathFile, List<ClasspathEntry> classpath) {
		try {
			Document doc = XmlUtils.createParser().parse(classpathFile);
			Node root = (Node) XmlUtils.xpath("classpath", doc, XPathConstants.NODE);
			NodeList libsNodes = (NodeList) XmlUtils.xpath("classpath/classpathentry[@kind='lib' and @path]", doc, XPathConstants.NODESET);

			for (int i=0; i<libsNodes.getLength(); i++) {
				root.removeChild(libsNodes.item(i));
			}

			for (ClasspathEntry entry : classpath) {
				Element elem = doc.createElement("classpathentry");
				root.appendChild(elem);

				elem.setAttribute("kind", "lib");
				if (entry.exported) elem.setAttribute("exported", "true");
				elem.setAttribute("path", entry.path);
				if (entry.sourcepath != null) elem.setAttribute("sourcepath", entry.sourcepath);
			}

			XmlUtils.clean(doc);
			String str = XmlUtils.transform(doc);
			FileUtils.writeStringToFile(classpathFile, str);

		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void writeGwtDefinition(File gwtDefitionFile, List<GwtModule> modules) {
		try {
			Document doc = XmlUtils.createParser().parse(gwtDefitionFile);
			Node root = (Node) XmlUtils.xpath("module", doc, XPathConstants.NODE);
			NodeList nodes = (NodeList) XmlUtils.xpath("module/inherits", doc, XPathConstants.NODESET);

			for (int i=0; i<nodes.getLength(); i++) {
				root.removeChild(nodes.item(i));
			}

			for (GwtModule module : modules) {
				Element elem = doc.createElement("inherits");
				root.appendChild(elem);

				elem.setAttribute("name", module.name);
			}

			XmlUtils.clean(doc);
			String str = XmlUtils.transform(doc);
			FileUtils.writeStringToFile(gwtDefitionFile, str);

		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
	}
}
