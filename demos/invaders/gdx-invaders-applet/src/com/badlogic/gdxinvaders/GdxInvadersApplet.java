/*******************************************************************************
 * Copyright 2010 Mario  Zechner (contact@badlogicgames.com)
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
package com.badlogic.gdxinvaders;

import java.applet.Applet;

import com.badlogic.gdx.backends.applet.AppletApplication;

public class GdxInvadersApplet extends Applet
{
	private static final long serialVersionUID = -2444740109586326922L;

	public void init()
	{
		AppletApplication app = new AppletApplication( this, false, false );		
		app.getGraphics().setRenderListener( new GdxInvaders() );
	}
}
