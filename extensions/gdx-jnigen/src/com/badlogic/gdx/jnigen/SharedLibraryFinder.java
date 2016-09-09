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

import java.util.zip.ZipFile;

/** Interface used for overriding the way of finding a name of a shared library, for a specific platform.
 * @author Rob Bogie <bogie.rob@gmail.com> */
public interface SharedLibraryFinder {
	/** @param sharedLibName The name of the shared lib that is asked to be loaded.
	 * @param is64Bit Whether the platform is 64 bit
	 * @param nativesJar A ZipFile object, which gives the ability to walk through the containing files, and allows for pattern
	 *           matching. May be null if no zipfile is used.
	 * @return The name of the shared file, or null if none available */
	String getSharedLibraryNameWindows (String sharedLibName, boolean is64Bit, ZipFile nativesJar);

	/** @param sharedLibName The name of the shared lib that is asked to be loaded.
	 * @param is64Bit Whether the platform is 64 bit
	 * @param isArm Whether the platform has the ARM architecture
	 * @param nativesJar A ZipFile object, which gives the ability to walk through the containing files, and allows for pattern
	 *           matching. May be null if no zipfile is used.
	 * @return The name of the shared file, or null if none available */
	String getSharedLibraryNameLinux (String sharedLibName, boolean is64Bit, boolean isArm, ZipFile nativesJar);

	/** @param sharedLibName The name of the shared lib that is asked to be loaded.
	 * @param is64Bit
	 * @param nativesJar A ZipFile object, which gives the ability to walk through the containing files, and allows for pattern
	 *           matching. May be null if no zipfile is used.
	 * @return The name of the shared file, or null if none available */
	String getSharedLibraryNameMac (String sharedLibName, boolean is64Bit, ZipFile nativesJar);

	/** @param sharedLibName The name of the shared lib that is asked to be loaded.
	 * @param nativesJar A ZipFile object, which gives the ability to walk through the containing files, and allows for pattern
	 *           matching. May be null if no zipfile is used.
	 * @return The name of the shared file, or null if none available */
	String getSharedLibraryNameAndroid (String sharedLibName, ZipFile nativesJar);
}
