package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Sort;

/**
 * <p>
 * Minimalistic grouping strategy useful for orthogonal scenes where the camera faces the negative z axis.
 * Handles enabling and disabling of blending and uses world-z only front to back sorting for transparent decals.
 * </p>
 * <p>
 * States (* = any, EV = entry value - same as value before flush):<br/>
 * <table>
 * <tr>
 * <td></td><td>expects</td><td>exits on</td>
 * </tr>
 * <tr>
 * <td>glDepthMask</td><td>true</td><td>EV | true</td>
 * </tr>
 * <tr>
 * <td>GL_DEPTH_TEST</td><td>enabled</td><td>EV</td>
 * </tr>
 * <tr>
 * <td>glDepthFunc</td><td>GL_LESS | GL_LEQUAL</td><td>EV</td>
 * </tr>
 * <tr>
 * <td>GL_BLEND</td><td>disabled</td><td>EV | disabled</td>
 * </tr>
 * <tr>
 * <td>glBlendFunc</td><td>*</td><td>*</td>
 * </tr>
 * <tr>
 * <td>GL_TEXTURE_2D</td><td>*</td><td>disabled</td>
 * </tr>
 * </table>
 * </p>
 */
public class SimpleOrthoGroupStrategy implements GroupStrategy {
	private Comparator comparator = new Comparator();
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;

	@Override
	public int decideGroup(Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup(int group, ObjectMap.Values<Array<Decal>> contents) {
		if(group == GROUP_BLEND) {
			for(Array<Decal> entry : contents) {
				Sort.instance().sort(entry, comparator);
			}
			Gdx.gl10.glEnable(GL10.GL_BLEND);
			//no need for writing into the z buffer if transparent decals are the last thing to be rendered
			//and they are rendered back to front
			Gdx.gl10.glDepthMask(false);
		}
	}

	@Override
	public void afterGroup(int group) {
		if(group == GROUP_BLEND) {
			Gdx.gl10.glDepthMask(true);
			Gdx.gl10.glDisable(GL10.GL_BLEND);
		}
	}

	@Override
	public void beforeGroups() {
		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
	}

	@Override
	public void afterGroups() {
		Gdx.gl10.glDisable(GL10.GL_TEXTURE_2D);
	}

	private class Comparator implements java.util.Comparator<Decal> {
		@Override
		public int compare(Decal a, Decal b) {
			return a.getZ() - b.getZ() < 0 ? -1 : 1;
		}
	}
}
