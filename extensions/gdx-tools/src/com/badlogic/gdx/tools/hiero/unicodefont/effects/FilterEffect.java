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

package com.badlogic.gdx.tools.hiero.unicodefont.effects;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

import com.badlogic.gdx.tools.hiero.unicodefont.Glyph;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;

/** Applys a {@link BufferedImageOp} filter to glyphs. Many filters can be fond here: http://www.jhlabs.com/ip/filters/index.html
 * @author Nathan Sweet */
public class FilterEffect implements Effect {
	private BufferedImageOp filter;

	public FilterEffect () {
	}

	public FilterEffect (BufferedImageOp filter) {
		this.filter = filter;
	}

	public void draw (BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		BufferedImage scratchImage = EffectUtil.getScratchImage();
		filter.filter(image, scratchImage);
		image.getGraphics().drawImage(scratchImage, 0, 0, null);
	}

	public BufferedImageOp getFilter () {
		return filter;
	}

	public void setFilter (BufferedImageOp filter) {
		this.filter = filter;
	}
}
