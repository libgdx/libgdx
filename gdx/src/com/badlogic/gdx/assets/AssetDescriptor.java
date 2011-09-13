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
package com.badlogic.gdx.assets;

/** Describes an asset to be loaded by it's filename, type and {@link AssetLoaderParameters}. Instances of this are used in
 * {@link AssetLoadingTask} to load the actual asset.
 * @author mzechner */
public class AssetDescriptor<T> {
	public final String fileName;
	public final Class<T> type;
	public final AssetLoaderParameters params;

	public AssetDescriptor (String fileName, Class<T> assetType) {
		this(fileName, assetType, null);
	}
	
	public AssetDescriptor (String fileName, Class<T> assetType, AssetLoaderParameters<T> params) {
		this.fileName = fileName.replaceAll("\\\\", "/");
		this.type = assetType;
		this.params = params;
	}

	@Override
	public String toString () {
		return fileName;
	}
}