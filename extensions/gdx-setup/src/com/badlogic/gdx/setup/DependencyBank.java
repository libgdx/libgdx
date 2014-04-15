package com.badlogic.gdx.setup;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class DependencyBank {

	//Versions
	static String libgdxVersion = "1.0-SNAPSHOT";
	static String roboVMVersion = "0.0.11";

	static String packageString = "com.badlogicgames.gdx";

	//Repositories
	static String gwtPluginUrl = "https://github.com/steffenschaefer/gwt-gradle-plugin/raw/maven-repo/";
	static String libGDXSnapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/";

	//Project plugins
	static String gwtPluginImport = "de.richsource.gradle.plugins:gwt-gradle-plugin:0.3";
	static String androidPluginImport = "com.android.tools.build:gradle:0.9";
	static String roboVMPluginImport = "com.github.jtakakura:gradle-robovm-plugin:0.0.5";

	HashMap<ProjectDependency, Dependency> gdxDependencies = new HashMap<ProjectDependency, Dependency>();
	LinkedHashMap<ProjectDependency, String[]> gwtInheritances = new LinkedHashMap<ProjectDependency, String[]>();

	public DependencyBank() {
		for (ProjectDependency projectDep : ProjectDependency.values()) {
			Dependency dependency = new Dependency(projectDep.name(),
					projectDep.getDependencies(ProjectType.CORE),
					projectDep.getDependencies(ProjectType.DESKTOP),
					projectDep.getDependencies(ProjectType.ANDROID),
					projectDep.getDependencies(ProjectType.IOS),
					projectDep.getDependencies(ProjectType.HTML));
			gdxDependencies.put(projectDep, dependency);
		}
		gwtInheritances.put(ProjectDependency.GDX, new String[]{"com.badlogic.gdx.backends.gdx_backends_gwt"});
		gwtInheritances.put(ProjectDependency.CONTROLLERS, new String[]{"com.badlogic.gdx.controllers.controllers-gwt"});
	}

	public Dependency getDependency(ProjectDependency gdx) {
		return gdxDependencies.get(gdx);
	}


	/**
	 * This enum will hold all dependencies available for libgdx, allowing the setup to pick the ones needed by default,
	 * and allow the option to choose extensions as the user wishes.
	 * <p/>
	 * These depedency strings can be later used in a simple gradle plugin to manipulate the users project either after/before
	 * project generation
	 *
	 * @see Dependency for the object that handles sub-module dependencies. If no dependency is found for a sub-module, ie
	 * FreeTypeFont for gwt, an exception is thrown so the user can be notified of incompatability
	 */
	public enum ProjectDependency {
		GDX(
			new String[]{"%PACKAGE%:gdx:$gdxVersion"},
			new String[]{"%PACKAGE%:gdx-backend-lwjgl:$gdxVersion", "%PACKAGE%:gdx-platform:$gdxVersion:natives-desktop"},
			new String[]{"%PACKAGE%:gdx-backend-android:$gdxVersion", "%PACKAGE%:gdx-platform:$gdxVersion:natives-x86", "%PACKAGE%:gdx-platform:$gdxVersion:natives-armeabi", "%PACKAGE%:gdx-platform:$gdxVersion:natives-armeabi-v7a"},
			new String[]{"org.robovm:robovm-rt:${roboVMVersion}", "org.robovm:robovm-cocoatouch:${roboVMVersion}", "%PACKAGE%:gdx-backend-robovm:$gdxVersion", "%PACKAGE%:gdx-platform:$gdxVersion:natives-ios"},
			new String[]{"%PACKAGE%:gdx-backend-gwt:$gdxVersion", "%PACKAGE%:gdx:$gdxVersion:sources", "%PACKAGE%:gdx-backend-gwt:$gdxVersion:sources"}
		),
		BULLET(
			new String[]{"%PACKAGE%:gdx-bullet:$gdxVersion"},
			new String[]{"%PACKAGE%:gdx-bullet-platform:$gdxVersion:natives-desktop"},
			new String[]{"%PACKAGE%:gdx-bullet:$gdxVersion", "%PACKAGE%:gdx-bullet-platform:$gdxVersion:natives-armeabi", "%PACKAGE%:gdx-bullet-platform:$gdxVersion:natives-armeabi-v7a"},
			new String[]{"%PACKAGE%:gdx-bullet:$gdxVersion", "%PACKAGE%:gdx-bullet-platform:$gdxVersion:natives-ios"},
			new String[]{null}
		),
		FREETYPE(
			new String[]{"%PACKAGE%:gdx-freetype:$gdxVersion"},
			new String[]{"%PACKAGE%:gdx-freetype-platform:$gdxVersion:natives-desktop"},
			new String[]{"%PACKAGE%:gdx-freetype:$gdxVersion", "%PACKAGE%:gdx-freetype-platform:$gdxVersion:natives-armeabi", "%PACKAGE%:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a"},
			new String[]{"%PACKAGE%:gdx-freetype:$gdxVersion", "%PACKAGE%:gdx-freetype-platform:$gdxVersion:natives-ios"},
			new String[]{null}
		),
		TOOLS(
			new String[]{"%PACKAGE%:gdx-tools:$gdxVersion"},
			new String[]{"%PACKAGE%:gdx-tools:$gdxVersion"},
			new String[]{null},
			new String[]{null},
			new String[]{null}
		),
		CONTROLLERS(
			new String[]{"%PACKAGE%:gdx-controllers:$gdxVersion"},
			new String[]{"%PACKAGE%:gdx-controllers-desktop:$gdxVersion", "%PACKAGE%:gdx-controllers-platform:$gdxVersion:natives-desktop"},
			new String[]{"%PACKAGE%:gdx-controllers-android:$gdxVersion"},
			new String[]{null},
			new String[]{"%PACKAGE%:gdx-controllers:$gdxVersion:sources", "%PACKAGE%:gdx-controllers-gwt:$gdxVersion", "%PACKAGE%:gdx-controllers-gwt:$gdxVersion:sources"}
		);

		private String[] coreDependencies;
		private String[] desktopDependencies;
		private String[] androidDependencies;
		private String[] iosDependencies;
		private String[] gwtDependencies;

		ProjectDependency(String[] coreDeps, String[] desktopDeps, String[] androidDeps, String[] iosDeps, String[] gwtDeps) {
			this.coreDependencies = regex(coreDeps);
			this.desktopDependencies = regex(desktopDeps);
			this.androidDependencies = regex(androidDeps);
			this.iosDependencies = regex(iosDeps);
			this.gwtDependencies = regex(gwtDeps);
		}

		public String[] getDependencies(ProjectType type) {
			switch (type) {
				case CORE:
					return coreDependencies;
				case DESKTOP:
					return desktopDependencies;
				case ANDROID:
					return androidDependencies;
				case IOS:
					return iosDependencies;
				case HTML:
					return gwtDependencies;
			}
			return null;
		}

		private String[] regex(String[] in) {
			String[] out;
			if (in != null) {
				out = new String[in.length];
				for (int i = 0; i < in.length; i++) {
					if (in[i] != null) {
						out[i] = in[i].replace("%PACKAGE%", packageString);
					}
				}
				return out;
			}
			return null;
		}

	}


	public enum ProjectType {
		CORE("core", new String[]{"java"}),
		DESKTOP("desktop", new String[]{"java"}),
		ANDROID("android", new String[]{"android"}),
		IOS("ios", new String[]{"java", "robovm"}),
		HTML("html", new String[]{"gwt", "war"});

		private final String name;
		private final String[] plugins;

		ProjectType(String name, String plugins[]) {
			this.name = name;
			this.plugins = plugins;
		}

		public String getName() {
			return name;
		}

		public String[] getPlugins() {
			return plugins;
		}
	}

}
