
package com.badlogic.gdx.setup;

public enum Language {
	JAVA("java", "", "", "",
		"java-library;java-library;java-library;com.android.application;java-library,robovm;java-library,gwt,war,org.gretty",
		true), KOTLIN("kotlin", "ext.kotlinVersion = '1.3.41'",
			"classpath \"org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion\"",
			"api \"org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion\"",
			"kotlin;kotlin;kotlin;android,kotlin-android;kotlin,robovm; ", false);

	public final String[] corePlugins, lwjgl2Plugins, lwjgl3Plugins, androidPlugins, iosPlugins, htmlPlugins;
	public final String[][] platformPlugins;
	public final String name, buildScript, buildScriptDependencies, dependencies;
	public final boolean gwtSupported;

	private Language (String name, String buildScript, String buildScriptDependencies, String dependencies, String src,
		boolean gwtSupported) {
		this.name = name;
		this.buildScript = buildScript;
		this.buildScriptDependencies = buildScriptDependencies;
		this.dependencies = dependencies;
		String[] parts = src.split(";");
		platformPlugins = new String[parts.length][];
		for (int i = 0; i < platformPlugins.length; i++) {
			platformPlugins[i] = parts[i].split(",");
		}
		this.corePlugins = parts[0].split(",");
		this.lwjgl2Plugins = parts[1].split(",");
		this.lwjgl3Plugins = parts[2].split(",");
		this.androidPlugins = parts[3].split(",");
		this.iosPlugins = parts[4].split(",");
		this.htmlPlugins = parts[5].split(",");
		this.gwtSupported = gwtSupported;
	}
}
