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

package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.os.Build;
import android.app.Activity;
import android.text.ClipboardManager;
import android.content.ClipData;

import com.badlogic.gdx.utils.Clipboard;

public class AndroidClipboard implements Clipboard {
	Context context;

	public AndroidClipboard (Context context) {
		this.context = context;
	}

	@Override
	public String getContents () {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard.getText() == null) return null;
			return clipboard.getText().toString();
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager)context
				.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = clipboard.getPrimaryClip();
			if (clip == null) return null;
			CharSequence text = clip.getItemAt(0).getText();
			if (text == null) return null;
			return text.toString();
		}
	}

	@Override
	public void setContents (final String contents) {
		try {
			((Activity)context).runOnUiThread(new Runnable() {
				public void run () {
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						android.text.ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
						clipboard.setText(contents);
					} else {
						android.content.ClipboardManager clipboard = (android.content.ClipboardManager)context
							.getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData data = ClipData.newPlainText(contents, contents);
						clipboard.setPrimaryClip(data);
					}
				}
			});
		} catch (final Exception ex) {
		}
	}
}
