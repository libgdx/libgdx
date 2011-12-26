package com.badlogic.gdx.jnigen;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuildExecutor {
	public static void executeAnt(String buildFile, String params) {
			FileDescriptor build = new FileDescriptor(buildFile);
			String ant = System.getProperty("os.name").contains("Windows")?"ant.bat":"ant";
			String command = ant + " -f " + build.name() + " " + params;
			System.out.println("Executing '" + command + "'");
			startProcess(command, build.parent().file());
	}
	
	public static void executeNdk(String directory) {
		FileDescriptor build = new FileDescriptor(directory);
		String command = "ndk-build";
		startProcess(command, build.file());
	}
	
	private static void startProcess(String command, File directory) {
		try {
			final Process process = Runtime.getRuntime().exec(command, null, directory);
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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
