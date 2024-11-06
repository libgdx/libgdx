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

import java.util.HashMap;

public class DependencyBank {

	// Versions
	static String libgdxVersion = "1.13.1";
	// Temporary snapshot version, we need a more dynamic solution for pointing to the latest nightly
	static String libgdxNightlyVersion = "1.13.1-SNAPSHOT";
	static String roboVMVersion = "2.3.22";
	static String buildToolsVersion = "33.0.2";
	static String androidAPILevel = "33";
	static String androidMinAPILevel = "19";
	static String gwtVersion = "2.11.0";

	// Repositories
	static String mavenLocal = "mavenLocal()";
	static String mavenCentral = "mavenCentral()";
	static String google = "google()";
	static String gradlePlugins = "gradlePluginPortal()";
	static String libGDXSnapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/";
	static String libGDXReleaseUrl = "https://oss.sonatype.org/content/repositories/releases/";
	static String jitpackUrl = "https://jitpack.io";

	// Project plugins
	static String gwtPluginImport = "org.docstr:gwt-gradle-plugin:1.1.29";
	static String grettyPluginImport = "org.gretty:gretty:3.1.0";
	static String androidPluginImport = "com.android.tools.build:gradle:8.1.2";
	static String roboVMPluginImport = "com.mobidevelop.robovm:robovm-gradle-plugin:" + roboVMVersion;

	// Extension versions
	static String box2DLightsVersion = "1.5";
	static String ashleyVersion = "1.7.4";
	static String aiVersion = "1.8.2";
	static String controllersVersion = "2.2.1";

	HashMap<ProjectDependency, Dependency> gdxDependencies = new HashMap<ProjectDependency, Dependency>();

	public DependencyBank () {
		for (ProjectDependency projectDep : ProjectDependency.values()) {
			Dependency dependency = new Dependency(projectDep.name(), projectDep.getGwtInherits(),
				projectDep.getDependencies(ProjectType.CORE), projectDep.getDependencies(ProjectType.LWJGL2),
				projectDep.getDependencies(ProjectType.LWJGL3), projectDep.getDependencies(ProjectType.ANDROID),
				projectDep.getDependencies(ProjectType.IOS), projectDep.getDependencies(ProjectType.HTML));
			gdxDependencies.put(projectDep, dependency);
		}
	}

	public Dependency getDependency (ProjectDependency gdx) {
		return gdxDependencies.get(gdx);
	}

