/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.harmony.beans.internal.nls.Messages;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.badlogic.gdx.beans.XMLDecoder;

public class Handler extends DefaultHandler {

	private Vector<Object> result;

	private Vector<Command> commands;

	private XMLDecoder decoder;

	private Map<String, Command> references;

	private Stack<Command> stack;

	private int tabCount;

	public Handler (XMLDecoder decoder, Vector<Object> result) {
		this.decoder = decoder;
		this.result = result;
		this.commands = new Vector<Command>();
		this.references = new HashMap<String, Command>();
		this.stack = new Stack<Command>();
	}

	// clear collections to prepare parsing document
	@Override
	public void startDocument () {
		references.clear();
		tabCount = 0;
	}

	// create new command and put it on stack
	@Override
	public void startElement (String namespaceURI, String localeName, String tagName, Attributes attrs) throws SAXException {
		Command.printAttrs(tabCount, tagName, attrs);
		Command cmd = tagName.equals("java") ? new Command(decoder, tagName, //$NON-NLS-1$
			Command.parseAttrs(tagName, attrs)) : new Command(tagName, Command.parseAttrs(tagName, attrs));
		stack.push(cmd);
		++tabCount;
	}

	// add data to command
	@Override
	public void characters (char[] text, int start, int length) throws SAXException {
		if (length > 0) {
			String data = String.valueOf(text, start, length).replace('\n', ' ').replace('\t', ' ').trim();
			if (data.length() > 0) {
				Command.prn(tabCount, tabCount + ">setting data=" + data //$NON-NLS-1$
					+ "<EOL>"); //$NON-NLS-1$
				Command cmd = stack.peek();
				cmd.setData(data);
			}
		}
	}

	// pop command from stack and put it to one of collections
	@Override
	public void endElement (String namespaceURI, String localeName, String tagName) throws SAXException {
		Command cmd = stack.pop();
		// cmd.setTabCount(tabCount);

		// find if command works in context
		if (!stack.isEmpty()) {
			Command ctx = stack.peek();
			ctx.addChild(cmd);
		}

		// upper level commands
		if (stack.size() == 1 && cmd.isExecutable()) {
			commands.add(cmd);
		}

		// store reference to command
		if (cmd.hasAttr("id")) { //$NON-NLS-1$
			references.put(cmd.getAttr("id"), cmd); //$NON-NLS-1$
		}

		try {
			cmd.exec(references);
		} catch (Exception e) {
			SAXException e2 = new SAXException(e.getMessage());

			e2.initCause(e);
			throw e2;
		}

		if (--tabCount < 0) {
			tabCount = 0;
		}

		Command.prn(tabCount, tabCount + ">...<" + tagName + "> end"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// iterate over deferred commands and execute them again
	@Override
	public void endDocument () throws SAXException {
		for (int i = 0; i < commands.size(); ++i) {
			Command cmd = commands.elementAt(i);
			try {
				cmd.backtrack(references);
			} catch (Exception e) {
				throw new SAXException(Messages.getString("beans.0B")); //$NON-NLS-1$
			}
			// if(!backtracked)
			// throw new SAXException("Command " + cmd.getTagName() +
			// " is unresolved on second run() call.");
		}

		for (int i = 0; i < commands.size(); ++i) {
			Command cmd = commands.elementAt(i);
			result.add(cmd.getResultValue());
		}
	}
}
