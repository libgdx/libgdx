
package com.badlogic.gdx.scenes.scene2d.ui.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DesktopClipboard implements Clipboard, ClipboardOwner {

	@Override public String getContents () {
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

	@Override public void setContents (String content) {
		StringSelection stringSelection = new StringSelection(content);
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	@Override public void lostOwnership (java.awt.datatransfer.Clipboard arg0, Transferable arg1) {
	}
}
