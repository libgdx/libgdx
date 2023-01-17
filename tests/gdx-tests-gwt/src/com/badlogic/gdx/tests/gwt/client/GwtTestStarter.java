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

package com.badlogic.gdx.tests.gwt.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.gwt.user.client.Window;

public class GwtTestStarter extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration config = new GwtApplicationConfiguration(true);
		config.useGyroscope = true;
		config.padVertical = 150;

		// Look for URL parameter '?useGL30=true' to enable WebGL2
		config.useGL30 = Boolean.parseBoolean(Window.Location.getParameter("useGL30"));

		if (config.useGL30) {
			ShaderProgram.prependVertexCode = "#version 300 es\n#define varying out\n#define attribute in\n";
			ShaderProgram.prependFragmentCode = "#version 300 es\n#define varying in\n#define texture2D texture\n#define gl_FragColor fragColor\nprecision mediump float;\nout vec4 fragColor;\n";
		}

		// config.openURLInNewWindow = true;
		return config;
	}

	@Override
	public ApplicationListener createApplicationListener () {
		return new GwtTestWrapper();
	}
}
