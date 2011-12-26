package com.badlogic.gdx.jnigen;

import java.util.ArrayList;

import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.FileDescriptor.FileType;

public class AntScriptGenerator {
	public void generate(BuildConfig config, BuildTarget ... targets) {
		// create all the directories for outputing object files, shared libs and natives jar as well as build scripts.
		if(!config.libsDir.exists()) {
			if(!config.libsDir.mkdirs()) throw new RuntimeException("Couldn't create directory for shared library files in '" + config.libsDir + "'");
		}
		if(!config.jniDir.exists()) {
			if(!config.jniDir.mkdirs()) throw new RuntimeException("Couldn't create native code directory '" + config.jniDir + "'");
		}
		
		ArrayList<String> buildFiles = new ArrayList<String>();
		ArrayList<String> libsDirs = new ArrayList<String>();
		ArrayList<String> sharedLibFiles = new ArrayList<String>();
		
		// generate an Ant build script for each target
		for(BuildTarget target: targets) {
			String buildFile = generateBuildTargetTemplate(config, target);
			FileDescriptor libsDir = new FileDescriptor(getLibsDirectory(config, target));
			
			if(!libsDir.exists()) {
				if(!libsDir.mkdirs()) throw new RuntimeException("Couldn't create libs directory '" + libsDir + "'");
			}
			
			String buildFileName = "build-" + target.os.toString().toLowerCase() + (target.is64Bit?"64":"32") + ".xml";
			if(target.buildFileName != null) buildFileName = target.buildFileName;
			config.jniDir.child(buildFileName).writeString(buildFile, false);
			System.out.println("Wrote target '" + target.os + (target.is64Bit?"64":"") + "' build script '" + config.jniDir.child(buildFileName) + "'");
			
			if(!target.excludeFromMasterBuildFile) {
				buildFiles.add(buildFileName);
				sharedLibFiles.add(getSharedLibFilename(target.os, target.is64Bit, config.sharedLibName));
				libsDirs.add("../" + libsDir.path().replace('\\', '/'));
			}
		}
		
		// generate the master build script
		String template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build.xml.template", FileType.Classpath).readString();
		StringBuffer clean = new StringBuffer();
		StringBuffer compile = new StringBuffer();
		StringBuffer pack = new StringBuffer();
		
		for(int i = 0; i < buildFiles.size(); i++) {
			clean.append("\t\t<ant antfile=\"" + buildFiles.get(i) + "\" target=\"clean\"/>\n");
			compile.append("\t\t<ant antfile=\"" + buildFiles.get(i) + "\"/>\n");
			pack.append("\t\t\t<fileset dir=\"" + libsDirs.get(i) + "\" includes=\"" + sharedLibFiles.get(i) + "\"/>");
		}
		
		template = template.replace("%projectName%", config.sharedLibName + "-natives");
		template = template.replace("<clean/>", clean.toString());
		template = template.replace("<compile/>", compile.toString());
		template = template.replace("%packFile%", "../" + config.libsDir.path().replace('\\', '/') + "/" + config.sharedLibName + "-natives.jar");
		template = template.replace("<pack/>", pack);
		
		config.jniDir.child("build.xml").writeString(template, false);
		System.out.println("Wrote master build script '" + config.jniDir.child("build.xml") + "'");
	}
	
	private String getSharedLibFilename(TargetOs os, boolean is64Bit, String sharedLibName) {
		// generate shared lib prefix and suffix, determine jni platform headers directory
		String libPrefix = "";
		String libSuffix = "";
		if(os == TargetOs.Windows) {
			libSuffix = (is64Bit?"64":"") + ".dll";
		}
		if(os == TargetOs.Linux || os == TargetOs.Android) {
			libPrefix = "lib";
			libSuffix = (is64Bit?"64":"") + ".so";
		}
		if(os == TargetOs.MacOsX) {
			libPrefix = "lib";
			libSuffix = ".dylib";
		}
		return libPrefix + sharedLibName + libSuffix;
	}
	
	private String getJniPlatform(TargetOs os) {
		if(os == TargetOs.Windows) return "win32";
		if(os == TargetOs.Linux) return "linux";
		if(os == TargetOs.MacOsX) return "mac";
		return "";
	}
	
	private String getLibsDirectory(BuildConfig config, BuildTarget target) {
		return config.libsDir.child(target.os.toString().toLowerCase() + (target.is64Bit?"64":"32")).path().replace('\\', '/');
	}
	
	private String generateBuildTargetTemplate(BuildConfig config, BuildTarget target) {
		// special case for android
		if(target.os == TargetOs.Android) {
			new AndroidNdkScriptGenerator().generate(config, target);
			return new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build-android.xml.template", FileType.Classpath).readString();
		}
		
		// read template file from resources
		String template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build-target.xml.template", FileType.Classpath).readString();
		
		// generate shared lib filename and jni platform headers directory name
		String libName = getSharedLibFilename(target.os, target.is64Bit, config.sharedLibName);
		String jniPlatform = getJniPlatform(target.os);
		
		// generate include and exclude fileset Ant description for C/C++
		StringBuffer cIncludes = new StringBuffer();
		for(String cInclude: target.cIncludes) {
			cIncludes.append("\t\t<include name=\"" + cInclude + "\"/>\n");
		}
		StringBuffer cppIncludes = new StringBuffer();
		for(String cppInclude: target.cppIncludes) {
			cppIncludes.append("\t\t<include name=\"" + cppInclude + "\"/>\n");
		}
		StringBuffer cExcludes = new StringBuffer();
		for(String cExclude: target.cExcludes) {
			cExcludes.append("\t\t<exclude name=\"" + cExclude + "\"/>\n");
		}
		StringBuffer cppExcludes = new StringBuffer();
		for(String cppExclude: target.cppExcludes) {
			cppExcludes.append("\t\t<exclude name=\"" + cppExclude + "\"/>\n");
		}
			
		// generate C/C++ header directories
		StringBuffer headerDirs = new StringBuffer();
		for(String headerDir: target.headerDirs) {
			headerDirs.append("\t\t\t<arg value=\"" + headerDir + "\"/>\n");
		}
		
		// replace template vars with proper values
		template = template.replace("%projectName%", config.sharedLibName + "-" + target.os + "-" + (target.is64Bit?"64":"32"));
		template = template.replace("%buildDir%", config.buildDir.child(target.os.toString().toLowerCase() + (target.is64Bit?"64":"32")).path().replace('\\', '/'));
		template = template.replace("%libsDir%", "../" + getLibsDirectory(config, target));
		template = template.replace("%libName%", libName);
		template = template.replace("%jniPlatform%", jniPlatform);
		template = template.replace("%compilerPrefix%", target.compilerPrefix);
		template = template.replace("%cFlags%", target.cFlags);
		template = template.replace("%cppFlags%", target.cppFlags);
		template = template.replace("%linkerFlags%", target.linkerFlags);
		template = template.replace("%cIncludes%", cIncludes);
		template = template.replace("%cExcludes%", cExcludes);
		template = template.replace("%cppIncludes%", cppIncludes);
		template = template.replace("%cppExcludes%", cppExcludes);
		template = template.replace("%headerDirs%", headerDirs);
		
		return template;
	}
}
