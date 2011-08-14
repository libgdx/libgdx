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
package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetManager;

public abstract class AsynchronousAssetLoader<T, P> extends AssetLoader<T, P> {
	public AsynchronousAssetLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	public abstract void loadAsync (AssetManager manager, String fileName, P parameter);

	public abstract T loadSync ();
}