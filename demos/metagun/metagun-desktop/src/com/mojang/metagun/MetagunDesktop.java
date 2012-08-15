
package com.mojang.metagun;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class MetagunDesktop {
	public static void main (String[] argv) {
		new LwjglApplication(new Metagun(), "Metagun", 320, 240, false);
	}
}
