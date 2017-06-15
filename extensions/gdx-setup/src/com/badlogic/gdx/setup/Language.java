package com.badlogic.gdx.setup;

public enum Language {
	JAVA ("java", "", "", "", "java;java;android;java,robovm;moe;gwt,war") ,
	KOTLIN ("kotlin", "ext.kotlinVersion = '1.0.0'", 
			"classpath \"org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion\"", 
			"compile \"org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion\"",
			"kotlin;kotlin;android,kotlin-android;kotlin,robovm;moe;gwt,war");
	
	public final String[] corePlugins, desktopPlugins, androidPlugins, iosPlugins, iosmoePlugins, htmlPlugins;
	private final String source;
	public final String[][] values;
	public final String name, buildScript, buildScriptDependencies, dependencies;
	
	private Language(String name, String buildScript, String buildScriptDependencies, String dependencies, String src) {
		this.name = name;
		this.buildScript = buildScript;
		this.buildScriptDependencies = buildScriptDependencies;
		this.dependencies = dependencies;
		source = src;
		String[] parts = src.split(";");
		values = new String[parts.length][];
		for(int i = 0; i < values.length; i++) {
			values[i] = parts[i].split(",");
		}
		this.corePlugins = parts[0].split(",");
		this.desktopPlugins = parts[1].split(",");
		this.androidPlugins = parts[2].split(",");
		this.iosPlugins = parts[3].split(",");
		this.iosmoePlugins = parts[4].split(",");
		this.htmlPlugins = parts[5].split(",");
	}
	
}
