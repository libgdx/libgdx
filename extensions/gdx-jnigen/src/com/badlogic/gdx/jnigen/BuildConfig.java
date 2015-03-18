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

package com.badlogic.gdx.jnigen;

/** Specifies the global properties of a native build.</p>
 * 
 * The shared library name will be used to generate the shared libraries. The build directory is used to store the object files
 * during compilation for each {@link BuildTarget}. It is relative to the jni directory which houses the C/C++ source code. The
 * libs directory is the final output directory where the natives jar and arm shared libraries will be written to.
 * 
 * Used with {@link AntScriptGenerator} to generate the build scripts for build targets.
 * @author mzechner */
public class BuildConfig {
	/** the name of the shared library, without prefix or suffix, e.g. 'gdx', 'bullet' **/
	public final String sharedLibName;
	/** the directory to put the object files in **/
	public final FileDescriptor buildDir;
	/** the directory to put the shared libraries and natives jar file in **/
	public final FileDescriptor libsDir;
	/** the directory containing the native code **/
	public final FileDescriptor jniDir;
	/** additional shared library files to be packed into the natives jar, relative to the jni dir **/
	public String[] sharedLibs;

	/** Creates a new BuildConfig. The build directory, the libs directory and the jni directory are assumed to be "target", "libs"
	 * and "jni". All paths are relative to the application's working directory.
	 * @param sharedLibName the shared library name, without prefix or suffix, e.g. 'gdx', 'bullet' */
	public BuildConfig (String sharedLibName) {
		this.sharedLibName = sharedLibName;
		this.buildDir = new FileDescriptor("target");
		this.libsDir = new FileDescriptor("libs");
		this.jniDir = new FileDescriptor("jni");
	}

	/** Creates a new BuildConfig. All paths are relative to the application's working directory.
	 * @param sharedLibName the shared library name, without prefix or suffix, e.g. 'gdx', 'bullet'
	 * @param temporaryDir
	 * @param libsDir
	 * @param jniDir */
	public BuildConfig (String sharedLibName, String temporaryDir, String libsDir, String jniDir) {
		this.sharedLibName = sharedLibName;
		this.buildDir = new FileDescriptor(temporaryDir);
		this.libsDir = new FileDescriptor(libsDir);
		this.jniDir = new FileDescriptor(jniDir);
	}
}
