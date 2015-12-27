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

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.badlogic.gdx.utils.Clipboard;

/** Clipboard implementation for desktop that uses the system clipboard via the default AWT {@link Toolkit}.
 * @author mzechner */
public class LwjglClipboard implements Clipboard, ClipboardOwner {
	@Override
	public String getContents () {
		String result = "";
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException ex) {
				// doh...
			} catch (IOException ex) {
				// doh...
			}
		}
		return result;
	}

	@Override
	public void setContents (String content) {
		StringSelection stringSelection = new StringSelection(content);
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	@Override
	public void lostOwnership (java.awt.datatransfer.Clipboard arg0, Transferable arg1) {
	}
}
