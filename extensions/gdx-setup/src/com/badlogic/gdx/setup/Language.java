package com.badlogic.gdx.setup;

public enum Language {
	JAVA ("java", "", "", "", "java;java;android;java,robovm;moe;gwt,war", "_specifics/java", ".java", true) ,
	KOTLIN ("kotlin", "ext.kotlinVersion = '1.1.3'", 
			"classpath \"org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion\"", 
			"compile \"org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion\"",
			"kotlin;kotlin;android,kotlin-android;kotlin,robovm;moe,kotlin; ",
			"_specifics/kotlin",
			".kt",
			false);
	
	public final String[] corePlugins, desktopPlugins, androidPlugins, iosPlugins, iosmoePlugins, htmlPlugins;
	public final String[][] platformPlugins;
	public final String name, buildScript, buildScriptDependencies, dependencies, resourcePath, fileExtension;
	public final boolean gwtSupported;
	
	private Language(String name, String buildScript, String buildScriptDependencies, String dependencies, String src, String resourcePath, String fileExtension, boolean gwtSupported) {
		this.name = name;
		this.buildScript = buildScript;
		this.buildScriptDependencies = buildScriptDependencies;
		this.dependencies = dependencies;
		String[] parts = src.split(";");
		platformPlugins = new String[parts.length][];
		for(int i = 0; i < platformPlugins.length; i++) {
			platformPlugins[i] = parts[i].split(",");
		}
		this.corePlugins = parts[0].split(",");
		this.desktopPlugins = parts[1].split(",");
		this.androidPlugins = parts[2].split(",");
		this.iosPlugins = parts[3].split(",");
		this.iosmoePlugins = parts[4].split(",");
		this.htmlPlugins = parts[5].split(",");
		this.resourcePath = resourcePath;
		this.fileExtension = fileExtension;
		this.gwtSupported = gwtSupported;
	}
}
