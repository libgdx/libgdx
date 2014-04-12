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

package com.badlogic.gdx.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Executor {
	public interface CharCallback {
		public void character(char c);
	}
	
	/** Execute the Ant script file with the given parameters.
	 * @return whether the Ant succeeded */
	public static boolean execute (File workingDir, String windowsFile, String unixFile, String parameters, CharCallback callback) {
		String exec = workingDir.getAbsolutePath() + "/" + (System.getProperty("os.name").contains("Windows") ? windowsFile : unixFile);
		String command = exec + " " + parameters;
		String log = "Executing '" + command + "'";
		for(int i = 0; i < log.length(); i++) {
			callback.character(log.charAt(i));
		}
		callback.character('\n');
		return startProcess(command, workingDir, callback);
	}

	private static boolean startProcess (String command, File directory, final CharCallback callback) {
		try {
			final Process process = new ProcessBuilder(command.split(" ")).redirectErrorStream(true).directory(directory).start();

			Thread t = new Thread(new Runnable() {
				@Override
				public void run () {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1);
					try {
						int c = 0;
						while ((c = reader.read()) != -1) {
							callback.character((char)c);						
						}
					} catch (IOException e) {
//						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			process.waitFor();
			t.interrupt();
			return process.exitValue() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
