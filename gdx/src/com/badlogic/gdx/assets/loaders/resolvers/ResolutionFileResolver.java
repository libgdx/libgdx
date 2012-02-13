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

package com.badlogic.gdx.assets.loaders.resolvers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class ResolutionFileResolver implements FileHandleResolver {
	public static class Resolution {
		int portraitWidth;
		int portraitHeight;
		String suffix;

		public Resolution (int portraitWidth, int portraitHeight, String suffix) {
			this.portraitWidth = portraitWidth;
			this.portraitHeight = portraitHeight;
			this.suffix = suffix;
		}
	}

	final FileHandleResolver baseResolver;
	final Resolution[] descriptors;

	public ResolutionFileResolver (FileHandleResolver baseResolver, Resolution... descriptors) {
		this.baseResolver = baseResolver;
		this.descriptors = descriptors;
	}

	@Override
	public FileHandle resolve (String fileName) {
		int width = 0;
		int height = 0;
		if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight()) {
			width = Gdx.graphics.getHeight();
			height = Gdx.graphics.getWidth();
		} else {
			width = Gdx.graphics.getWidth();
			height = Gdx.graphics.getHeight();
		}

		Resolution bestDesc = null;
		int bestDistance = Integer.MAX_VALUE;
		for (int i = 0, n = descriptors.length; i < n; i++) {
			int distance = Math.abs(width - descriptors[i].portraitWidth) + Math.abs(height - descriptors[i].portraitHeight);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestDesc = descriptors[i];
			}
		}

		FileHandle originalHandle = new FileHandle(fileName);
		FileHandle handle = baseResolver.resolve(resolve(originalHandle, bestDesc.suffix));
		if (!handle.exists()) handle = baseResolver.resolve(fileName);
		return handle;
	}

	protected String resolve (FileHandle originalHandle, String suffix) {
		return originalHandle.parent() + "/" + suffix + "/" + originalHandle.name();
	}
}
