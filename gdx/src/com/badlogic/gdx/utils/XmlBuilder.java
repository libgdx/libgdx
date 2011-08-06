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
import java.util.ArrayDeque;

//@off
/**
 * Convenience class for emitting XML. <pre>
 * StringWriter writer = new StringWriter();
 * XmlBuilder xml = new XmlBuilder(writer);
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
public class XmlBuilder {
	private final Writer writer;
	private final ArrayDeque<String> stack = new ArrayDeque();
	private String currentElement;

	public XmlBuilder (Writer writer) {
		this.writer = writer;
	}

	private void indent () throws IOException {
		int count = stack.size();
		if (currentElement != null) count++;
		for (int i = 0; i < count; i++)
			writer.write('\t');
	}

	public XmlBuilder element (String name) throws IOException {
		startElementContent();
		writer.write('<');
		writer.write(name);
		currentElement = name;
		return this;
	}

	private void startElementContent () throws IOException {
		if (currentElement != null) {
			stack.push(currentElement);
			currentElement = null;
			writer.write(">\n");
		}
		indent();
	}

	public XmlBuilder attribute (String name, String value) throws IOException {
		if (currentElement == null) throw new IllegalStateException();
		writer.write(' ');
		writer.write(name);
		writer.write("=\"");
		writer.write(value);
		writer.write('"');
		return this;
	}

	public XmlBuilder text (String text) throws IOException {
		startElementContent();
		writer.write(text);
		writer.write('\n');
		return this;
	}

	public XmlBuilder pop () throws IOException {
		if (currentElement != null) {
			writer.write("/>\n");
			currentElement = null;
		} else {
			String name = stack.pop();
			indent();
			writer.write("</");
			writer.write(name);
			writer.write(">\n");
		}
		return this;
	}

	/**
	 * Calls {@link #pop()} for each remaining open element, if any.
	 */
	public void close () throws IOException {
		while (!stack.isEmpty())
			pop();
	}
}
