/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.junit.Assert.*;

/** Tests how a skin resolves atlas regions for the pages of a bitmap font. */
public class SkinBitmapFontTest {
	@Rule public TemporaryFolder temp = new TemporaryFolder();

	private final Skin skin = new Skin();
	private final Texture texture = new Texture() {
		public int getWidth () {
			return 16;
		}

		public int getHeight () {
			return 16;
		}
	};

	private TextureRegion addRegion (String name) {
		TextureRegion region = new TextureRegion(texture);
		skin.add(name, region, TextureRegion.class);
		return region;
	}

	private BitmapFont loadFont (String firstPage, String secondPage) throws IOException {
		FileHandle fontFile = new FileHandle(temp.newFile("font.fnt"));
		fontFile.writeString(
			"info face=\"test\" padding=0,0,0,0 spacing=1,1\n" + "common lineHeight=8 base=7 scaleW=16 scaleH=16 pages=2\n"
				+ "page id=0 file=\"" + firstPage + "\"\npage id=1 file=\"" + secondPage + "\"\n"
				+ "chars count=1\nchar id=65 x=0 y=0 width=1 height=1 xoffset=0 yoffset=0 xadvance=1 page=1 chnl=0\n",
			false);
		FileHandle skinFile = new FileHandle(temp.newFile("skin.json"));
		skinFile.writeString("{BitmapFont: {default: {file: font.fnt}}}", false);
		skin.load(skinFile);
		return skin.getFont("default");
	}

	@Test
	public void loadsPagesByDeclaredPath () throws IOException {
		TextureRegion second = addRegion("symbols/page");
		TextureRegion first = addRegion("letters/page");
		BitmapFont font = loadFont("letters/page.png", "symbols/page.png");
		assertSame(first, font.getRegions().get(0));
		assertSame(second, font.getRegions().get(1));
		assertFalse(font.ownsTexture());
	}

	@Test
	public void loadsRemainingPagesWhenFirstPageMatchesFontName () throws IOException {
		TextureRegion first = addRegion("font");
		TextureRegion second = addRegion("symbols");
		BitmapFont font = loadFont("font.png", "symbols.png");
		assertSame(first, font.getRegions().get(0));
		assertSame(second, font.getRegions().get(1));
	}

	@Test
	public void prefersIndexedFontRegions () throws IOException {
		TextureRegion first = addRegion("font_0");
		TextureRegion second = addRegion("font_1");
		addRegion("letters");
		addRegion("symbols");
		BitmapFont font = loadFont("letters.png", "symbols.png");
		assertSame(first, font.getRegions().get(0));
		assertSame(second, font.getRegions().get(1));
	}
}
