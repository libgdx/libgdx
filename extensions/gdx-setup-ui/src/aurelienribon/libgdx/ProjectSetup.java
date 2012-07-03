package aurelienribon.libgdx;

import aurelienribon.utils.TemplateManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import res.Res;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectSetup {
	private final ProjectConfiguration cfg;
	private final File tmpDst = new File("__libgdx_setup_tmp");
	private final TemplateManager templateManager = new TemplateManager();

	public ProjectSetup(ProjectConfiguration cfg) {
		this.cfg = cfg;

		templateManager.define("PROJECT_NAME", cfg.projectName);
		templateManager.define("MAINCLASS_NAME", cfg.mainClassName);
		templateManager.define("PACKAGE_NAME", cfg.packageName);
		templateManager.define("PACKAGE_NAME_AS_PATH", cfg.packageName.replace('.', '/'));

		templateManager.define("PRJ_COMMON_NAME", cfg.projectName + cfg.commonSuffix);
		if (cfg.isDesktopIncluded) templateManager.define("PRJ_DESKTOP_NAME", cfg.projectName + cfg.desktopSuffix);
		if (cfg.isAndroidIncluded) templateManager.define("PRJ_ANDROID_NAME", cfg.projectName + cfg.androidSuffix);
		if (cfg.isHtmlIncluded) templateManager.define("PRJ_HTML_NAME", cfg.projectName + cfg.htmlSuffix);

		if (!cfg.androidMinSdkVersion.equals("")) templateManager.define("ANDROID_MIN_SDK", cfg.androidMinSdkVersion);
		if (!cfg.androidTargetSdkVersion.equals("")) templateManager.define("ANDROID_TARGET_SDK", cfg.androidTargetSdkVersion);
		if (!cfg.androidMaxSdkVersion.equals("")) templateManager.define("ANDROID_MAX_SDK", cfg.androidMaxSdkVersion);
		if (!cfg.androidMinSdkVersion.equals("") || !cfg.androidTargetSdkVersion.equals("") || !cfg.androidMaxSdkVersion.equals("")) templateManager.define("ANDROID_USES_SDK");
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public void inflateProjects() throws IOException {
		FileUtils.forceMkdir(tmpDst);
		FileUtils.cleanDirectory(tmpDst);

		InputStream is = Res.getStream("projects.zip");
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;

		while ((entry = zis.getNextEntry()) != null) {
			File file = new File(tmpDst, entry.getName());
			if (entry.isDirectory()) {
				FileUtils.forceMkdir(file);
			} else {
				OutputStream os = new FileOutputStream(file);
				IOUtils.copy(zis, os);
				os.close();
			}
		}

		zis.close();
	}

	public void inflateLibraries() throws IOException {
		File commonPrjLibsDir = new File(tmpDst, "/prj-common/libs");
		File desktopPrjLibsDir = new File(tmpDst, "/prj-desktop/libs");
		File androidPrjLibsDir = new File(tmpDst, "/prj-android/libs");
		File htmlPrjLibsDir = new File(tmpDst, "/prj-html/war/WEB-INF/lib");

		for (String library : cfg.libs.getNames()) {
			if (!cfg.libs.isUsed(library)) continue;

			InputStream is = new FileInputStream(cfg.libs.getPath(library));
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;

			LibraryDef def = cfg.libs.getDef(library);

			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) continue;

				String name = entry.getName();

				for (String libElem : def.libsCommon)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, commonPrjLibsDir);
				for (String libElem : def.libsDesktop)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, desktopPrjLibsDir);
				for (String libElem : def.libsAndroid)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, androidPrjLibsDir);
				for (String libElem : def.libsHtml)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, htmlPrjLibsDir);
			}

			zis.close();
		}
	}

	public void configureLibraries() throws IOException {
		String entriesCommon = "";
		String entriesDesktop = "";
		String entriesAndroid = "";
		String entriesHtml = "";
		String gwtInherits = "";

		for (String library : cfg.libs.getNames()) {
			if (!cfg.libs.isUsed(library)) continue;

			LibraryDef def = cfg.libs.getDef(library);

			for (String file : def.libsCommon) {
				String name = FilenameUtils.getBaseName(file);
				if (!file.endsWith(".jar")) continue;
				if (endsWidth(name, "-source", "-sources", "-src")) continue;

				String source = getSource(def.libsCommon, file);

				entriesCommon += "\t<classpathentry exported=\"true\" kind=\"lib\" path=\"libs/" + file + "\"";
				if (source != null) entriesCommon += " sourcepath=\"libs/" + source + "\"";
				entriesCommon += "/>\n";

				entriesAndroid += "\t<classpathentry exported=\"true\" kind=\"lib\" path=\"/@{PRJ_COMMON_NAME}/libs/" + file + "\"";
				if (source != null) entriesAndroid += " sourcepath=\"/@{PRJ_COMMON_NAME}/libs/" + source + "\"";
				entriesAndroid += "/>\n";

				entriesHtml += "\t<classpathentry kind=\"lib\" path=\"/@{PRJ_COMMON_NAME}/libs/" + file + "\"";
				if (source != null) entriesHtml += " sourcepath=\"/@{PRJ_COMMON_NAME}/libs/" + source + "\"";
				entriesHtml += "/>\n";

				if (source != null) {
					entriesHtml += "\t<classpathentry kind=\"lib\" path=\"/@{PRJ_COMMON_NAME}/libs/" + source + "\"/>\n";
				}
			}

			for (String file : def.libsDesktop) {
				String name = FilenameUtils.getBaseName(file);
				if (!file.endsWith(".jar")) continue;
				if (endsWidth(name, "-source", "-sources", "-src")) continue;

				String source = getSource(def.libsDesktop, file);

				entriesDesktop += "\t<classpathentry kind=\"lib\" path=\"libs/" + file + "\"";
				if (source != null) entriesDesktop += " sourcepath=\"libs/" + source + "\"";
				entriesDesktop += "/>\n";
			}

			for (String file : def.libsAndroid) {
				String name = FilenameUtils.getBaseName(file);
				if (!file.endsWith(".jar")) continue;
				if (endsWidth(name, "-source", "-sources", "-src")) continue;

				String source = getSource(def.libsAndroid, file);

				entriesAndroid += "\t<classpathentry exported=\"true\" kind=\"lib\" path=\"libs/" + file + "\"";
				if (source != null) entriesAndroid += " sourcepath=\"libs/" + source + "\"";
				entriesAndroid += "/>\n";
			}

			for (String file : def.libsHtml) {
				String name = FilenameUtils.getBaseName(file);
				if (!file.endsWith(".jar")) continue;
				if (endsWidth(name, "-source", "-sources", "-src")) continue;

				String source = getSource(def.libsHtml, file);

				entriesHtml += "\t<classpathentry kind=\"lib\" path=\"war/WEB-INF/lib/" + file + "\"";
				if (source != null) entriesHtml += " sourcepath=\"war/WEB-INF/lib/" + source + "\"";
				entriesHtml += "/>\n";

				if (source != null) {
					entriesHtml += "\t<classpathentry kind=\"lib\" path=\"war/WEB-INF/lib/" + source + "\"/>\n";
				}
			}

			if (def.gwtModuleName != null) {
				gwtInherits += "\t<inherits name='" + def.gwtModuleName + "' />\n";
			}
		}

		templateManager.define("CLASSPATHENTRIES_COMMON", entriesCommon.trim());
		templateManager.define("CLASSPATHENTRIES_DESKTOP", entriesDesktop.trim());
		templateManager.define("CLASSPATHENTRIES_ANDROID", entriesAndroid.trim());
		templateManager.define("CLASSPATHENTRIES_HTML", entriesHtml.trim());
		templateManager.define("GWT_INHERITS", gwtInherits.trim());
		templateManager.processOver(new File(tmpDst, "prj-common/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-desktop/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-android/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-html/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-html/src/GwtDefinition.gwt.xml"));
	}

	public void postProcess() throws IOException {
		{
			File src = new File(tmpDst, "prj-common");
			File dst = new File(tmpDst, cfg.projectName + cfg.commonSuffix);
			move(src, "src/MyGame.java", "src/" + cfg.packageName.replace('.', '/') + "/" + cfg.mainClassName + ".java");
			move(src, "src/MyGame.gwt.xml", "src/" + cfg.mainClassName + ".gwt.xml");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isDesktopIncluded) {
			File src = new File(tmpDst, "prj-desktop");
			File dst = new File(tmpDst, cfg.projectName + cfg.desktopSuffix);
			move(src, "src/Main.java", "src/" + cfg.packageName.replace('.', '/') + "/Main.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			File src = new File(tmpDst, "prj-android");
			File dst = new File(tmpDst, cfg.projectName + cfg.androidSuffix);
			move(src, "src/MainActivity.java", "src/" + cfg.packageName.replace('.', '/') + "/MainActivity.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			File src = new File(tmpDst, "prj-html");
			File dst = new File(tmpDst, cfg.projectName + cfg.htmlSuffix);
			move(src, "src/GwtDefinition.gwt.xml", "src/" + cfg.packageName.replace('.', '/') + "/GwtDefinition.gwt.xml");
			move(src, "src/client", "src/" + cfg.packageName.replace('.', '/') + "/client");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}
	}

	public void copy() throws IOException {
		File src = new File(tmpDst, cfg.projectName + cfg.commonSuffix);
		File dst = new File(cfg.destinationPath);
		FileUtils.copyDirectoryToDirectory(src, dst);

		if (cfg.isDesktopIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.desktopSuffix);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.androidSuffix);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.htmlSuffix);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}
	}

	public void clean() throws IOException {
		FileUtils.deleteDirectory(tmpDst);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void templateDir(File dir) throws IOException {
		if (dir.getName().equals("libs")) return;

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				templateDir(file);
			} else {
				if (endsWidth(file.getName(), ".jar", ".zip", ".png")) continue;
				templateManager.processOver(file);
			}
		}
	}

	private void copyEntry(ZipInputStream zis, String name, File dst) throws IOException {
		File file = new File(dst, name);
		file.getParentFile().mkdirs();

		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(zis, os);
		os.close();
	}

	private void move(File base, String path1, String path2) throws IOException {
		if (path1.equals(path2)) return;
		File file1 = new File(base, FilenameUtils.normalize(path1));
		File file2 = new File(base, FilenameUtils.normalize(path2));
		FileUtils.deleteQuietly(file2);
		if (file1.isDirectory()) FileUtils.moveDirectory(file1, file2);
		else FileUtils.moveFile(file1, file2);
	}

	private boolean endsWidth(String str, String... ends) {
		for (String end : ends) if (str.endsWith(end)) return true;
		return false;
	}

	private String getSource(List<String> files, String file) {
		String path = FilenameUtils.getFullPath(file);
		String name = FilenameUtils.getBaseName(file);
		String ext = FilenameUtils.getExtension(file);

		if (files.contains(path + name + "-source." + ext)) return path + name + "-source." + ext;
		if (files.contains(path + name + "-sources." + ext)) return path + name + "-sources." + ext;
		if (files.contains(path + name + "-src." + ext)) return path + name + "-src." + ext;
		return null;
	}
}
