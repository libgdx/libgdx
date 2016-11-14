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

package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.Writer;

//@off
/**
 * Builder style API for emitting XML. <pre>
 * StringWriter writer = new StringWriter();
 * XmlWriter xml = new XmlWriter(writer);
 * xml.element("meow")
 *	.attribute("moo", "cow")
 *	.element("child")
 *		.attribute("moo", "cow")
 *		.element("child")
 *			.attribute("moo", "cow")
 *			.text("XML is like violence. If it doesn't solve your problem, you're not using enough of it.")
 *		.pop()
 *	.pop()
 * .pop();
 * System.out.println(writer);
 * </pre>
 * @author Nathan Sweet
 */
//@on
public class XmlWriter extends Writer {
	private final Writer writer;
	private final Array<String> stack = new Array();
	private String currentElement;
	private boolean indentNextClose;

	public int indent;

	public XmlWriter (Writer writer) {
		this.writer = writer;
	}

	private void indent () throws IOException {
		int count = indent;
		if (currentElement != null) count++;
		for (int i = 0; i < count; i++)
			writer.write('\t');
	}

	public XmlWriter element (String name) throws IOException {
		if (startElementContent()) writer.write('\n');
		indent();
		writer.write('<');
		writer.write(name);
		currentElement = name;
		return this;
	}

	public XmlWriter element (String name, Object text) throws IOException {
		return element(name).text(text).pop();
	}

	private boolean startElementContent () throws IOException {
		if (currentElement == null) return false;
		indent++;
		stack.add(currentElement);
		currentElement = null;
		writer.write(">");
		return true;
	}

	public XmlWriter attribute (String name, Object value) throws IOException {
		if (currentElement == null) throw new IllegalStateException();
		writer.write(' ');
		writer.write(name);
		writer.write("=\"");
		writer.write(value == null ? "null" : value.toString());
		writer.write('"');
		return this;
	}

	public XmlWriter text (Object text) throws IOException {
		startElementContent();
		String string = text == null ? "null" : text.toString();
		indentNextClose = string.length() > 64;
		if (indentNextClose) {
			writer.write('\n');
			indent();
		}
		writer.write(string);
		if (indentNextClose) writer.write('\n');
		return this;
	}

	public XmlWriter pop () throws IOException {
		if (currentElement != null) {
			writer.write("/>\n");
			currentElement = null;
		} else {
			indent = Math.max(indent - 1, 0);
			if (indentNextClose) indent();
			writer.write("</");
			writer.write(stack.pop());
			writer.write(">\n");
		}
		indentNextClose = true;
		return this;
	}

	/** Calls {@link #pop()} for each remaining open element, if any, and closes the stream. */
	public void close () throws IOException {
		while (stack.size != 0)
			pop();
		writer.close();
	}

	public void write (char[] cbuf, int off, int len) throws IOException {
		startElementContent();
		writer.write(cbuf, off, len);
	}

	public void flush () throws IOException {
		writer.flush();
	}
}
