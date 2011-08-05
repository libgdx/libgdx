package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Sort;

import java.util.Comparator;

/**
 * <p>
 * Minimalistic grouping strategy that splits decals into opaque and transparent ones enabling and disabling blending
 * as needed. Opaque decals are rendered first (decal color is ignored in opacity check).<br/>
 * Use this strategy only if the vast majority of your decals are opaque and the few transparent ones are unlikely
 * to overlap.
 * </p>
 * <p>
 * Can produce invisible artifacts when transparent decals overlap each other.
 * </p>
 * <p>
 * States (* = any, EV = entry value - same as value before flush):<br/>
 * <table>
 * <tr>
 * <td></td><td>expects</td><td>exits on</td>
 * </tr>
 * <tr>
 * <td>glDepthMask</td><td>true</td><td>EV</td>
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
public class DefaultGroupStrategy implements GroupStrategy {
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;

	@Override
	public int decideGroup(Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup(int group, ObjectMap.Values<Array<Decal>> contents) {
		if(group == GROUP_BLEND) {
			Gdx.gl10.glEnable(GL10.GL_BLEND);
		}
	}

	@Override
	public void afterGroup(int group) {
		if(group == GROUP_BLEND) {
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
}
