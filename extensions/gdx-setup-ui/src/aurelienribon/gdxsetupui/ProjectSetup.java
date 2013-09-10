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
package aurelienribon.gdxsetupui;

import aurelienribon.utils.Res;
import aurelienribon.utils.TemplateManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Standalone class used to completely setup a libgdx project.
 * Uses a ProjectConfiguration instance as parameter, and provides several
 * methods to initialize the sub-projects step-by-step.
 * <p/>
 *
 * The raw projects are contained in a zip file. The first thing to do is
 * to inflate this file on the hard drive to get the files adn folders of the
 * projects. Then, selected libraries should be inflated too in libs folders
 * of the projects, and configurated. Post-process is required to update the
 * files of the projects, and then a call to copy() moves everything to the
 * right location.
 * <pre>
 * 1. inflateProjects
 * 2. inflateLibraries
 * 3. configureLibraries
 * 4. postProcess
 * 5. copy
 * 6. clean
 * </pre>
 *
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectSetup {
	private final ProjectSetupConfiguration cfg;
	private final LibraryManager libs;
	private final File tmpDst = new File("__libgdx_setup_tmp");
	private final TemplateManager templateManager = new TemplateManager();

	public ProjectSetup(ProjectSetupConfiguration cfg, LibraryManager libs) {
		this.cfg = cfg;
		this.libs = libs;

		// General definitions
		templateManager.define("PROJECT_NAME", cfg.projectName);
		templateManager.define("MAINCLASS_NAME", cfg.mainClassName);
		templateManager.define("PACKAGE_NAME", cfg.packageName);
		templateManager.define("PACKAGE_NAME_AS_PATH", cfg.packageName.replace('.', '/'));

		// Project specific definitions
		templateManager.define("PRJ_COMMON_NAME", cfg.projectName + cfg.suffixCommon);
		if (cfg.isDesktopIncluded) templateManager.define("PRJ_DESKTOP_NAME", cfg.projectName + cfg.suffixDesktop);
		if (cfg.isAndroidIncluded) templateManager.define("PRJ_ANDROID_NAME", cfg.projectName + cfg.suffixAndroid);
		if (cfg.isHtmlIncluded) templateManager.define("PRJ_HTML_NAME", cfg.projectName + cfg.suffixHtml);
		if (cfg.isIosIncluded) {
			templateManager.define("PRJ_IOS_NAME", cfg.projectName + cfg.suffixIos);
			templateManager.define("PRJ_ROBOVM_NAME", cfg.projectName + cfg.suffixRobovm);
		}

		// Android manifest definitions
		if (!cfg.androidMinSdkVersion.equals("")) templateManager.define("ANDROID_MIN_SDK", cfg.androidMinSdkVersion);
		if (!cfg.androidTargetSdkVersion.equals("")) templateManager.define("ANDROID_TARGET_SDK", cfg.androidTargetSdkVersion);
		if (!cfg.androidMaxSdkVersion.equals("")) templateManager.define("ANDROID_MAX_SDK", cfg.androidMaxSdkVersion);
		if (!cfg.androidMinSdkVersion.equals("") || !cfg.androidTargetSdkVersion.equals("") || !cfg.androidMaxSdkVersion.equals("")) templateManager.define("ANDROID_USES_SDK");
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * The raw structure of all projects is contained in a zip file. This
	 * method inflates this file in a temporary folder.
	 * @throws IOException
	 */
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

	/**
	 * Selected libraries are inflated from their zip files, and put in the
	 * libs folders of the projects.
	 * @throws IOException
	 */
	public void inflateLibraries() throws IOException {
		File commonPrjLibsDir = new File(tmpDst, "/prj-common/libs");
		File desktopPrjLibsDir = new File(tmpDst, "/prj-desktop/libs");
		File androidPrjLibsDir = new File(tmpDst, "/prj-android/libs");
		File htmlPrjLibsDir = new File(tmpDst, "/prj-html/war/WEB-INF/lib");
		File iosPrjLibsDir = new File(tmpDst, "/prj-ios/libs");
		File robovmPrjLibsDir = new File(tmpDst, "/prj-robovm/libs");
		File dataDir = new File(tmpDst, "/prj-android/assets");
		System.out.println("infalting libs");

		for (String library : cfg.libraries) {
			InputStream is = new FileInputStream(cfg.librariesZipPaths.get(library));
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;

			LibraryDef def = libs.getDef(library);

			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) continue;
				String entryName = entry.getName();
				byte[] bytes = IOUtils.toByteArray(zis);

				for (String elemName : def.libsCommon)
					if (entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, commonPrjLibsDir);
				for (String elemName : def.libsDesktop)
					if (entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, desktopPrjLibsDir);
				for (String elemName : def.libsAndroid)
					if (entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, androidPrjLibsDir);
				for (String elemName : def.libsHtml)
					if (entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, htmlPrjLibsDir);
				for (String elemName : def.libsIos)
					if(entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, iosPrjLibsDir);
				for (String elemName : def.libsRobovm)
					if(entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, robovmPrjLibsDir);
				for (String elemName : def.data)
					if (entryName.endsWith(elemName)) copyEntry(new ByteArrayInputStream(bytes), elemName, dataDir);
			}

			zis.close();
		}
	}

	/**
	 * Classpaths are configurated according to the selected libraries.
	 * @throws IOException
	 */
	public void configureLibraries() throws IOException {
		List<String> entriesCommon = new ArrayList<String>();
		List<String> entriesDesktop = new ArrayList<String>();
		List<String> entriesAndroid = new ArrayList<String>();
		List<String> entriesHtml = new ArrayList<String>();
		List<String> entriesRobovm = new ArrayList<String>();
		List<String> gwtInherits = new ArrayList<String>();

		for (String library : cfg.libraries) {
			LibraryDef def = libs.getDef(library);

			for (String file : def.libsCommon) {
				if (!isLibJar(file)) continue;
				String source = getSource(def.libsCommon, file);
				entriesCommon.add(buildClasspathEntry(file, source, "libs/", true));
				entriesAndroid.add(buildClasspathEntry(file, source, "/@{PRJ_COMMON_NAME}/libs/", true));
				entriesHtml.add(buildClasspathEntry(file, source, "/@{PRJ_COMMON_NAME}/libs/", false));
				if (source != null) entriesHtml.add(buildClasspathEntry(source, null, "/@{PRJ_COMMON_NAME}/libs/", false));
			}

			for (String file : def.libsDesktop) {
				if (!isLibJar(file)) continue;
				String source = getSource(def.libsDesktop, file);
				entriesDesktop.add(buildClasspathEntry(file, source, "libs/", false));
			}

			for (String file : def.libsAndroid) {
				if (!isLibJar(file)) continue;
				String source = getSource(def.libsAndroid, file);
				entriesAndroid.add(buildClasspathEntry(file, source, "libs/", true));
			}

			for (String file : def.libsHtml) {
				if (!isLibJar(file)) continue;
				String source = getSource(def.libsHtml, file);
				entriesHtml.add(buildClasspathEntry(file, source, "war/WEB-INF/lib/", false));
				if (source != null) entriesHtml.add(buildClasspathEntry(source, null, "war/WEB-INF/lib/", false));
			}
			
			for(String file: def.libsRobovm) {
				if(!isLibJar(file)) continue;
				String source = getSource(def.libsAndroid, file);
				entriesRobovm.add(buildClasspathEntry(file, source, "libs/", false));
			}

			if (def.gwtModuleName != null) {
				gwtInherits.add("<inherits name='" + def.gwtModuleName + "' />");
			}
		}

		templateManager.define("CLASSPATHENTRIES_COMMON", flatten(entriesCommon, "\t", "\n").trim());
		templateManager.define("CLASSPATHENTRIES_DESKTOP", flatten(entriesDesktop, "\t", "\n").trim());
		templateManager.define("CLASSPATHENTRIES_ANDROID", flatten(entriesAndroid, "\t", "\n").trim());
		templateManager.define("CLASSPATHENTRIES_HTML", flatten(entriesHtml, "\t", "\n").trim());
		templateManager.define("CLASSPATHENTRIES_ROBOVM", flatten(entriesRobovm, "\t", "\n").trim());
		templateManager.define("GWT_INHERITS", flatten(gwtInherits, "\t", "\n").trim());
		templateManager.processOver(new File(tmpDst, "prj-common/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-desktop/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-android/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-html/.classpath"));
		templateManager.processOver(new File(tmpDst, "prj-html/src/GwtDefinition.gwt.xml"));
	}

	/**
	 * Launchers and packages are set up according to the user choices.
	 * @throws IOException
	 */
	public void postProcess() throws IOException {
		{
			File src = new File(tmpDst, "prj-common");
			File dst = new File(tmpDst, cfg.projectName + cfg.suffixCommon);
			move(src, "src/MyGame.java", "src/" + cfg.packageName.replace('.', '/') + "/" + cfg.mainClassName + ".java");
			move(src, "src/MyGame.gwt.xml", "src/" + cfg.mainClassName + ".gwt.xml");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isDesktopIncluded) {
			File src = new File(tmpDst, "prj-desktop");
			File dst = new File(tmpDst, cfg.projectName + cfg.suffixDesktop);
			move(src, "src/Main.java", "src/" + cfg.packageName.replace('.', '/') + "/Main.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			File src = new File(tmpDst, "prj-android");
			File dst = new File(tmpDst, cfg.projectName + cfg.suffixAndroid);
			move(src, "src/MainActivity.java", "src/" + cfg.packageName.replace('.', '/') + "/MainActivity.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			File src = new File(tmpDst, "prj-html");
			File dst = new File(tmpDst, cfg.projectName + cfg.suffixHtml);
			move(src, "src/GwtDefinition.gwt.xml", "src/" + cfg.packageName.replace('.', '/') + "/GwtDefinition.gwt.xml");
			move(src, "src/client", "src/" + cfg.packageName.replace('.', '/') + "/client");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}
		
		if(cfg.isIosIncluded) {
			File src = new File(tmpDst, "prj-ios");
			File dst = new File(tmpDst, cfg.projectName + cfg.suffixIos);
			move(src, "my-gdx-game-ios.csproj", cfg.projectName + cfg.suffixIos + ".csproj");
			move(src, "my-gdx-game-ios.sln", cfg.projectName + cfg.suffixIos + ".sln");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
			
			src = new File(tmpDst, "prj-robovm");
			dst = new File(tmpDst, cfg.projectName + cfg.suffixRobovm);
			move(src, "src/RobovmLauncher.java", "src/" + cfg.packageName.replace('.', '/') + "/RobovmLauncher.java");
			templateDir(src);
			FileUtils.moveDirectory(src, dst);
		}
	}

	/**
	 * Everything is moved to the right location.
	 * @throws IOException
	 */
	public void copy() throws IOException {
		File src = new File(tmpDst, cfg.projectName + cfg.suffixCommon);
		File dst = new File(cfg.destinationPath);
		FileUtils.copyDirectoryToDirectory(src, dst);

		if (cfg.isDesktopIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.suffixDesktop);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isAndroidIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.suffixAndroid);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}

		if (cfg.isHtmlIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.suffixHtml);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}
		
		if(cfg.isIosIncluded) {
			src = new File(tmpDst, cfg.projectName + cfg.suffixIos);
			FileUtils.copyDirectoryToDirectory(src, dst);
			
			src = new File(tmpDst, cfg.projectName + cfg.suffixRobovm);
			FileUtils.copyDirectoryToDirectory(src, dst);
		}
	}

	/**
	 * Temporary folder is deleted.
	 */
	public void clean() {
		FileUtils.deleteQuietly(tmpDst);
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
				if (endsWith(file.getName(), ".jar", ".zip", ".dll", ".a", ".png")) continue;
				templateManager.processOver(file);
			}
		}
	}

	private void copyEntry(InputStream zis, String name, File dst) throws IOException {
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

	private boolean endsWith(String str, String... ends) {
		for (String end : ends) if (str.endsWith(end)) return true;
		return false;
	}

	private boolean isLibJar(String file) {
		if (!file.endsWith(".jar")) return false;
		String name = FilenameUtils.getBaseName(file);
		if (endsWith(name, "-source", "-sources", "-src")) return false;
		return true;
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

	private String buildClasspathEntry(String file, String sourceFile, String path, boolean exported) {
		String str = "<classpathentry kind=\"lib\" ";
		if (exported) str += "exported=\"true\" ";
		str += "path=\"" + path + file + "\"";
		if (sourceFile != null) str += " sourcepath=\"" + path + sourceFile + "\"";
		str += "/>";
		return str;
	}

	private String flatten(List<String> strs, String begin, String end) {
		String ret = "";
		for (String str : strs) ret += begin + str + end;
		return ret;
	}
}