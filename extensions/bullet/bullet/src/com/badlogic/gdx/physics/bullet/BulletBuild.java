package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class BulletBuild {
	public static void main(String[] args) throws Exception {
		new NativeCodeGenerator().generate("src/", "bin/", "jni/");
	}
}
