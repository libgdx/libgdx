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

package com.badlogic.gdx.backends.gwt.preloader;

/** Interface used by the PreloaderBundleGenerator to decide whether an asset found in the gdx.assetpath should be included in the
 * war/ folder or not. Also used to determine the type of an asset. Default implementation can be found in DefaultAssetFilter, and
 * is used if user doesn't specify a custom filter in the module gwt.xml file.
 * @author mzechner */
public interface AssetFilter {
	public enum AssetType {
		Image("i"), Audio("a"), Text("t"), Binary("b"), Directory("d");

		public final String code;

		private AssetType (String code) {
			this.code = code;
		}
	}

	/** @param file the file to filter
	 * @param isDirectory whether the file is a directory
	 * @return whether to include the file in the war/ folder or not. */
	public boolean accept (String file, boolean isDirectory);

	/** @param file the file to get the type for
	 * @return the type of the file, one of {@link AssetType} */
	public AssetType getType (String file);

	/** @param file the file to get the bundle name for
	 * @return the name of the bundle to which this file should be added */	
	public String getBundleName(String file);

}
