/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.badlogic.gdx.jglfw.tests;

import com.badlogic.gdx.backends.jglfw.JglfwApplication;
import com.badlogic.gdx.backends.jglfw.JglfwApplicationConfiguration;
import com.badlogic.gdx.tests.SpriteBatchShaderTest;
import com.badlogic.gdx.tests.VertexArrayTest;
import com.badlogic.gdx.tests.superkoalio.SuperKoalio;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class JglfwDebugStarter {
	public static void main (String[] argv) {
		// this is only here for me to debug native code faster
		new SharedLibraryLoader("../../extensions/gdx-audio/libs/gdx-audio-natives.jar").load("gdx-audio");
		new SharedLibraryLoader("../../extensions/gdx-image/libs/gdx-image-natives.jar").load("gdx-image");
		new SharedLibraryLoader("../../extensions/gdx-freetype/libs/gdx-freetype-natives.jar").load("gdx-freetype");
		new SharedLibraryLoader("../../extensions/gdx-controllers/gdx-controllers-desktop/libs/gdx-controllers-desktop-natives.jar").load("gdx-controllers-desktop");
		new SharedLibraryLoader("../../gdx/libs/gdx-natives.jar").load("gdx");

		GdxTest test = new SpriteBatchShaderTest();
		JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
		config.useGL20 = test.needsGL20();
		new JglfwApplication(test, config);
	}
}
