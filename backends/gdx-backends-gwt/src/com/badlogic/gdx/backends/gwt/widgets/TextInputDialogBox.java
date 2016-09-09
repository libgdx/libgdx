/*******************************************************************************
 * Copyright 2012 See AUTHORS file.
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

package com.badlogic.gdx.backends.gwt.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TextInputDialogBox extends DialogBox {
	private PlaceholderTextBox textBox;

	public TextInputDialogBox (String title, String text, String placeholder) {
		// Set the dialog box's caption.
		setText(title);

		VerticalPanel vPanel = new VerticalPanel();
		HorizontalPanel hPanel = new HorizontalPanel();

		// Enable animation.
		setAnimationEnabled(true);

		// Enable glass background.
		setGlassEnabled(true);

		// Center this bad boy.
		center();

		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		Button ok = new Button("OK");
		ok.addClickHandler(new ClickHandler() {
			public void onClick (ClickEvent event) {
				TextInputDialogBox.this.onPositive();
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addClickHandler(new ClickHandler() {
			public void onClick (ClickEvent event) {
				TextInputDialogBox.this.onNegative();
			}
		});

		hPanel.add(ok);
		hPanel.add(cancel);

		textBox = new PlaceholderTextBox();
		textBox.setPlaceholder(placeholder);
		textBox.setWidth("97%");
		textBox.setText(text);
		vPanel.add(textBox);
		vPanel.add(hPanel);

		setWidget(vPanel);
	}

	protected void onPositive () {
		if (listener != null) {
			listener.onPositive(textBox.getText());
		}
		this.hide();
	}

	protected void onNegative () {
		if (listener != null) {
			listener.onNegative();
		}
		this.hide();
	}

	TextInputDialogListener listener;

	public void setListener (TextInputDialogListener listener) {
		this.listener = listener;
	}

	public interface TextInputDialogListener {
		void onPositive (String text);

		void onNegative ();
	}
}
