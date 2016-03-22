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

package com.badlogic.gdx.tools.etc1;

import java.io.OutputStream;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class ETC1CompressorParams {

	private Pixmap inputPixmap;
	private Color transparentColor;
	private OutputStream outputStream;

	public ETC1CompressorParams () {
	}

	public void setInputPixmap (Pixmap inputPixmap) {
		this.inputPixmap = inputPixmap;
	}

	public Pixmap getInputPixmap () {
		return inputPixmap;
	}

	public Color getTransparentColor () {
		return transparentColor;
	}

	public void setTransparentColor (Color transparentColor) {
		this.transparentColor = transparentColor;
	}

	public void setOutputStream (OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public OutputStream getOutputStream () {
		return outputStream;
	}

}
