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

package com.google.gwt.corp.compatibility;

import java.io.OutputStream;
import java.io.PrintStream;

/** Print stream for GWT that prints to the browser console.
 * 
 * @author Stefan Haustein */
public class ConsolePrintStream extends PrintStream {

	StringBuilder buf = new StringBuilder();

	public ConsolePrintStream () {
		super((OutputStream)null);
	}

	public void print (String s) {

		while (true) {
			int cut = s.indexOf('\n');
			if (cut == -1) {
				break;
			}
			println(s.substring(0, cut));
			s = s.substring(cut + 1);
		}

		buf.append(s);
	}

	public native void consoleLog (String msg) /*-{
																if (window.console) {
																window.console.log(msg);
																} else {
																document.title = "LOG:" + msg;
																}
																}-*/;

	public void print (char c) {
		if (c == '\n') {
			println("");
		} else {
			buf.append(c);
		}
	}

	public void println () {
		println("");
	}

	@Override
	public void println (String s) {
		buf.append(s);
		consoleLog(buf.toString());
		buf.setLength(0);
	}

}
