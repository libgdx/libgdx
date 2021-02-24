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

package com.badlogic.gdx.utils;

import com.google.gwt.user.client.Window.Navigator;

public class PlatformUtils {

	static public boolean isAndroid = Navigator.getPlatform().contains("Android Runtime");
	static public boolean isMac = Navigator.getPlatform().contains("Mac");
	static public boolean isWindows = Navigator.getPlatform().contains("Win");
	static public boolean isLinux = Navigator.getPlatform().contains("Linux");
	static public boolean isIos = Navigator.getPlatform().contains("iPhone") || Navigator.getPlatform().contains("iPod")
		|| Navigator.getPlatform().contains("iPad");

}
