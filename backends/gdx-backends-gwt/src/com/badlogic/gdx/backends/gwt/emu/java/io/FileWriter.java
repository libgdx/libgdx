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

public class FileWriter {

	private final RandomAccessFile file;

	public FileWriter (String name) throws FileNotFoundException {
		this.file = new RandomAccessFile(new File(name), "rw");
	}

	public void close () throws IOException {
		file.close();
	}

	public void flush () throws IOException {
		file.flush();
	}

	public void write (String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			file.write(s.charAt(i));
		}
	}

// public void write(char[] cbuf, int off, int len) throws IOException {
// // TODO Auto-generated method stub
//
// }

}