	/** This enum will hold all dependencies available for libgdx, allowing the setup to pick the ones needed by default, and allow
	 * the option to choose extensions as the user wishes.
	 * <p/>
	 * These dependency strings can be later used in a simple gradle plugin to manipulate the users project either after/before
	 * project generation
	 *
	 * @see Dependency for the object that handles sub-module dependencies. If no dependency is found for a sub-module, ie
	 *      FreeTypeFont for gwt, an exception is thrown so the user can be notified of incompatability */
	public enum ProjectDependency {
		GDX(new String[] {"com.badlogicgames.gdx:gdx:$gdxVersion"},
			new String[] {"com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"},
			new String[] {"com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"},
			new String[] {"com.badlogicgames.gdx:gdx-backend-android:$gdxVersion",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64"},
			new String[] {"com.mobidevelop.robovm:robovm-rt:$roboVMVersion",
				"com.mobidevelop.robovm:robovm-cocoatouch:$roboVMVersion", "com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion",
				"com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios"},
			new String[] {"com.badlogicgames.gdx:gdx-backend-gwt:$gdxVersion", "com.badlogicgames.gdx:gdx:$gdxVersion:sources",
				"com.badlogicgames.gdx:gdx-backend-gwt:$gdxVersion:sources",
				"com.google.jsinterop:jsinterop-annotations:2.0.2:sources"},
			new String[] {"com.badlogic.gdx.backends.gdx_backends_gwt"},

			"Core Library for libGDX"), BULLET(new String[] {"com.badlogicgames.gdx:gdx-bullet:$gdxVersion"},
				new String[] {"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-desktop"},
				new String[] {"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-desktop"},
				new String[] {"com.badlogicgames.gdx:gdx-bullet:$gdxVersion",
					"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-armeabi-v7a",
					"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-arm64-v8a",
					"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86",
					"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86_64"},
				new String[] {"com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-ios"}, null, null,

				"3D Collision Detection and Rigid Body Dynamics"), FREETYPE(
					new String[] {"com.badlogicgames.gdx:gdx-freetype:$gdxVersion"},
					new String[] {"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"},
					new String[] {"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"},
					new String[] {"com.badlogicgames.gdx:gdx-freetype:$gdxVersion",
						"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a",
						"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a",
						"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86",
						"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64"},
					new String[] {"com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-ios"}, null, null,

					"Generate BitmapFonts from .ttf font files"), TOOLS(new String[] {},
						new String[] {"com.badlogicgames.gdx:gdx-tools:$gdxVersion"}, new String[] {}, new String[] {}, new String[] {},
						new String[] {}, new String[] {},

						"Collection of tools, including 3D particle editor, texture packers, and file processors"), CONTROLLERS(
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-core:$gdxControllersVersion"},
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-desktop:$gdxControllersVersion"},
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-desktop:$gdxControllersVersion"},
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-android:$gdxControllersVersion"},
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-ios:$gdxControllersVersion"},
							new String[] {"com.badlogicgames.gdx-controllers:gdx-controllers-core:$gdxControllersVersion:sources",
								"com.badlogicgames.gdx-controllers:gdx-controllers-gwt:$gdxControllersVersion",
								"com.badlogicgames.gdx-controllers:gdx-controllers-gwt:$gdxControllersVersion:sources"},
							new String[] {"com.badlogic.gdx.controllers", "com.badlogic.gdx.controllers.controllers-gwt"},

							"Game Controller/Gamepad API"), BOX2D(new String[] {"com.badlogicgames.gdx:gdx-box2d:$gdxVersion"},
								new String[] {"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop"},
								new String[] {"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop"},
								new String[] {"com.badlogicgames.gdx:gdx-box2d:$gdxVersion",
									"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-armeabi-v7a",
									"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-arm64-v8a",
									"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86",
									"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-x86_64"},
								new String[] {"com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-ios"},
								new String[] {"com.badlogicgames.gdx:gdx-box2d:$gdxVersion:sources",
									"com.badlogicgames.gdx:gdx-box2d-gwt:$gdxVersion:sources"},
								new String[] {"com.badlogic.gdx.physics.box2d.box2d-gwt"},

								"2D Physics Library"), BOX2DLIGHTS(
									new String[] {"com.badlogicgames.box2dlights:box2dlights:$box2DLightsVersion"}, new String[] {},
									new String[] {}, new String[] {"com.badlogicgames.box2dlights:box2dlights:$box2DLightsVersion"},
									new String[] {},
									new String[] {"com.badlogicgames.box2dlights:box2dlights:$box2DLightsVersion:sources"},
									new String[] {"Box2DLights"},

									"2D Lighting framework that utilises Box2D"), ASHLEY(
										new String[] {"com.badlogicgames.ashley:ashley:$ashleyVersion"}, new String[] {}, new String[] {},
										new String[] {"com.badlogicgames.ashley:ashley:$ashleyVersion"}, new String[] {},
										new String[] {"com.badlogicgames.ashley:ashley:$ashleyVersion:sources"},
										new String[] {"com.badlogic.ashley_gwt"},

										"Lightweight Entity framework"), AI(new String[] {"com.badlogicgames.gdx:gdx-ai:$aiVersion"},
											new String[] {}, new String[] {}, new String[] {"com.badlogicgames.gdx:gdx-ai:$aiVersion"},
											new String[] {}, new String[] {"com.badlogicgames.gdx:gdx-ai:$aiVersion:sources"},
											new String[] {"com.badlogic.gdx.ai"},

											"Artificial Intelligence framework");

		private String[] coreDependencies;
		private String[] lwjgl2Dependencies;
		private String[] lwjgl3Dependencies;
		private String[] androidDependencies;
		private String[] iosDependencies;
		private String[] gwtDependencies;
		private String[] gwtInherits;
		private String description;

		ProjectDependency (String[] coreDeps, String[] lwjgl2Deps, String[] lwjgl3Deps, String[] androidDeps, String[] iosDeps,
			String[] gwtDeps, String[] gwtInhertis, String description) {
			this.coreDependencies = coreDeps;
			this.lwjgl2Dependencies = lwjgl2Deps;
			this.lwjgl3Dependencies = lwjgl3Deps;
			this.androidDependencies = androidDeps;
			this.iosDependencies = iosDeps;
			this.gwtDependencies = gwtDeps;
			this.gwtInherits = gwtInhertis;
			this.description = description;
		}

		public String[] getDependencies (ProjectType type) {
			switch (type) {
			case CORE:
				return coreDependencies;
			case LWJGL2:
				return lwjgl2Dependencies;
			case LWJGL3:
				return lwjgl3Dependencies;
			case ANDROID:
				return androidDependencies;
			case IOS:
				return iosDependencies;
			case HTML:
				return gwtDependencies;
			}
			return null;
		}

		public String[] getGwtInherits () {
			return gwtInherits;
		}

		public String getDescription () {
			return description;
		}
	}

	public enum ProjectType {
		CORE("core", "Core"), LWJGL2("legacy_desktop", "Desktop (LWJGL2)"), LWJGL3("desktop",
			"Desktop (LWJGL 3)"), ANDROID("android", "Android"), IOS("ios", "iOS"), HTML("html", "HTML");

		private final String name;
		private final String displayName;

		ProjectType (String name, String displayName) {
			this.name = name;
			this.displayName = displayName;
		}

		public String getName () {
			return name;
		}

		public String getDisplayName () {
			return displayName;
		}

		public String[] getPlugins (Language sourceLanguage) {
			return sourceLanguage.platformPlugins[ordinal()];
		}
	}

}
