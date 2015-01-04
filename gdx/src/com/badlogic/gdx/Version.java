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

package com.badlogic.gdx;

import com.badlogic.gdx.utils.GdxRuntimeException;

/** The version of libgdx
 * 
 * @author mzechner */
public class Version {
	/** the current version of libgdx as a String in the major.minor.revision format **/
	public static final String VERSION = "1.5.3";

	/** the current major version of libgdx **/
	public static final int MAJOR;

	/** the current minor version of libgdx **/
	public static final int MINOR;

	/** the current revision version of libgdx **/
	public static final int REVISION;

	static {
		try {
			String[] v = VERSION.split("\\.");
			MAJOR = v.length < 1 ? 0 : Integer.valueOf(v[0]);
			MINOR = v.length < 2 ? 0 : Integer.valueOf(v[1]);
			REVISION = v.length < 3 ? 0 : Integer.valueOf(v[2]);
		}
		catch (Throwable t) {
			// Should never happen
			throw new GdxRuntimeException("Invalid version "+VERSION, t);
		}
	}

	public static boolean isHigher (int major, int minor, int revision) {
		return isHigherEqual(major, minor, revision+1);
	}

	public static boolean isHigherEqual (int major, int minor, int revision) {
		if (MAJOR != major)
			return MAJOR > major;
		if (MINOR != minor)
			return MINOR > minor;
		return REVISION >= revision;
	}

	public static boolean isLower (int major, int minor, int revision) {
		return isLowerEqual(major, minor, revision-1);
	}

	public static boolean isLowerEqual (int major, int minor, int revision) {
		if (MAJOR != major)
			return MAJOR < major;
		if (MINOR != minor)
			return MINOR < minor;
		return REVISION <= revision;
	}

}
