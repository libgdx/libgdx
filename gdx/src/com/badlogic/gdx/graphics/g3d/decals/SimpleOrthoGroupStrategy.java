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

package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;

/** <p>
 * Minimalistic grouping strategy useful for orthogonal scenes where the camera faces the negative z axis. Handles enabling and
 * disabling of blending and uses world-z only front to back sorting for transparent decals.
 * </p>
 * <p>
 * States (* = any, EV = entry value - same as value before flush):<br/>
 * <table>
 * <tr>
 * <td></td>
 * <td>expects</td>
 * <td>exits on</td>
 * </tr>
 * <tr>
 * <td>glDepthMask</td>
 * <td>true</td>
 * <td>EV | true</td>
 * </tr>
 * <tr>
 * <td>GL_DEPTH_TEST</td>
 * <td>enabled</td>
 * <td>EV</td>
 * </tr>
 * <tr>
 * <td>glDepthFunc</td>
 * <td>GL_LESS | GL_LEQUAL</td>
 * <td>EV</td>
 * </tr>
 * <tr>
 * <td>GL_BLEND</td>
 * <td>disabled</td>
 * <td>EV | disabled</td>
 * </tr>
 * <tr>
 * <td>glBlendFunc</td>
 * <td>*</td>
 * <td>*</td>
 * </tr>
 * <tr>
 * <td>GL_TEXTURE_2D</td>
 * <td>*</td>
 * <td>disabled</td>
 * </tr>
 * </table>
 * </p> */
public class SimpleOrthoGroupStrategy implements GroupStrategy {
	private Comparator comparator = new Comparator();
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;

	@Override
	public int decideGroup (Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup (int group, Array<Decal> contents) {
		if (group == GROUP_BLEND) {
			Sort.instance().sort(contents, comparator);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			// no need for writing into the z buffer if transparent decals are the last thing to be rendered
			// and they are rendered back to front
			Gdx.gl.glDepthMask(false);
		} else {
			// FIXME sort by material
		}
	}

	@Override
	public void afterGroup (int group) {
		if (group == GROUP_BLEND) {
			Gdx.gl.glDepthMask(true);
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}

	@Override
	public void beforeGroups () {
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
	}

	@Override
	public void afterGroups () {
		Gdx.gl.glDisable(GL20.GL_TEXTURE_2D);
	}

	class Comparator implements java.util.Comparator<Decal> {
		@Override
		public int compare (Decal a, Decal b) {
			if (a.getZ() == b.getZ()) return 0;
			return a.getZ() - b.getZ() < 0 ? -1 : 1;
		}
	}

	@Override
	public ShaderProgram getGroupShader (int group) {
		return null;
	}
}
