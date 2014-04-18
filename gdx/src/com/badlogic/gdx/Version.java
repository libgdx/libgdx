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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The version of libgdx
 * 
 * @author mzechner */
public class Version {
	/** the current version of libgdx as a String in the major.minor.revision format **/
	public static final String VERSION = "1.0.0";

	/** the current major version of libgdx **/
	public static final int MAJOR;

	/** the current minor version of libgdx **/
	public static final int MINOR;

	/** the current revision version of libgdx **/
	public static final int REVISION;

	static {
		Matcher m = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(VERSION);
		if (m.matches()) {
			MAJOR = Integer.valueOf(m.group(1));
			MINOR = Integer.valueOf(m.group(2));
			REVISION = Integer.valueOf(m.group(3));
		}
		else {
			// Should never happen
			MAJOR = 0;
			MINOR = 0;
			REVISION = 0;
		}
	}
}
