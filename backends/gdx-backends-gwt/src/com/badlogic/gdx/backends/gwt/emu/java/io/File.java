/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.io;

import java.util.ArrayList;

import com.google.gwt.storage.client.Storage;

/** LocalStorage based File implementation for GWT. Should probably have used Harmony as a starting point instead of writing this
 * from scratch.
 * 
 * @author Stefan Haustein */
public class File {

	public static final File ROOT = new File("");

	public static final char separatorChar = '/';

	public static final String separator = "" + separatorChar;

	public static final char pathSeparatorChar = ':';

	public static final String pathSeparator = "" + pathSeparatorChar;

	public static final Storage LocalStorage = Storage.getLocalStorageIfSupported();
	
	File parent;
	String name;
	boolean absolute;

	public File (String pathname) {
		while (pathname.endsWith(separator) && pathname.length() > 0) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}

		int cut = pathname.lastIndexOf(separatorChar);
		if (cut == -1) {
			name = pathname;
		} else if (cut == 0) {
			name = pathname.substring(cut);
			parent = name.equals("") ? null : ROOT;
		} else {
			name = pathname.substring(cut + 1);
			parent = new File(pathname.substring(0, cut));
		}

// Compatibility.println("new File ('"+pathname+ "'); canonical name: '" + getCanonicalPath() + "'");
	}

	public File (String parent, String child) {
		this(new File(parent), child);
	}

	public File (File parent, String child) {
		this.parent = parent;
		this.name = child;
	}

	/*
	 * public File(URI uri) { }
	 */

	public String getName () {
		return name;
	}

	public String getParent () {
		return parent == null ? "" : parent.getPath();
	}

	public File getParentFile () {
		return parent;
	}

	public String getPath () {
		return parent == null ? name : (parent.getPath() + separatorChar + name);
	}

	private boolean isRoot () {
		return name.equals("") && parent == null;
	}

	public boolean isAbsolute () {
		if (isRoot()) {
			return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.isAbsolute();
	}

	public String getAbsolutePath () {
		String path = getAbsoluteFile().getPath();
		return path.length() == 0 ? "/" : path;
	}

	public File getAbsoluteFile () {
		if (isAbsolute()) {
			return this;
		}
		if (parent == null) {
			return new File(ROOT, name);
		}
		return new File(parent.getAbsoluteFile(), name);
	}

	public String getCanonicalPath () {
		return getCanonicalFile().getAbsolutePath();
	}

	public File getCanonicalFile () {
		File cParent = parent == null ? null : parent.getCanonicalFile();
		if (name.equals(".")) {
			return cParent == null ? ROOT : cParent;
		}
		if (cParent != null && cParent.name.equals("")) {
			cParent = null;
		}
		if (name.equals("..")) {
			if (cParent == null) {
				return ROOT;
			}
			if (cParent.parent == null) {
				return ROOT;
			}
			return cParent.parent;
		}
		if (cParent == null && !name.equals("")) {
			return new File(ROOT, name);
		}
		return new File(cParent, name);
	}

	/*
	 * public URL toURL() throws MalformedURLException { }
	 * 
	 * public URI toURI() { }
	 */

	public boolean canRead () {
		return true;
	}

	public boolean canWrite () {
		return true;
	}

	public boolean exists () {
		return LocalStorage.getItem(getCanonicalPath()) != null;
	}

	public boolean isDirectory () {
		String s = LocalStorage.getItem(getCanonicalPath());
		return s != null && s.startsWith("{");
	}

	public boolean isFile () {
		String s = LocalStorage.getItem(getCanonicalPath());
		return s != null && !s.startsWith("{");
	}

	public boolean isHidden () {
		return false;
	}

	public long lastModified () {
		return 0;
	}

	public long length () {
		try {
			if (!exists()) {
				return 0;
			}

			RandomAccessFile raf = new RandomAccessFile(this, "r");
			long len = raf.length();
			raf.close();
			return len;
		} catch (IOException e) {
			return 0;
		}
	}

	public boolean createNewFile () throws IOException {
		if (exists()) return false;
		if (!parent.exists()) return false;
		LocalStorage.setItem(getCanonicalPath(), RandomAccessFile.btoa(""));
		return true;
	}

	public boolean delete () {
		if (!exists()) {
			return false;
		}
		LocalStorage.removeItem(getCanonicalPath());
		return true;
	}

	public void deleteOnExit () {
		throw new RuntimeException("NYI: File.deleteOnExit()");
	}

	public String[] list () {
		throw new RuntimeException("NYI: File.list()");
	}

	/*
	 * public String[] list(FilenameFilter filter) { return null; }
	 */

	public File[] listFiles () {
		return listFiles(null);
	}

	public File[] listFiles (FilenameFilter filter) {
		ArrayList<File> files = new ArrayList<File>();
		String prefix = getCanonicalPath();
		if (!prefix.endsWith(separator)) {
			prefix += separatorChar;
		}
		int cut = prefix.length();
		int cnt = LocalStorage.getLength();
		for (int i = 0; i < cnt; i++) {
			String key = LocalStorage.key(i);
			if (key.startsWith(prefix) && key.indexOf(separatorChar, cut) == -1) {
				String name = key.substring(cut);
				if (filter == null || filter.accept(this, name)) {
					files.add(new File(this, name));
				}
			}
		}
		return files.toArray(new File[files.size()]);
	}

	/*
	 * public File[] listFiles(FileFilter filter) { return null; }
	 */

	public boolean mkdir () {
		if (parent != null && !parent.exists()) {
			return false;
		}
		if (exists()) {
			return false;
		}
		// We may want to make this a JS map
		LocalStorage.setItem(getCanonicalPath(), "{}");
		return true;
	}

	public boolean mkdirs () {
		if (parent != null) {
			parent.mkdirs();
		}
		return mkdir();
	}

	public boolean renameTo (File dest) {
		throw new RuntimeException("renameTo()");
	}

	public boolean setLastModified (long time) {
		return false;
	}

	public boolean setReadOnly () {
		return false;
	}

	public static File[] listRoots () {
		return new File[] {ROOT};
	}

	public static File createTempFile (String prefix, String suffix, File directory) throws IOException {
		throw new RuntimeException("NYI: createTempFile");
	}

	public static File createTempFile (String prefix, String suffix) throws IOException {
		throw new RuntimeException("NYI: createTempFile");
	}

	public int compareTo (File pathname) {
		throw new RuntimeException("NYI: File.compareTo()");
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof File)) {
			return false;
		}
		return getPath().equals(((File)obj).getPath());
	}

	public int hashCode () {
		return parent != null ? parent.hashCode() + name.hashCode() : name.hashCode();
	}

	public String toString () {
		return name;
	}
}
