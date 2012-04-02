package aurelienribon.libgdx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class ProjectConfiguration {
	private final List<String> libraries = new ArrayList<String>();
	private final Map<String, LibraryDef> libraryDefs = new HashMap<String, LibraryDef>();
	private final Map<String, String> libraryPaths = new HashMap<String, String>();

	private String projectName = "my-gdx-game";
	private String mainClassName = "MyGdxGame";
	private String packageName = "com.me.mygdxgame";
	private String destinationPath = "";

	public boolean isDesktopIncluded = true;
	public boolean isAndroidIncluded = true;
	public boolean isHtmlIncluded = true;
	public String commonSuffix = "";
	public String desktopSuffix = "-desktop";
	public String androidSuffix = "-android";
	public String htmlSuffix = "-html";
	public String androidMinSdkVersion = "5";
	public String androidTargetSdkVersion = "15";
	public String androidMaxSdkVersion = "";

	// -------------------------------------------------------------------------

	public List<String> getLibraryNames() {
		return Collections.unmodifiableList(libraries);
	}

	public String getLibraryPath(String libraryName) {
		return libraryPaths.get(libraryName);
	}

	public LibraryDef getLibraryDef(String libraryName) {
		return libraryDefs.get(libraryName);
	}

	public void registerLibrary(String name, LibraryDef def) {
		libraries.add(name);
		libraryDefs.put(name, def);
	}

	public void setLibraryPath(String libraryName, String libraryPath) {
		libraryPaths.put(libraryName, libraryPath);
	}

	// -------------------------------------------------------------------------

	public String getProjectName() {
		return projectName;
	}

	public String getMainClassName() {
		return mainClassName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	// -------------------------------------------------------------------------

	public String getCommonPrjName() {
		return projectName + commonSuffix;
	}

	public String getDesktopPrjName() {
		return projectName + desktopSuffix;
	}

	public String getAndroidPrjName() {
		return projectName + androidSuffix;
	}

	public String getHtmlPrjName() {
		return projectName + htmlSuffix;
	}

	public boolean isValid() {
		if (projectName.trim().equals("")) return false;
		if (packageName.trim().equals("")) return false;
		if (packageName.endsWith(".")) return false;
		if (mainClassName.trim().equals("")) return false;

		for (String libraryName : libraries) {
			if (!isLibraryValid(libraryName)) return false;
		}

		return true;
	}

	public boolean isLibraryValid(String libraryName) {
		String path = getLibraryPath(libraryName);
		if (path == null) return false;
		if (!path.endsWith(".zip")) return false;
		if (!new File(path).isFile()) return false;
		return true;
	}

	public String getErrorMessage() {
		if (projectName.trim().equals("")) return "Project name is not set.";
		if (packageName.trim().equals("")) return "Package name is not set.";
		if (packageName.endsWith(".")) return "Package name ends with a dot.";
		if (mainClassName.trim().equals("")) return "Main class name is not set.";

		for (String libraryName : libraries) {
			if (!isLibraryValid(libraryName))
				return "At least one selected library has a missing or invalid archive.";
		}

		return "No error found";
	}
}
