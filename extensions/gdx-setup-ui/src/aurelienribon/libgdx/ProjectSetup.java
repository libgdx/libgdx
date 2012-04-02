package aurelienribon.libgdx;

import aurelienribon.utils.TemplateManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

		templateManager.define("PROJECT_NAME", cfg.getProjectName());
		templateManager.define("MAINCLASS_NAME", cfg.getMainClassName());
		templateManager.define("PACKAGE_NAME", cfg.getPackageName());
		templateManager.define("PACKAGE_NAME_AS_PATH", cfg.getPackageName().replace('.', '/'));

		templateManager.define("PRJ_COMMON_NAME", cfg.getCommonPrjName());
		if (cfg.isDesktopIncluded) templateManager.define("PRJ_DESKTOP_NAME", cfg.getDesktopPrjName());
		if (cfg.isAndroidIncluded) templateManager.define("PRJ_ANDROID_NAME", cfg.getAndroidPrjName());
		if (cfg.isHtmlIncluded) templateManager.define("PRJ_HTML_NAME", cfg.getHtmlPrjName());

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
		postProcessInflate();
	}

	public void inflateLibraries() throws IOException {
		File commonPrjLibsDir = new File(FilenameUtils.normalize(tmpDst.getPath() + "/" + cfg.getCommonPrjName() + "/libs"));
		File desktopPrjLibsDir = new File(FilenameUtils.normalize(tmpDst.getPath() + "/" + cfg.getDesktopPrjName() + "/libs"));
		File androidPrjLibsDir = new File(FilenameUtils.normalize(tmpDst.getPath() + "/" + cfg.getAndroidPrjName() + "/libs"));
		File htmlPrjLibsDir = new File(FilenameUtils.normalize(tmpDst.getPath() + "/" + cfg.getHtmlPrjName() + "/war/WEB-INF/lib"));

		for (String libraryName : cfg.getLibraryNames()) {
			String libraryPath = cfg.getLibraryPath(libraryName);
			LibraryDef libraryDef = cfg.getLibraryDef(libraryName);

			InputStream is = new FileInputStream(libraryPath);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;

			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) continue;

				String name = entry.getName();

				for (String libElem : libraryDef.libsCommon)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, commonPrjLibsDir);
				for (String libElem : libraryDef.libsDesktop)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, desktopPrjLibsDir);
				for (String libElem : libraryDef.libsAndroid)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, androidPrjLibsDir);
				for (String libElem : libraryDef.libsHtml)
					if (name.endsWith(libElem)) copyEntry(zis, libElem, htmlPrjLibsDir);
			}

			zis.close();
		}
	}

	public void copy() throws IOException {
		File src = new File(tmpDst, cfg.getCommonPrjName());
		File dst = new File(cfg.getDestinationPath());
		FileUtils.copyDirectoryToDirectory(src, dst);

		if (cfg.isDesktopIncluded) {
			src = new File(tmpDst, cfg.getDesktopPrjName());
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			src = new File(tmpDst, cfg.getAndroidPrjName());
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			src = new File(tmpDst, cfg.getHtmlPrjName());
			FileUtils.copyDirectoryToDirectory(src, dst);
		}
	}

	public void clean() throws IOException {
		FileUtils.deleteDirectory(tmpDst);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private void postProcessInflate() throws IOException {
		File src = new File(tmpDst, "prj-common");
		File dst = new File(tmpDst, cfg.getCommonPrjName());
		move(src, "src/MyGame.java", "src/" + cfg.getPackageName().replace('.', '/') + "/" + cfg.getMainClassName() + ".java");
		move(src, "src/MyGame.gwt.xml", "src/" + cfg.getMainClassName() + ".gwt.xml");
		templateDir(src);
		FileUtils.moveDirectory(src, dst);

		if (cfg.isDesktopIncluded) {
			src = new File(tmpDst, "prj-desktop");
			dst = new File(tmpDst, cfg.getDesktopPrjName());
			move(src, "src/Main.java", "src/" + cfg.getPackageName().replace('.', '/') + "/Main.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			src = new File(tmpDst, "prj-android");
			dst = new File(tmpDst, cfg.getAndroidPrjName());
			move(src, "src/MainActivity.java", "src/" + cfg.getPackageName().replace('.', '/') + "/MainActivity.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			src = new File(tmpDst, "prj-html");
			dst = new File(tmpDst, cfg.getHtmlPrjName());
			move(src, "src/MyGame.gwt.xml", "src/" + cfg.getPackageName().replace('.', '/') + "/" + cfg.getMainClassName() + ".gwt.xml");
			move(src, "src/client", "src/" + cfg.getPackageName().replace('.', '/') + "/client");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}
	}

	private void templateDir(File dir) throws IOException {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				templateDir(file);
			} else {
				if (file.getName().endsWith(".jar")) continue;
				if (file.getName().endsWith(".zip")) continue;
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
		File file1 = new File(base, FilenameUtils.normalize(path1));
		File file2 = new File(base, FilenameUtils.normalize(path2));
		if (file1.isDirectory()) FileUtils.moveDirectory(file1, file2);
		else FileUtils.moveFile(file1, file2);
	}
}
