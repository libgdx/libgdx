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

package com.badlogic.gdx.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

/** <code>PropertyFileControl</code> is a concrete implementation of <code>Control</code> that creates a
 * <code>PropertyResourceBundle</code> loading properties files from a path relative to a given {@link FileType}. Both
 * ".properties" and ".xml" formats are supported, see {@link java.util.Properties} documentation. This class implements the
 * default behavior of <code>ResourceBundle</code>.
 * @author davebaol */
public class PropertyFileControl extends Control {

	static final String JAVA_PROPERTIES = "gdx.properties";
	static final String JAVA_PROPERTIES_XML = "gdx.properties.xml";

	static final List<String> FORMATS_PROPERTIES_ONLY = Collections.unmodifiableList(Arrays.asList(JAVA_PROPERTIES));
	static final List<String> FORMATS_XML_ONLY = Collections.unmodifiableList(Arrays.asList(JAVA_PROPERTIES_XML));
	static final List<String> FORMATS_PROPERTIES_AND_XML = Collections.unmodifiableList(Arrays.asList(JAVA_PROPERTIES,
		JAVA_PROPERTIES_XML));
	static final List<String> FORMATS_XML_AND_PROPERTIES = Collections.unmodifiableList(Arrays.asList(JAVA_PROPERTIES_XML,
		JAVA_PROPERTIES));

	/** A control using the {@link FileType#Internal Internal file type} and ISO-8859-1 encoding. It doesn't support property files
	 * in the form of a XML document.
	 * <p>
	 * This is also the default control for <code>ResourceBundle.getBundle()</code> methods. */
	public static final PropertyFileControl INTERNAL_PROPERTIES_ONLY = new PropertyFileControl();

	/** A control using the {@link FileType#Internal Internal file type} and ISO-8859-1 encoding. It only supports property files in
	 * the form of a XML document. */
	public static final PropertyFileControl INTERNAL_XML_ONLY = new PropertyFileControl() {
		@Override
		public List<String> getFormats (String baseName) {
			if (baseName == null) throw new NullPointerException();
			return FORMATS_XML_ONLY;
		}
	};

	/** A control using the {@link FileType#Internal Internal file type} and ISO-8859-1 encoding. It supports property files in the
	 * form of key/value pairs and XML document. The key/value pairs format has higher priority for the search strategy. */
	public static final PropertyFileControl INTERNAL_PROPERTIES_AND_XML = new PropertyFileControl() {
		@Override
		public List<String> getFormats (String baseName) {
			if (baseName == null) throw new NullPointerException();
			return FORMATS_PROPERTIES_AND_XML;
		}
	};

	/** A control using the {@link FileType#Internal Internal file type} and ISO-8859-1 encoding. It supports property files in the
	 * form of key/value pairs and XML document. The XML format has higher priority for the search strategy. */
	public static final PropertyFileControl INTERNAL_XML_AND_PROPERTIES = new PropertyFileControl() {
		@Override
		public List<String> getFormats (String baseName) {
			if (baseName == null) throw new NullPointerException();
			return FORMATS_XML_AND_PROPERTIES;
		}
	};

	private static final String SUFFIX_PROPERTIES = "properties";
	private static final String SUFFIX_XML = "xml";

	private FileType fileType;
	private String encoding;

	public PropertyFileControl () {
		this(FileType.Internal);
	}

	public PropertyFileControl (FileType fileType) {
		this(fileType, "ISO-8859-1");
	}

	public PropertyFileControl (FileType fileType, String encoding) {
		this.fileType = fileType;
		this.encoding = encoding;
	}

	/** This implementation returns the absolute path of the base name of the resource bundle. This method is called by the
	 * <code>ResourceBundle.getBundle</code> factory method to create a unique cache key. This way resource bundles created by
	 * <code>PropertyFileControl</code> instances having different <code>FileType</code> are considered to be distinct even if they
	 * have the same base name.
	 * @param baseName baseName the base name of the resource bundle
	 * @return the auxiliary id of the bundle. */
	@Override
	public String getAuxId (String baseName) {
		return Gdx.files.getFileHandle(baseName, fileType).file().getAbsolutePath();
	}

	/** Returns a <code>List</code> of <code>String</code>s containing formats to be used to load resource bundles for the given
	 * <code>baseName</code>. The <code>ResourceBundle.getBundle</code> factory method tries to load resource bundles with formats
	 * in the order specified by the list. The list returned by this method must have at least one <code>String</code>. The
	 * predefined formats are <code>"gdx.properties"</code> and <code>"gdx.properties.xml"</code> for
	 * {@linkplain PropertyResourceBundle properties-based} resource bundles with format "properties" and "xml" respectively, see
	 * {@link java.util.Properties} documentation. Strings starting with <code>"gdx."</code> are reserved for future extensions and
	 * should not be used by application-defined formats.
	 * 
	 * <p>
	 * The default implementation returns a list containing only the format <code>"gdx.properties"</code> so that the
	 * <code>ResourceBundle.getBundle</code> factory method only looks up properties-based bundles in the form of key/value pairs,
	 * i.e. the typical ".properties" files.
	 * 
	 * @param baseName the base name of the resource bundle.
	 * @return a <code>List</code> of <code>String</code>s containing formats for loading resource bundles.
	 * @exception NullPointerException if <code>baseName</code> is null */
	@Override
	public List<String> getFormats (String baseName) {
		if (baseName == null) throw new NullPointerException();
		return FORMATS_PROPERTIES_ONLY;
	}

	@Override
	public ResourceBundle newBundle (String baseName, Locale locale, String format, boolean reload) throws IOException {
		if (baseName == null || locale == null || format == null) throw new NullPointerException();
		ResourceBundle bundle = null;
		String suffix = getSuffix(format);
		if (suffix != null) {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, suffix);
			FileHandle fileHandle = Gdx.files.getFileHandle(resourceName, fileType);
			if (fileHandle.exists()) {
				InputStream stream = null;
				try {
					stream = fileHandle.read();
					if (suffix == SUFFIX_PROPERTIES)
						bundle = new PropertyResourceBundle(new InputStreamReader(stream, encoding));
					else
						bundle = new PropertyResourceBundle(stream, true);
				} finally {
					StreamUtils.closeQuietly(stream);
				}
			}
		}
		return bundle;
	}

	@Override
	public boolean needsReload (String baseName, Locale locale, String format, ResourceBundle bundle, long loadTime) {
		if (bundle == null) {
			throw new NullPointerException();
		}
		String suffix = getSuffix(format);
		if (suffix != null) {
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, suffix);
			FileHandle fileHandle = Gdx.files.getFileHandle(resourceName, fileType);
			if (fileHandle.exists()) {
				long lastModified = fileHandle.lastModified();
				if (lastModified > 0 && lastModified > loadTime) {
					return true;
				}
			}
		}
		return false;
	}

	private String getSuffix (String format) {
		if (format.equals(JAVA_PROPERTIES)) return SUFFIX_PROPERTIES;
		if (format.equals(JAVA_PROPERTIES_XML)) return SUFFIX_XML;
		return null;
	}
}
