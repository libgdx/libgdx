package com.badlogic.gdx.setup;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class DependencyBank {

	//Versions
	static String libgdxVersion = "1.3.1";
	//Temporary snapshot version, we need a more dynamic solution for pointing to the latest nightly
	static String libgdxNightlyVersion = "1.3.2-SNAPSHOT";
	static String roboVMVersion = "0.0.14";
	static String buildToolsVersion = "19.1.0";
	static String androidAPILevel = "19";
	static String gwtVersion = "2.6.0";

	//Repositories
	static String mavenCentral = "mavenCentral()";
	static String gwtPluginUrl = "https://github.com/steffenschaefer/gwt-gradle-plugin/raw/maven-repo/";
	static String libGDXSnapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/";
	static String libGDXReleaseUrl = "https://oss.sonatype.org/content/repositories/releases/";

	//Project plugins
	static String gwtPluginImport = "de.richsource.gradle.plugins:gwt-gradle-plugin:0.4";
	static String androidPluginImport = "com.android.tools.build:gradle:0.10+";
	static String roboVMPluginImport = "com.github.jtakakura:gradle-robovm-plugin:0.0.10";

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
		gwtInheritances.put(ProjectDependency.BOX2D, new String[]{"com.badlogic.gdx.physics.box2d.box2d-gwt"});
		gwtInheritances.put(ProjectDependency.BOX2DLIGHTS, new String[]{"Box2DLights"});
		gwtInheritances.put(ProjectDependency.ASHLEY, new String[]{"com.badlogic.ashley_gwt"});
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
			new String[]{"com.badlogicgames.gdx:gdx:$gdxVersion"},
			new String[]{"com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"},
			new String[]{"com.badlogicgames.gdx:gdx-backend-android:$gdxVersion", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86"},
			new String[]{"org.robovm:robovm-rt:${roboVMVersion}", "org.robovm:robovm-cocoatouch:${roboVMVersion}", "com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios"},
			new String[]{"com.badlogicgames.gdx:gdx-backend-gwt:$gdxVersion", "com.badlogicgames.gdx:gdx:$gdxVersion:sources", "com.badlogicgames.gdx:gdx-backend-gwt:$gdxVersion:sources"}
		),
		BULLET(
			new String[]{"com.badlogicgames.gdx:gdx-bullet:$gdxVersion"},
			new String[]{"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-desktop"},
			new String[]{"com.badlogicgames.gdx:gdx-bullet:$gdxVersion", "com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-armeabi", "com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-armeabi-v7a", "com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86"},
			new String[]{"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-ios"},
			null
		),
		FREETYPE(
			new String[]{"com.badlogicgames.gdx:gdx-freetype:$gdxVersion"},
			new String[]{"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"},
			new String[]{"com.badlogicgames.gdx:gdx-freetype:$gdxVersion", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86"},
			new String[]{"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-ios"},
			null
		),
		TOOLS(
			new String[]{},
			new String[]{"com.badlogicgames.gdx:gdx-tools:$gdxVersion"},
			new String[]{},
			new String[]{},
			new String[]{}
		),
		CONTROLLERS(
			new String[]{"com.badlogicgames.gdx:gdx-controllers:$gdxVersion"},
			new String[]{"com.badlogicgames.gdx:gdx-controllers-desktop:$gdxVersion", "com.badlogicgames.gdx:gdx-controllers-platform:$gdxVersion:natives-desktop"},
			new String[]{"com.badlogicgames.gdx:gdx-controllers:$gdxVersion", "com.badlogicgames.gdx:gdx-controllers-android:$gdxVersion"},
			new String[] {}, // works on iOS but never reports any controllers :)
			new String[]{"com.badlogicgames.gdx:gdx-controllers:$gdxVersion:sources", "com.badlogicgames.gdx:gdx-controllers-gwt:$gdxVersion", "com.badlogicgames.gdx:gdx-controllers-gwt:$gdxVersion:sources"}
		),
		BOX2D(
			new String[]{"com.badlogicgames.gdx:gdx-box2d:$gdxVersion"},
			new String[]{"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop"},
			new String[]{"com.badlogicgames.gdx:gdx-box2d:$gdxVersion", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a", "com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86"},
			new String[]{"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-ios"},
			new String[]{"com.badlogicgames.gdx:gdx-box2d:$gdxVersion:sources", "com.badlogicgames.gdx:gdx-box2d-gwt:$gdxVersion:sources"}
		),	
		BOX2DLIGHTS(
			new String[]{"com.badlogicgames.box2dlights:box2dlights:1.2"},
			new String[]{},
			new String[]{"com.badlogicgames.box2dlights:box2dlights:1.2"},
			new String[]{},
			new String[]{"com.badlogicgames.box2dlights:box2dlights:1.2:sources"}
		),
		ASHLEY(
			new String[]{"com.badlogicgames.ashley:ashley:1.0.1"},
			new String[]{},
			new String[]{"com.badlogicgames.ashley:ashley:1.0.1"},
			new String[]{},
			new String[]{"com.badlogicgames.ashley:ashley:1.0.1:sources"}
		);

		private String[] coreDependencies;
		private String[] desktopDependencies;
		private String[] androidDependencies;
		private String[] iosDependencies;
		private String[] gwtDependencies;

		ProjectDependency(String[] coreDeps, String[] desktopDeps, String[] androidDeps, String[] iosDeps, String[] gwtDeps) {
			this.coreDependencies = coreDeps;
			this.desktopDependencies = desktopDeps;
			this.androidDependencies = androidDeps;
			this.iosDependencies = iosDeps;
			this.gwtDependencies = gwtDeps;
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
