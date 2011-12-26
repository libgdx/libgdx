package com.badlogic.gdx.jnigen;

public class BuildConfig {
	/** the name of the shared library, without prefix or suffix, e.g. 'gdx', 'bullet' **/
	public final String sharedLibName;
	/** the directory to put the object files in **/
	public final FileDescriptor buildDir;
	/** the directory to put the shared libraries and natives jar file in **/
	public final FileDescriptor libsDir;
	/** the directory containing the native code **/
	public final FileDescriptor jniDir;
	
	public BuildConfig(String sharedLibName) {
		this.sharedLibName = sharedLibName;
		this.buildDir = new FileDescriptor("target");
		this.libsDir = new FileDescriptor("libs");
		this.jniDir = new FileDescriptor("jni");
	}
	
	public BuildConfig(String sharedLibName, String temporaryDir, String libsDir, String jniDir) {
		this.sharedLibName = sharedLibName;
		this.buildDir = new FileDescriptor(temporaryDir);
		this.libsDir = new FileDescriptor(libsDir);
		this.jniDir = new FileDescriptor(jniDir);
	}
}