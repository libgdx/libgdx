package com.badlogic.gdx.jnigen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class AntScriptExecutor {
	public static void execute(String buildFile, String params) {
		try {
			FileDescriptor build = new FileDescriptor(buildFile);
			String ant = System.getProperty("os.name").contains("Windows")?"ant.bat":"ant";
			String command = ant + " -f " + build.name() + " " + params;
			System.out.println("Executing '" + command + "'");
			final Process process = Runtime.getRuntime().exec(command, null, build.parent().file());
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					try {
						while((line = reader.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			});
			t.setDaemon(true);
			t.start();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
