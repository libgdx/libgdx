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

import java.util.Locale;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

/** {@link AssetLoader} for {@link I18NBundle} instances. The I18NBundle is loaded asynchronously.
 * <p>
 * Notice that you can't load two bundles with the same base name and different locale or encoding using the same {@link AssetManager}.
 * For example, if you try to load the 2 bundles below
 * 
 * <pre>
 * manager.load(&quot;i18n/message&quot;, I18NBundle.class, new I18NBundleParameter(Locale.ITALIAN));
 * manager.load(&quot;i18n/message&quot;, I18NBundle.class, new I18NBundleParameter(Locale.ENGLISH));
 * </pre>
 * 
 * the English bundle won't be loaded because the asset manager thinks they are the same bundle since they have the same name.
 * There are 2 use cases:
 * <ul>
 * <li>If you want to load the English bundle so to replace the Italian bundle you have to unload the Italian bundle first.
 * <li>If you want to load the English bundle without replacing the Italian bundle you should use another asset manager.
 * </ul>
 * @author davebaol */
public class I18NBundleLoader extends AsynchronousAssetLoader<I18NBundle, I18NBundleLoader.I18NBundleParameter> {

	public I18NBundleLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	I18NBundle bundle;

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, I18NBundleParameter parameter) {
		this.bundle = null;
		Locale locale;
		String encoding;
		if (parameter == null) {
			locale = Locale.getDefault();
			encoding = null;
		} else {
			locale = parameter.locale == null ? Locale.getDefault() : parameter.locale;
			encoding = parameter.encoding;
		}
		if (encoding == null) {
			this.bundle = I18NBundle.createBundle(file, locale);
		} else {
			this.bundle = I18NBundle.createBundle(file, locale, encoding);
		}
	}

	@Override
	public I18NBundle loadSync (AssetManager manager, String fileName, FileHandle file, I18NBundleParameter parameter) {
		I18NBundle bundle = this.bundle;
		this.bundle = null;
		return bundle;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, I18NBundleParameter parameter) {
		return null;
	}

	static public class I18NBundleParameter extends AssetLoaderParameters<I18NBundle> {
		public final Locale locale;
		public final String encoding;

		public I18NBundleParameter () {
			this(null, null);
		}

		public I18NBundleParameter (Locale locale) {
			this(locale, null);
		}

		public I18NBundleParameter (Locale locale, String encoding) {
			this.locale = locale;
			this.encoding = encoding;
		}
	}

}
