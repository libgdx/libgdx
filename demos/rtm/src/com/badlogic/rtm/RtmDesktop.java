package com.badlogic.rtm;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class RtmDesktop {
	public static void main(String[] argv) {
		new JoglApplication(new LevelRenderer(), "RTM", 480, 320, false);
	}
}
